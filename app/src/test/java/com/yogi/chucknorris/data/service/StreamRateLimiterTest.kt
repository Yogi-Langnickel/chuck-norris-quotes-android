package com.yogi.chucknorris.data.service

import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class StreamRateLimiterTest {

    @Test
    fun checkRequestAllowed_allowsConfiguredRequestsWithinWindow() {
        val clock = MutableClock()
        val limiter = StreamRateLimiter(maxRequests = 2, windowMillis = 60_000, clock = clock)

        limiter.checkRequestAllowed("test")
        limiter.checkRequestAllowed("test")
    }

    @Test
    fun checkRequestAllowed_rejectsRequestsPastLimitWithinWindow() {
        val clock = MutableClock()
        val limiter = StreamRateLimiter(maxRequests = 2, windowMillis = 60_000, clock = clock)

        limiter.checkRequestAllowed("test")
        limiter.checkRequestAllowed("test")

        assertThrows(FactServiceException::class.java) {
            limiter.checkRequestAllowed("test")
        }
    }

    @Test
    fun checkRequestAllowed_allowsRequestsAfterWindowExpires() {
        val clock = MutableClock()
        val limiter = StreamRateLimiter(maxRequests = 2, windowMillis = 60_000, clock = clock)

        limiter.checkRequestAllowed("test")
        limiter.checkRequestAllowed("test")
        clock.advanceMillis(60_000)

        limiter.checkRequestAllowed("test")
    }

    private class MutableClock(
        private var instant: Instant = Instant.parse("2026-05-11T00:00:00Z")
    ) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")

        override fun withZone(zone: ZoneId): Clock = this

        override fun instant(): Instant = instant

        fun advanceMillis(millis: Long) {
            instant = instant.plusMillis(millis)
        }
    }
}
