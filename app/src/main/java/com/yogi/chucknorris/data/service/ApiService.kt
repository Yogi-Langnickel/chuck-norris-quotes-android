package com.yogi.chucknorris.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

class ApiService(private val client: HttpClient) : FactService {
    override suspend fun getRandomJoke(): String {
        return try {
            val response = client.get("https://api.chucknorris.io/jokes/random")
            if (response.status == HttpStatusCode.OK) {
                val joke: JokeResponse = response.body()
                joke.value.ifBlank {
                    throw FactServiceException("Chuck Norris API returned an empty quote.")
                }
            } else {
                throw FactServiceException("Chuck Norris API failed with status ${response.status.value}.")
            }
        } catch (e: FactServiceException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw FactServiceException("Chuck Norris API request failed.", e)
        }
    }

    override suspend fun getRandomCatFact(): String {
        return try {
            val response = client.get("https://catfact.ninja/fact")
            if (response.status == HttpStatusCode.OK) {
                val catFact: CatFactResponse = response.body()
                catFact.fact.ifBlank {
                    throw FactServiceException("Cat Fact API returned an empty fact.")
                }
            } else {
                throw FactServiceException("Cat Fact API failed with status ${response.status.value}.")
            }
        } catch (e: FactServiceException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw FactServiceException("Cat Fact API request failed.", e)
        }
    }
}

class FactServiceException(message: String, cause: Throwable? = null) : Exception(message, cause)

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
