package com.yogi.quotebattleroyal.data.repository

import com.yogi.quotebattleroyal.data.model.Quote
import com.yogi.quotebattleroyal.domain.BattleContender
import com.yogi.quotebattleroyal.domain.BattleRound
import com.yogi.quotebattleroyal.domain.FactSource

interface QuoteDataSource {
    suspend fun getRandomQuote(): Quote
    suspend fun getRandomCatFact(): Quote
    suspend fun getRandomDogFact(): Quote
    suspend fun getBattleRound(): BattleRound
    suspend fun getBattleChallenger(excludedSources: Set<FactSource>): BattleContender
}
