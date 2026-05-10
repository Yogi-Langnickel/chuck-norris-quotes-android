package com.yogi.chucknorris.data.model

/**
 * Data class representing a fetched fact or quote.
 *
 * @property id Unique identifier for the quote.
 * @property value The text of the quote.
 * @property sourceLabel User-facing source label for the current fact.
 */
data class Quote(
    val id: String,
    val value: String,
    val sourceLabel: String = "Chuck Norris"
)
