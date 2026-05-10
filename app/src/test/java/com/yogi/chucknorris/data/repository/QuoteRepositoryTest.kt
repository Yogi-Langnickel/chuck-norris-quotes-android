package com.yogi.chucknorris.data.repository

import com.yogi.chucknorris.data.service.FactService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
    fun getBattleRound_returnsBothContendersWithPowerProfiles() = runBlocking {
        val repository = QuoteRepository(
            fakeFactService(
                joke = "Chuck Norris can divide by zero.",
                catFact = "Cats have excellent night vision."
            )
        )

        val round = repository.getBattleRound()

        assertEquals("Chuck Norris can divide by zero.", round.chuck.quote.value)
        assertEquals("Chuck Norris", round.chuck.quote.sourceLabel)
        assertEquals("Cats have excellent night vision.", round.cat.quote.value)
        assertEquals("Cat Fact", round.cat.quote.sourceLabel)
        assertTrue(round.chuck.powerProfile.score in 25..100)
        assertTrue(round.cat.powerProfile.score in 25..100)
    }

    private fun fakeFactService(joke: String, catFact: String) = object : FactService {
        override suspend fun getRandomJoke() = joke
        override suspend fun getRandomCatFact() = catFact
    }
}
