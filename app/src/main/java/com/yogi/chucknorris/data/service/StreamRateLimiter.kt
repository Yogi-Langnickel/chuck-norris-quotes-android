package com.yogi.chucknorris.data.service

import java.time.Clock

class StreamRateLimiter(
    private val maxRequests: Int = 10,
    private val windowMillis: Long = 60_000,
    private val clock: Clock = Clock.systemUTC()
) {
    private val requestTimes = ArrayDeque<Long>()

    @Synchronized
    fun checkRequestAllowed(streamName: String) {
        val now = clock.millis()
        while (requestTimes.isNotEmpty() && now - requestTimes.first() >= windowMillis) {
            requestTimes.removeFirst()
        }

        if (requestTimes.size >= maxRequests) {
            throw FactServiceException("$streamName requests are limited to $maxRequests per minute.")
        }

        requestTimes.addLast(now)
    }
}
