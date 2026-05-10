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
    DOG,
    DRAW
}

enum class FactSource(
    val winner: BattleWinner,
    val sourceLabel: String,
    val scoreLabel: String,
    val initials: String
) {
    CHUCK(BattleWinner.CHUCK, "Chuck Norris", "Chuck", "CH"),
    CAT(BattleWinner.CAT, "Cat Fact", "Cat", "CA"),
    DOG(BattleWinner.DOG, "Dog Fact", "Dog", "DO")
}

data class BattleContender(
    val source: FactSource,
    val quote: Quote,
    val powerProfile: QuotePowerProfile
)

data class BattleRound(
    val first: BattleContender,
    val second: BattleContender,
    val winner: BattleWinner,
    val margin: Int
) {
    val chuck: BattleContender
        get() = contenderFor(FactSource.CHUCK)

    val cat: BattleContender
        get() = contenderFor(FactSource.CAT)

    val dog: BattleContender
        get() = contenderFor(FactSource.DOG)

    val contenders: List<BattleContender>
        get() = listOf(first, second)

    fun contenderFor(source: FactSource): BattleContender {
        return contenders.first { it.source == source }
    }

    fun contenderFor(winner: BattleWinner): BattleContender? {
        return contenders.firstOrNull { it.source.winner == winner }
    }

    companion object {
        fun from(chuckQuote: Quote, catFact: Quote): BattleRound {
            return from(
                BattleContender(FactSource.CHUCK, chuckQuote, QuotePowerProfile.from(chuckQuote.value)),
                BattleContender(FactSource.CAT, catFact, QuotePowerProfile.from(catFact.value))
            )
        }

        fun from(first: BattleContender, second: BattleContender): BattleRound {
            val winner = when {
                first.powerProfile.score > second.powerProfile.score -> first.source.winner
                second.powerProfile.score > first.powerProfile.score -> second.source.winner
                else -> BattleWinner.DRAW
            }

            return BattleRound(
                first = first,
                second = second,
                winner = winner,
                margin = kotlin.math.abs(first.powerProfile.score - second.powerProfile.score)
            )
        }
    }
}

data class BattleScore(
    val chuckWins: Int = 0,
    val catWins: Int = 0,
    val dogWins: Int = 0,
    val draws: Int = 0
) {
    val totalBattles: Int = chuckWins + catWins + dogWins + draws
    val leader: BattleWinner
        get() {
            val wins = mapOf(
                BattleWinner.CHUCK to chuckWins,
                BattleWinner.CAT to catWins,
                BattleWinner.DOG to dogWins
            )
            val topScore = wins.values.maxOrNull() ?: 0
            return if (topScore == 0 || wins.values.count { it == topScore } > 1) {
                BattleWinner.DRAW
            } else {
                wins.entries.first { it.value == topScore }.key
            }
        }
    val leaderMargin: Int
        get() {
            val orderedScores = listOf(chuckWins, catWins, dogWins).sortedDescending()
            return if (leader == BattleWinner.DRAW) 0 else orderedScores[0] - orderedScores[1]
        }

    fun record(winner: BattleWinner): BattleScore {
        return when (winner) {
            BattleWinner.CHUCK -> copy(chuckWins = chuckWins + 1)
            BattleWinner.CAT -> copy(catWins = catWins + 1)
            BattleWinner.DOG -> copy(dogWins = dogWins + 1)
            BattleWinner.DRAW -> copy(draws = draws + 1)
        }
    }
}
