package com.yogi.chucknorris.data.repository

import com.yogi.chucknorris.data.model.Quote
import com.yogi.chucknorris.data.service.FactService
import com.yogi.chucknorris.domain.BattleRound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class QuoteRepository(private val factService: FactService) : QuoteDataSource {

    override suspend fun getRandomQuote(): Quote {
        return withContext(Dispatchers.IO) {
            val jokeString = factService.getRandomJoke()
            Quote(
                id = UUID.randomUUID().toString(),
                value = jokeString,
                sourceLabel = "Chuck Norris"
            )
        }
    }

    override suspend fun getRandomCatFact(): Quote {
        return withContext(Dispatchers.IO) {
            val catFact = factService.getRandomCatFact()
            Quote(
                id = UUID.randomUUID().toString(),
                value = catFact,
                sourceLabel = "Cat Fact"
            )
        }
    }

    override suspend fun getBattleRound(): BattleRound {
        return withContext(Dispatchers.IO) {
            val chuckQuote = Quote(
                id = UUID.randomUUID().toString(),
                value = factService.getRandomJoke(),
                sourceLabel = "Chuck Norris"
            )
            val catFact = Quote(
                id = UUID.randomUUID().toString(),
                value = factService.getRandomCatFact(),
                sourceLabel = "Cat Fact"
            )

            BattleRound.from(chuckQuote, catFact)
        }
    }
}
