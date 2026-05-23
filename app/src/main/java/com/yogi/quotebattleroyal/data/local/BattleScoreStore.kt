package com.yogi.quotebattleroyal.data.local

import com.yogi.quotebattleroyal.domain.BattlePeriod
import com.yogi.quotebattleroyal.domain.BattleScore
import com.yogi.quotebattleroyal.domain.BattleWinner

interface BattleScoreStore {
    fun getScore(period: BattlePeriod): BattleScore
    fun getScores(): Map<BattlePeriod, BattleScore>
    fun recordBattle(winner: BattleWinner): Map<BattlePeriod, BattleScore>
}
