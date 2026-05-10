package com.yogi.chucknorris.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

class ApiService(
    private val client: HttpClient,
    private val chuckRateLimiter: StreamRateLimiter = StreamRateLimiter(),
    private val catRateLimiter: StreamRateLimiter = StreamRateLimiter()
) : FactService {
    private var nextCatProvider = CatFactProvider.CATFACT_NINJA

    override suspend fun getRandomJoke(): String {
        return try {
            chuckRateLimiter.checkRequestAllowed("Chuck Norris")
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
            catRateLimiter.checkRequestAllowed("Cat fact")
            when (nextCatFactProvider()) {
                CatFactProvider.CATFACT_NINJA -> getCatFactNinjaFact()
                CatFactProvider.MEOWFACTS -> getMeowFactsFact()
            }
        } catch (e: FactServiceException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw FactServiceException("Cat Fact API request failed.", e)
        }
    }

    private suspend fun getCatFactNinjaFact(): String {
        val response = client.get("https://catfact.ninja/fact")
        if (response.status == HttpStatusCode.OK) {
            val catFact: CatFactResponse = response.body()
            return catFact.fact.ifBlank {
                throw FactServiceException("Cat Fact Ninja returned an empty fact.")
            }
        } else {
            throw FactServiceException("Cat Fact Ninja failed with status ${response.status.value}.")
        }
    }

    private suspend fun getMeowFactsFact(): String {
        val response = client.get("https://meowfacts.herokuapp.com/")
        if (response.status == HttpStatusCode.OK) {
            val catFact: MeowFactsResponse = response.body()
            return catFact.data.firstOrNull { it.isNotBlank() }
                ?: throw FactServiceException("MeowFacts returned an empty fact.")
        } else {
            throw FactServiceException("MeowFacts failed with status ${response.status.value}.")
        }
    }

    @Synchronized
    private fun nextCatFactProvider(): CatFactProvider {
        return nextCatProvider.also { current ->
            nextCatProvider = when (current) {
                CatFactProvider.CATFACT_NINJA -> CatFactProvider.MEOWFACTS
                CatFactProvider.MEOWFACTS -> CatFactProvider.CATFACT_NINJA
            }
        }
    }

    private enum class CatFactProvider {
        CATFACT_NINJA,
        MEOWFACTS
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

data class MeowFactsResponse(
    val data: List<String> = emptyList()
)
