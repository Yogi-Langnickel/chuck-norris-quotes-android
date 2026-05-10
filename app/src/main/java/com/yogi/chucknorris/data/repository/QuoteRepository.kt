package com.yogi.chucknorris.data.repository

import com.yogi.chucknorris.data.model.Quote
import com.yogi.chucknorris.data.service.FactService
import com.yogi.chucknorris.data.service.FactServiceException
import com.yogi.chucknorris.domain.BattleContender
import com.yogi.chucknorris.domain.BattleRound
import com.yogi.chucknorris.domain.FactSource
import com.yogi.chucknorris.domain.QuotePowerProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class QuoteRepository(private val factService: FactService) : QuoteDataSource {

    override suspend fun getRandomQuote(): Quote {
        return withContext(Dispatchers.IO) {
            quoteForSource(FactSource.CHUCK)
        }
    }

    override suspend fun getRandomCatFact(): Quote {
        return withContext(Dispatchers.IO) {
            quoteForSource(FactSource.CAT)
        }
    }

    override suspend fun getRandomDogFact(): Quote {
        return withContext(Dispatchers.IO) {
            quoteForSource(FactSource.DOG)
        }
    }

    override suspend fun getBattleRound(): BattleRound {
        return withContext(Dispatchers.IO) {
            val first = battleContender(excludedSources = emptySet())
            val second = battleContender(excludedSources = setOf(first.source))

            BattleRound.from(first, second)
        }
    }

    override suspend fun getBattleChallenger(excludedSources: Set<FactSource>): BattleContender {
        return withContext(Dispatchers.IO) {
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
                val quote = quoteForSource(source)
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
}
