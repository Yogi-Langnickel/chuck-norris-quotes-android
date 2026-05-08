package com.yogi.chucknorris.data.model

/**
 * Data class representing a Chuck Norris quote.
 *
 * @property id Unique identifier for the quote.
 * @property value The text of the quote.
 */
data class Quote(
    val id: String,
    val value: String
)