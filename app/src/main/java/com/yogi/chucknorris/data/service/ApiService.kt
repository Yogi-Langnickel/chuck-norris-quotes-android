package com.yogi.chucknorris.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.yogi.chucknorris.data.service.JokeResponse

class ApiService(private val client: HttpClient) {
    suspend fun getRandomJoke(): String {
        return try {
            val response = client.get("https://api.chucknorris.io/jokes/random")
            if (response.status == HttpStatusCode.OK) {
                val joke: JokeResponse = response.body()
                joke.value
            } else {
                "Chuck Norris is currently unreachable."
            }
        } catch (e: Exception) {
            "Network error: ${e.message}"
        }
    }
}

data class JokeResponse(
    val value: String,
    val icon_url: String? = null,
    val id: String? = null,
    val url: String? = null
)