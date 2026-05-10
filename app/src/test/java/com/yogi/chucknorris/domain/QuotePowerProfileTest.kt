package com.yogi.chucknorris.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuotePowerProfileTest {

    @Test
    fun from_returnsDeterministicScoreForSameQuote() {
        val quote = "Chuck Norris counted to infinity. Twice."

        val firstProfile = QuotePowerProfile.from(quote)
        val secondProfile = QuotePowerProfile.from(quote)

        assertEquals(firstProfile, secondProfile)
    }

    @Test
    fun from_keepsScoreInsideDisplayRange() {
        val profile = QuotePowerProfile.from("A short fact.")

        assertTrue(profile.score in 25..100)
        assertTrue(profile.progress in 0.25f..1.0f)
    }

    @Test
    fun from_countsWordsAfterTrimmingWhitespace() {
        val profile = QuotePowerProfile.from("  Roundhouse kicks need no introduction  ")

        assertEquals(5, profile.wordCount)
    }
}
