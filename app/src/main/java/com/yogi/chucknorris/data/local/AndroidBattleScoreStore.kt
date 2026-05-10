package com.yogi.chucknorris.data.local

import android.content.SharedPreferences
import com.yogi.chucknorris.domain.BattlePeriod
import com.yogi.chucknorris.domain.BattleScore
import com.yogi.chucknorris.domain.BattleWinner
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

class AndroidBattleScoreStore(
    private val sharedPreferences: SharedPreferences,
    private val clock: Clock = Clock.systemDefaultZone()
) : BattleScoreStore {

    override fun getScore(period: BattlePeriod): BattleScore {
        return readScore(periodKey(period))
    }

    override fun getScores(): Map<BattlePeriod, BattleScore> {
        return BattlePeriod.entries.associateWith(::getScore)
    }

    override fun recordBattle(winner: BattleWinner): Map<BattlePeriod, BattleScore> {
        BattlePeriod.entries.forEach { period ->
            val key = periodKey(period)
            val nextScore = readScore(key).record(winner)
            sharedPreferences.edit()
                .putInt("$key.chuck", nextScore.chuckWins)
                .putInt("$key.cat", nextScore.catWins)
                .putInt("$key.draw", nextScore.draws)
                .apply()
        }
        return getScores()
    }

    private fun readScore(key: String): BattleScore {
        return BattleScore(
            chuckWins = sharedPreferences.getInt("$key.chuck", 0),
            catWins = sharedPreferences.getInt("$key.cat", 0),
            draws = sharedPreferences.getInt("$key.draw", 0)
        )
    }

    private fun periodKey(period: BattlePeriod): String {
        val today = LocalDate.now(clock)
        val suffix = when (period) {
            BattlePeriod.DAILY -> today.format(DateTimeFormatter.BASIC_ISO_DATE)
            BattlePeriod.WEEKLY -> {
                val weekFields = WeekFields.ISO
                val weekYear = today.get(weekFields.weekBasedYear())
                val week = today.get(weekFields.weekOfWeekBasedYear())
                String.format(Locale.US, "%04d-W%02d", weekYear, week)
            }
            BattlePeriod.MONTHLY -> today.format(DateTimeFormatter.ofPattern("yyyyMM"))
        }
        return "battle.$period.$suffix"
    }
}
