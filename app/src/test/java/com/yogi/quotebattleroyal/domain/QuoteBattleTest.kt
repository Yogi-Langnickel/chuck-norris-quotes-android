package com.yogi.quotebattleroyal.domain

import com.yogi.quotebattleroyal.data.model.Quote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteBattleTest {

    @Test
    fun battleRoundFrom_selectsWinnerFromPowerScores() {
        val chuckQuote = Quote("chuck", "Chuck Norris counted to infinity. Twice.", "Chuck Norris")
        val catFact = Quote("cat", "Cats can rotate their ears.", "Cat Fact")

        val round = BattleRound.from(chuckQuote, catFact)

        val expectedWinner = when {
            round.chuck.powerProfile.score > round.cat.powerProfile.score -> BattleWinner.CHUCK
            round.cat.powerProfile.score > round.chuck.powerProfile.score -> BattleWinner.CAT
            else -> BattleWinner.DRAW
        }
        assertEquals(expectedWinner, round.winner)
        assertEquals(
            kotlin.math.abs(round.chuck.powerProfile.score - round.cat.powerProfile.score),
            round.margin
        )
    }

    @Test
    fun battleScore_recordIncrementsOnlyWinnerBucket() {
        val score = BattleScore()
            .record(BattleWinner.CHUCK)
            .record(BattleWinner.CAT)
            .record(BattleWinner.DOG)
            .record(BattleWinner.YOGI)
            .record(BattleWinner.DRAW)
            .record(BattleWinner.CHUCK)

        assertEquals(2, score.chuckWins)
        assertEquals(1, score.catWins)
        assertEquals(1, score.dogWins)
        assertEquals(1, score.yogiWins)
        assertEquals(1, score.draws)
        assertEquals(6, score.totalBattles)
    }

    @Test
    fun battleScore_reportsCurrentLeaderAndMargin() {
        assertEquals(BattleWinner.DRAW, BattleScore().leader)
        assertEquals(0, BattleScore().leaderMargin)

        val chuckLead = BattleScore(chuckWins = 4, catWins = 1, draws = 2)
        assertEquals(BattleWinner.CHUCK, chuckLead.leader)
        assertEquals(3, chuckLead.leaderMargin)

        val catLead = BattleScore(chuckWins = 2, catWins = 5)
        assertEquals(BattleWinner.CAT, catLead.leader)
        assertEquals(3, catLead.leaderMargin)

        val dogLead = BattleScore(chuckWins = 2, catWins = 1, dogWins = 6)
        assertEquals(BattleWinner.DOG, dogLead.leader)
        assertEquals(4, dogLead.leaderMargin)

        val yogiLead = BattleScore(chuckWins = 2, catWins = 1, dogWins = 3, yogiWins = 7)
        assertEquals(BattleWinner.YOGI, yogiLead.leader)
        assertEquals(4, yogiLead.leaderMargin)

        val tied = BattleScore(chuckWins = 2, catWins = 2, dogWins = 1, yogiWins = 1, draws = 1)
        assertEquals(BattleWinner.DRAW, tied.leader)
        assertEquals(0, tied.leaderMargin)
    }

    @Test
    fun battleStreak_tracksRepeatedChampionAndResetsForNewChampion() {
        val streak = BattleStreak()
            .record(FactSource.CHUCK)
            .record(FactSource.CHUCK)
            .record(FactSource.CAT)
            .record(FactSource.YOGI)

        assertEquals(FactSource.YOGI, streak.champion)
        assertEquals(1, streak.wins)
        assertTrue(streak.isActive)
    }

    @Test
    fun battleRoundFrom_keepsScoresInsidePowerProfileRange() {
        val round = BattleRound.from(
            Quote("chuck", "Chuck Norris can slam a revolving door.", "Chuck Norris"),
            Quote("cat", "Cats sleep for many hours each day.", "Cat Fact")
        )

        assertTrue(round.chuck.powerProfile.score in 25..100)
        assertTrue(round.cat.powerProfile.score in 25..100)
    }

    @Test
    fun battleRound_loserForReturnsNonWinningDogCapableContender() {
        val cat = BattleContender(
            FactSource.CAT,
            Quote("cat", "Cats sleep for many hours each day.", "Cat Fact"),
            QuotePowerProfile.from("Cats sleep for many hours each day.")
        )
        val dog = BattleContender(
            FactSource.DOG,
            Quote("dog", "Dogs can understand human pointing gestures.", "Dog Fact"),
            QuotePowerProfile.from("Dogs can understand human pointing gestures.")
        )
        val round = BattleRound.from(cat, dog)

        assertEquals(cat, round.loserFor(BattleWinner.DOG))
        assertEquals(dog, round.loserFor(BattleWinner.CAT))
        assertEquals(null, round.loserFor(BattleWinner.CHUCK))
        assertEquals(null, round.loserFor(BattleWinner.DRAW))
    }

    @Test
    fun battleRound_loserForReturnsNonWinningYogiCapableContender() {
        val chuck = BattleContender(
            FactSource.CHUCK,
            Quote("chuck", "Chuck Norris can divide by zero.", "Chuck Norris"),
            QuotePowerProfile.from("Chuck Norris can divide by zero.")
        )
        val yogi = BattleContender(
            FactSource.YOGI,
            Quote("yogi", "Yogi says ship the useful part first.", "Yogi"),
            QuotePowerProfile.from("Yogi says ship the useful part first.")
        )
        val round = BattleRound.from(chuck, yogi)

        assertEquals(chuck, round.loserFor(BattleWinner.YOGI))
        assertEquals(yogi, round.loserFor(BattleWinner.CHUCK))
        assertEquals(null, round.loserFor(BattleWinner.CAT))
    }
}
