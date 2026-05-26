package com.yogi.quotebattleroyal.data.repository

import com.yogi.quotebattleroyal.data.model.Quote
import com.yogi.quotebattleroyal.data.service.FactService
import com.yogi.quotebattleroyal.data.service.FactServiceException
import com.yogi.quotebattleroyal.domain.BattleContender
import com.yogi.quotebattleroyal.domain.BattleRound
import com.yogi.quotebattleroyal.domain.FactSource
import com.yogi.quotebattleroyal.domain.QuotePowerProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import java.util.EnumMap
import java.util.UUID

class QuoteRepository(
    private val factService: FactService,
    private val prefetchScope: CoroutineScope? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    prefetchEnabled: Boolean = false
) : QuoteDataSource, AutoCloseable {
    private val prefetchLock = Any()
    private val prefetchedQuotes = EnumMap<FactSource, Deferred<Quote>>(FactSource::class.java)
    private val ownedPrefetchScope = if (prefetchEnabled && prefetchScope == null) {
        CoroutineScope(SupervisorJob() + dispatcher)
    } else {
        null
    }
    private val activePrefetchScope = prefetchScope ?: ownedPrefetchScope

    init {
        prefetchAllSources()
    }

    override suspend fun getRandomQuote(): Quote {
        return withContext(dispatcher) {
            quoteForSourceWithPrefetch(FactSource.CHUCK)
        }
    }

    override suspend fun getRandomCatFact(): Quote {
        return withContext(dispatcher) {
            quoteForSourceWithPrefetch(FactSource.CAT)
        }
    }

    override suspend fun getRandomDogFact(): Quote {
        return withContext(dispatcher) {
            quoteForSourceWithPrefetch(FactSource.DOG)
        }
    }

    override suspend fun getBattleRound(): BattleRound {
        return withContext(dispatcher) {
            val first = battleContender(excludedSources = emptySet())
            val second = battleContender(excludedSources = setOf(first.source))

            BattleRound.from(first, second)
        }
    }

    override suspend fun getBattleChallenger(excludedSources: Set<FactSource>): BattleContender {
        return withContext(dispatcher) {
            battleContender(excludedSources)
        }
    }

    private suspend fun battleContender(excludedSources: Set<FactSource>): BattleContender {
        var lastError: Throwable? = null
        val eligibleSources = FactSource.entries
            .filterNot { it in excludedSources }
            .shuffled()

        eligibleSources.forEach { source ->
            try {
                val quote = quoteForSourceWithPrefetch(source)
                return BattleContender(
                    source = source,
                    quote = quote,
                    powerProfile = QuotePowerProfile.from(quote.value)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }

        throw FactServiceException("No available fact source returned a battle challenger.", lastError)
    }

    private suspend fun quoteForSourceWithPrefetch(source: FactSource): Quote {
        val prefetched = prefetchedQuoteFor(source)
        var shouldRefill = false

        return try {
            val quote = if (prefetched == null) {
                quoteForSource(source)
            } else {
                try {
                    prefetched.await()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    removePrefetchedQuote(source, prefetched)
                    quoteForSource(source)
                }
            }
            shouldRefill = true
            removePrefetchedQuote(source, prefetched)
            quote
        } finally {
            if (shouldRefill) {
                prefetchSource(source)
            }
        }
    }

    private suspend fun quoteForSource(source: FactSource): Quote {
        val value = when (source) {
            FactSource.CHUCK -> factService.getRandomJoke()
            FactSource.CAT -> factService.getRandomCatFact()
            FactSource.DOG -> factService.getRandomDogFact()
        }

        return Quote(
            id = UUID.randomUUID().toString(),
            value = value,
            sourceLabel = source.sourceLabel
        )
    }

    private fun prefetchAllSources() {
        FactSource.entries.forEach(::prefetchSource)
    }

    private fun prefetchSource(source: FactSource) {
        val scope = activePrefetchScope ?: return
        synchronized(prefetchLock) {
            if (prefetchedQuotes[source] == null) {
                prefetchedQuotes[source] = scope.async(dispatcher) {
                    quoteForSource(source)
                }
            }
        }
    }

    private fun prefetchedQuoteFor(source: FactSource): Deferred<Quote>? {
        return synchronized(prefetchLock) {
            prefetchedQuotes[source]
        }
    }

    private fun removePrefetchedQuote(source: FactSource, prefetched: Deferred<Quote>?) {
        if (prefetched == null) return
        synchronized(prefetchLock) {
            if (prefetchedQuotes[source] === prefetched) {
                prefetchedQuotes.remove(source)
            }
        }
    }

    override fun close() {
        ownedPrefetchScope?.cancel()
        synchronized(prefetchLock) {
            prefetchedQuotes.clear()
        }
    }
}
