package com.yogi.chucknorris.domain

import com.yogi.chucknorris.data.model.Quote
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
            .record(BattleWinner.DRAW)
            .record(BattleWinner.CHUCK)

        assertEquals(2, score.chuckWins)
        assertEquals(1, score.catWins)
        assertEquals(1, score.dogWins)
        assertEquals(1, score.draws)
        assertEquals(5, score.totalBattles)
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

        val tied = BattleScore(chuckWins = 2, catWins = 2, dogWins = 1, draws = 1)
        assertEquals(BattleWinner.DRAW, tied.leader)
        assertEquals(0, tied.leaderMargin)
    }

    @Test
    fun battleStreak_tracksRepeatedChampionAndResetsForNewChampion() {
        val streak = BattleStreak()
            .record(FactSource.CHUCK)
            .record(FactSource.CHUCK)
            .record(FactSource.CAT)

        assertEquals(FactSource.CAT, streak.champion)
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
}
