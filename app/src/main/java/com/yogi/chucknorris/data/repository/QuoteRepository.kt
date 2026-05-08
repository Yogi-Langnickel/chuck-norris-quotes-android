package com.yogi.chucknorris.data.repository

import com.yogi.chucknorris.data.model.Quote
import com.yogi.chucknorris.data.service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class QuoteRepository(private val apiService: ApiService) {

    suspend fun getRandomQuote(): Quote {
        return withContext(Dispatchers.IO) {
            val jokeString = apiService.getRandomJoke()
            Quote(
                id = UUID.randomUUID().toString(),
                value = jokeString
            )
        }
    }
}