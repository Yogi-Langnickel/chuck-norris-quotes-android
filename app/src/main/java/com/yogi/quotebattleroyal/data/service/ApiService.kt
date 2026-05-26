package com.yogi.quotebattleroyal.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class ApiService(
    private val client: HttpClient,
    private val chuckRateLimiter: StreamRateLimiter = StreamRateLimiter(),
    private val catRateLimiter: StreamRateLimiter = StreamRateLimiter(),
    private val dogRateLimiter: StreamRateLimiter = StreamRateLimiter(),
    private val yogiRateLimiter: StreamRateLimiter = StreamRateLimiter(),
    private val dogProviderTimeoutMs: Long = 4_500,
    private val yogiProviderTimeoutMs: Long = 4_500
) : FactService {
    private var nextCatProvider = CatFactProvider.CATFACT_NINJA
    private var nextYogiProvider = YogiProvider.ADVICE_SLIP
    private var nextEmergencyDogFactIndex = 0

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

    override suspend fun getRandomDogFact(): String {
        dogRateLimiter.checkRequestAllowed("Dog fact")
        var lastError: Throwable? = null
        DogFactProvider.entries.forEach { provider ->
            try {
                return withTimeout(dogProviderTimeoutMs) {
                    when (provider) {
                        DogFactProvider.DOG_API_V2 -> getDogApiV2Fact()
                        DogFactProvider.KINDUFF_CLASSIC -> getKinduffDogFact()
                        DogFactProvider.EMERGENCY_LOCAL -> getEmergencyDogFact()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                lastError = e
            } catch (e: FactServiceException) {
                lastError = e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }

        throw FactServiceException("Dog fact providers are unavailable right now.", lastError)
    }

    override suspend fun getRandomYogiQuote(): String {
        yogiRateLimiter.checkRequestAllowed("Ask Yogi")
        val providers = rotatedYogiProviders()
        var lastError: Throwable? = null

        providers.forEach { provider ->
            try {
                return withTimeout(yogiProviderTimeoutMs) {
                    when (provider) {
                        YogiProvider.ADVICE_SLIP -> getAdviceSlip()
                        YogiProvider.ICANHAZ_DAD_JOKE -> getIcanhazDadJoke()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                lastError = e
            } catch (e: FactServiceException) {
                lastError = e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }

        return YOGI_API_FALLBACK
    }

    private suspend fun getDogApiV2Fact(): String {
        return try {
            val response = client.get("https://dogapi.dog/api/v2/facts?limit=1")
            if (response.status == HttpStatusCode.OK) {
                val dogFact: DogApiResponse = response.body()
                dogFact.data
                    .firstOrNull()
                    ?.attributes
                    ?.body
                    ?.takeIf { it.isNotBlank() }
                    ?: throw FactServiceException("Dog API returned an empty fact.")
            } else {
                throw FactServiceException("Dog API failed with status ${response.status.value}.")
            }
        } catch (e: FactServiceException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw FactServiceException("Dog API request failed.", e)
        }
    }

    private suspend fun getKinduffDogFact(): String {
        return try {
            val response = client.get("https://dog-api.kinduff.com/api/facts")
            if (response.status == HttpStatusCode.OK) {
                val dogFact: KinduffDogFactResponse = response.body()
                if (!dogFact.success) {
                    throw FactServiceException("Kinduff Dog Facts returned an unsuccessful response.")
                }
                dogFact.facts
                    .firstOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: throw FactServiceException("Kinduff Dog Facts returned an empty fact.")
            } else {
                throw FactServiceException("Kinduff Dog Facts failed with status ${response.status.value}.")
            }
        } catch (e: FactServiceException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw FactServiceException("Kinduff Dog Facts request failed.", e)
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

    private suspend fun getAdviceSlip(): String {
        val response = client.get("https://api.adviceslip.com/advice")
        if (response.status == HttpStatusCode.OK) {
            val advice: AdviceSlipResponse = response.body()
            return cleanYogiText(
                advice.slip?.advice,
                "Advice Slip returned an empty response."
            )
        } else {
            throw FactServiceException("Advice Slip failed with status ${response.status.value}.")
        }
    }

    private suspend fun getIcanhazDadJoke(): String {
        val response = client.get("https://icanhazdadjoke.com/") {
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }
        if (response.status == HttpStatusCode.OK) {
            val joke: IcanhazDadJokeResponse = response.body()
            return cleanYogiText(
                joke.joke,
                "I Can Haz Dad Joke returned an empty joke."
            )
        } else {
            throw FactServiceException("I Can Haz Dad Joke failed with status ${response.status.value}.")
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

    @Synchronized
    private fun rotatedYogiProviders(): List<YogiProvider> {
        val start = nextYogiProvider
        val providers = YogiProvider.entries
        nextYogiProvider = providers[(start.ordinal + 1) % providers.size]
        return providers.drop(start.ordinal) + providers.take(start.ordinal)
    }

    private fun cleanYogiText(value: String?, emptyMessage: String): String {
        val normalized = value
            ?.trim()
            ?.replace(whitespace, " ")
            ?: throw FactServiceException(emptyMessage)

        if (normalized.isBlank()) {
            throw FactServiceException(emptyMessage)
        }
        if (normalized.length > MAX_YOGI_TEXT_LENGTH) {
            throw FactServiceException("Ask Yogi returned an oversized response.")
        }
        if (normalized.any { it.isISOControl() || Character.getType(it) == Character.FORMAT.toInt() }) {
            throw FactServiceException("Ask Yogi returned unsupported control characters.")
        }
        if (unsafeYogiFragments.any { normalized.contains(it, ignoreCase = true) }) {
            throw FactServiceException("Ask Yogi returned content outside the app tone.")
        }

        return normalized
    }

    private enum class CatFactProvider {
        CATFACT_NINJA,
        MEOWFACTS
    }

    private enum class YogiProvider {
        ADVICE_SLIP,
        ICANHAZ_DAD_JOKE
    }

    private enum class DogFactProvider {
        DOG_API_V2,
        KINDUFF_CLASSIC,
        EMERGENCY_LOCAL
    }

    @Synchronized
    private fun getEmergencyDogFact(): String {
        val fact = emergencyDogFacts[nextEmergencyDogFactIndex % emergencyDogFacts.size]
        nextEmergencyDogFactIndex += 1
        return fact
    }

    private companion object {
        private const val MAX_YOGI_TEXT_LENGTH = 280
        private const val YOGI_API_FALLBACK = "Even monkeys need a break sometimes. (API Error)"
        val whitespace = Regex("\\s+")
        val unsafeYogiFragments = listOf(
            "kill yourself",
            "kys",
            "rape",
            "nazi",
            "hitler"
        )
        val emergencyDogFacts = listOf(
            "Dogs have a sense of smell far stronger than humans.",
            "Dogs can learn more than 100 words and gestures.",
            "A dog's nose print is unique, much like a human fingerprint.",
            "Dogs use their tails, ears, posture, and eyes to communicate.",
            "Puppies are born deaf and begin hearing after about two weeks."
        )
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

data class DogApiResponse(
    val data: List<DogFactResource> = emptyList()
)

data class DogFactResource(
    val attributes: DogFactAttributes? = null
)

data class DogFactAttributes(
    val body: String? = null
)

data class KinduffDogFactResponse(
    val facts: List<String> = emptyList(),
    val success: Boolean = false
)

data class AdviceSlipResponse(
    val slip: AdviceSlip? = null
)

data class AdviceSlip(
    val advice: String? = null
)

data class IcanhazDadJokeResponse(
    val id: String? = null,
    val joke: String? = null,
    val status: Int? = null
)
