package com.yogi.chucknorris.domain

data class QuotePowerProfile(
    val score: Int,
    val title: String,
    val detail: String,
    val wordCount: Int
) {
    val progress: Float = score / 100f

    companion object {
        fun from(quote: String): QuotePowerProfile {
            val normalizedQuote = quote.trim()
            val words = normalizedQuote
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
            val longestWordLength = words.maxOfOrNull { it.length } ?: 0
            val checksum = normalizedQuote.sumOf { it.code } + (words.size * 17) + (longestWordLength * 3)
            val score = 25 + (checksum % 76)

            val title = when {
                score >= 90 -> "Planetary"
                score >= 75 -> "Legendary"
                score >= 60 -> "Maximum"
                else -> "Warm-up"
            }
            val detail = when (title) {
                "Planetary" -> "Physics requested a timeout."
                "Legendary" -> "Handle with reinforced confidence."
                "Maximum" -> "Approved for dramatic retelling."
                else -> "Still stronger than an ordinary fact."
            }

            return QuotePowerProfile(
                score = score,
                title = title,
                detail = detail,
                wordCount = words.size
            )
        }
    }
}
