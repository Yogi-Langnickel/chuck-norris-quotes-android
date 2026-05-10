package com.yogi.chucknorris.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiService(private val client: HttpClient) : FactService {
    override suspend fun getRandomJoke(): String {
        return try {
            val response = client.get("https://api.chucknorris.io/jokes/random")
            if (response.status == HttpStatusCode.OK) {
                val joke: JokeResponse = response.body()
                joke.value
            } else {
                "Chuck Norris is currently unreachable."
            }
        } catch (e: Exception) {
            "Chuck Norris is currently unreachable."
        }
    }

    override suspend fun getRandomCatFact(): String {
        return try {
            val response = client.get("https://catfact.ninja/fact")
            if (response.status == HttpStatusCode.OK) {
                val catFact: CatFactResponse = response.body()
                catFact.fact
            } else {
                "The cat facts are currently napping."
            }
        } catch (e: Exception) {
            "The cat facts are currently napping."
        }
    }
}

data class JokeResponse(
    val value: String,
    val icon_url: String? = null,
    val id: String? = null,
    val url: String? = null
)

data class CatFactResponse(
    val fact: String,
    val length: Int? = null
)
