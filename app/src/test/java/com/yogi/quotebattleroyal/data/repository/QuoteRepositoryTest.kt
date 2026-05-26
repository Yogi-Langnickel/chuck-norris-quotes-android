package com.yogi.quotebattleroyal.data.repository

import com.yogi.quotebattleroyal.data.service.FactService
import com.yogi.quotebattleroyal.domain.FactSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteRepositoryTest {

    @Test
    fun getRandomQuote_returnsChuckNorrisSourceLabel() = runBlocking {
        val repository = QuoteRepository(
            fakeFactService(
                joke = "Chuck Norris can divide by zero.",
                catFact = "Cats have excellent night vision."
            )
        )

        val quote = repository.getRandomQuote()

        assertEquals("Chuck Norris can divide by zero.", quote.value)
        assertEquals("Chuck Norris", quote.sourceLabel)
        assertTrue(quote.id.isNotBlank())
    }

    @Test
    fun getRandomCatFact_returnsCatFactSourceLabel() = runBlocking {
        val repository = QuoteRepository(
            fakeFactService(
                joke = "Chuck Norris can divide by zero.",
                catFact = "Cats have excellent night vision."
            )
        )

        val quote = repository.getRandomCatFact()

        assertEquals("Cats have excellent night vision.", quote.value)
        assertEquals("Cat Fact", quote.sourceLabel)
        assertTrue(quote.id.isNotBlank())
    }

    @Test
    fun getRandomDogFact_returnsDogFactSourceLabel() = runBlocking {
        val repository = QuoteRepository(
            fakeFactService(
                dogFact = "Dogs have a strong sense of smell."
            )
        )

        val quote = repository.getRandomDogFact()

        assertEquals("Dogs have a strong sense of smell.", quote.value)
        assertEquals("Dog Fact", quote.sourceLabel)
        assertTrue(quote.id.isNotBlank())
    }

    @Test
    fun getBattleRound_returnsBothContendersWithPowerProfiles() = runBlocking {
        val repository = QuoteRepository(
            fakeFactService(
                joke = "Chuck Norris can divide by zero.",
                catFact = "Cats have excellent night vision."
            )
        )

        val round = repository.getBattleRound()

        assertEquals(2, round.contenders.size)
        assertEquals(2, round.contenders.map { it.source }.toSet().size)
        round.contenders.forEach { contender ->
            assertTrue(contender.quote.value.isNotBlank())
            assertTrue(contender.quote.sourceLabel.isNotBlank())
            assertTrue(contender.powerProfile.score in 25..100)
        }
    }

    @Test
    fun getBattleChallenger_excludesWinnerSourceAndFallsBackOnFailure() = runBlocking {
        val repository = QuoteRepository(
            fakeFactService(
                catFactError = RuntimeException("Cat API failed"),
                dogFact = "Dogs can understand human pointing gestures."
            )
        )

        val challenger = repository.getBattleChallenger(setOf(FactSource.CHUCK))

        assertEquals(FactSource.DOG, challenger.source)
        assertEquals("Dogs can understand human pointing gestures.", challenger.quote.value)
    }

    @Test
    fun getRandomQuote_usesOneSlotPrefetchAndRefillsAfterConsumption() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val factService = QueuedFactService(
            jokes = mutableListOf(
                "Buffered Chuck quote.",
                "Replacement Chuck quote.",
                "Next buffered Chuck quote."
            )
        )
        val repository = QuoteRepository(
            factService = factService,
            prefetchScope = backgroundScope,
            dispatcher = dispatcher
        )
        advanceUntilIdle()

        val firstQuote = repository.getRandomQuote()
        advanceUntilIdle()

        assertEquals("Buffered Chuck quote.", firstQuote.value)
        assertEquals(2, factService.jokeRequestCount)

        val secondQuote = repository.getRandomQuote()

        assertEquals("Replacement Chuck quote.", secondQuote.value)
    }

    @Test
    fun getRandomQuote_propagatesApiFailures() {
        val repository = QuoteRepository(
            fakeFactService(
                jokeError = RuntimeException("Chuck API failed"),
                catFact = "Cats have excellent night vision."
            )
        )

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.getRandomQuote() }
        }
    }

    @Test
    fun getRandomCatFact_propagatesApiFailures() {
        val repository = QuoteRepository(
            fakeFactService(
                joke = "Chuck Norris can divide by zero.",
                catFactError = RuntimeException("Cat API failed")
            )
        )

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.getRandomCatFact() }
        }
    }

    @Test
    fun getRandomDogFact_propagatesApiFailures() {
        val repository = QuoteRepository(
            fakeFactService(
                dogFactError = RuntimeException("Dog API failed")
            )
        )

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.getRandomDogFact() }
        }
    }

    private fun fakeFactService(
        joke: String = "Chuck Norris can divide by zero.",
        jokeError: RuntimeException? = null,
        catFact: String = "Cats have excellent night vision.",
        catFactError: RuntimeException? = null,
        dogFact: String = "Dogs have a strong sense of smell.",
        dogFactError: RuntimeException? = null
    ) = object : FactService {
        override suspend fun getRandomJoke(): String {
            jokeError?.let { throw it }
            return joke
        }

        override suspend fun getRandomCatFact(): String {
            catFactError?.let { throw it }
            return catFact
        }

        override suspend fun getRandomDogFact(): String {
            dogFactError?.let { throw it }
            return dogFact
        }
    }

    private class QueuedFactService(
        private val jokes: MutableList<String> = mutableListOf("Chuck Norris can divide by zero."),
        private val catFacts: MutableList<String> = mutableListOf("Cats have excellent night vision."),
        private val dogFacts: MutableList<String> = mutableListOf("Dogs have a strong sense of smell.")
    ) : FactService {
        var jokeRequestCount = 0
            private set

        override suspend fun getRandomJoke(): String {
            jokeRequestCount++
            return jokes.removeFirst()
        }

        override suspend fun getRandomCatFact(): String {
            return catFacts.removeFirst()
        }

        override suspend fun getRandomDogFact(): String {
            return dogFacts.removeFirst()
        }
    }
}
