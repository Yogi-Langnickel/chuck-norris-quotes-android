package com.yogi.chucknorris.domain

import com.yogi.chucknorris.data.model.Quote

enum class BattlePeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class BattleWinner {
    CHUCK,
    CAT,
    DRAW
}

data class BattleContender(
    val quote: Quote,
    val powerProfile: QuotePowerProfile
)

data class BattleRound(
    val chuck: BattleContender,
    val cat: BattleContender,
    val winner: BattleWinner,
    val margin: Int
) {
    companion object {
        fun from(chuckQuote: Quote, catFact: Quote): BattleRound {
            val chuck = BattleContender(chuckQuote, QuotePowerProfile.from(chuckQuote.value))
            val cat = BattleContender(catFact, QuotePowerProfile.from(catFact.value))
            val winner = when {
                chuck.powerProfile.score > cat.powerProfile.score -> BattleWinner.CHUCK
                cat.powerProfile.score > chuck.powerProfile.score -> BattleWinner.CAT
                else -> BattleWinner.DRAW
            }

            return BattleRound(
                chuck = chuck,
                cat = cat,
                winner = winner,
                margin = kotlin.math.abs(chuck.powerProfile.score - cat.powerProfile.score)
            )
        }
    }
}

data class BattleScore(
    val chuckWins: Int = 0,
    val catWins: Int = 0,
    val draws: Int = 0
) {
    val totalBattles: Int = chuckWins + catWins + draws
    val leader: BattleWinner
        get() = when {
            chuckWins > catWins -> BattleWinner.CHUCK
            catWins > chuckWins -> BattleWinner.CAT
            else -> BattleWinner.DRAW
        }
    val leaderMargin: Int = kotlin.math.abs(chuckWins - catWins)

    fun record(winner: BattleWinner): BattleScore {
        return when (winner) {
            BattleWinner.CHUCK -> copy(chuckWins = chuckWins + 1)
            BattleWinner.CAT -> copy(catWins = catWins + 1)
            BattleWinner.DRAW -> copy(draws = draws + 1)
        }
    }
}
