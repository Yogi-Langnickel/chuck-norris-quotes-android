package com.yogi.chucknorris.data.repository

import com.yogi.chucknorris.data.model.Quote
import com.yogi.chucknorris.domain.BattleRound

interface QuoteDataSource {
    suspend fun getRandomQuote(): Quote
    suspend fun getRandomCatFact(): Quote
    suspend fun getBattleRound(): BattleRound
}
