package com.yogi.chucknorris.data.repository

import com.yogi.chucknorris.data.model.Quote
import com.yogi.chucknorris.domain.BattleContender
import com.yogi.chucknorris.domain.BattleRound
import com.yogi.chucknorris.domain.FactSource

interface QuoteDataSource {
    suspend fun getRandomQuote(): Quote
    suspend fun getRandomCatFact(): Quote
    suspend fun getRandomDogFact(): Quote
    suspend fun getBattleRound(): BattleRound
    suspend fun getBattleChallenger(excludedSources: Set<FactSource>): BattleContender
}
