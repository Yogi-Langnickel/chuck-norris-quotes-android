package com.yogi.chucknorris.data.local

import com.yogi.chucknorris.domain.BattlePeriod
import com.yogi.chucknorris.domain.BattleScore
import com.yogi.chucknorris.domain.BattleWinner

interface BattleScoreStore {
    fun getScore(period: BattlePeriod): BattleScore
    fun getScores(): Map<BattlePeriod, BattleScore>
    fun recordBattle(winner: BattleWinner): Map<BattlePeriod, BattleScore>
}
