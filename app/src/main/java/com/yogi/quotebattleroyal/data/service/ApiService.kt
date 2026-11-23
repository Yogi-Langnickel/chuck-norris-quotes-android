package com.yogi.quotebattleroyal.data.service

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.Strictness
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeoutOrNull

class ApiService(
    private val client: HttpClient,
    private val chuckRateLimiter: StreamRateLimiter = StreamRateLimiter(),
    private val catRateLimiter: StreamRateLimiter = StreamRateLimiter(),
    private val dogRateLimiter: StreamRateLimiter = StreamRateLimiter(),
    private val yogiRateLimiter: StreamRateLimiter = StreamRateLimiter(),
    private val dogProviderTimeoutMs: Long = 4_500,
    private val yogiProviderTimeoutMs: Long = 4_500,
    private val quoteProviderTimeoutMs: Long = 8_000
) : FactService {
    private var nextCatProvider = CatFactProvider.CATFACT_NINJA
    private var nextYogiProvider = YogiProvider.ADVICE_SLIP
    private var nextEmergencyDogFactIndex = 0

    override suspend fun getRandomJoke(): String {
        chuckRateLimiter.checkRequestAllowed("Chuck Norris")
        val response = readProviderObject("Chuck Norris", "https://api.chucknorris.io/jokes/random", quoteProviderTimeoutMs)
        return checkedRemoteText(response["value"].stringValue(), "Chuck Norris")
    }

    override suspend fun getRandomCatFact(): String {
        catRateLimiter.checkRequestAllowed("Cat fact")
        return when (nextCatFactProvider()) {
            CatFactProvider.CATFACT_NINJA -> {
                val response = readProviderObject("Cat Fact Ninja", "https://catfact.ninja/fact", quoteProviderTimeoutMs)
                checkedRemoteText(response["fact"].stringValue(), "Cat Fact Ninja")
            }
            CatFactProvider.MEOWFACTS -> {
                val response = readProviderObject("MeowFacts", "https://meowfacts.herokuapp.com/", quoteProviderTimeoutMs)
                val facts = response["data"] as? JsonArray
                val text = facts?.firstNotNullOfOrNull { element ->
                    element.stringValue()?.takeIf { it.isNotBlank() }
                }
                checkedRemoteText(text, "MeowFacts")
            }
        }
    }

    override suspend fun getRandomDogFact(): String {
        dogRateLimiter.checkRequestAllowed("Dog fact")
        for (provider in DogFactProvider.entries) {
            try {
                return when (provider) {
                    DogFactProvider.DOG_API_V2 -> getDogApiV2Fact()
                    DogFactProvider.KINDUFF_CLASSIC -> getKinduffDogFact()
                }
            } catch (_: FactServiceException) {
                // Try the next provider for transport, timeout, or data validation failures.
            }
        }
        currentCoroutineContext().ensureActive()
        return getEmergencyDogFact()
    }

    override suspend fun getRandomYogiQuote(): String {
        yogiRateLimiter.checkRequestAllowed("Ask Yogi")
        for (provider in rotatedYogiProviders()) {
            try {
                return when (provider) {
                    YogiProvider.ADVICE_SLIP -> getAdviceSlip()
                    YogiProvider.ICANHAZ_DAD_JOKE -> getIcanhazDadJoke()
                }
            } catch (_: FactServiceException) {
                // Keep the fallback available when a provider fails or rejects a response.
            }
        }
        currentCoroutineContext().ensureActive()
        return YOGI_API_FALLBACK
    }

    private suspend fun getDogApiV2Fact(): String {
        val response = readProviderObject("Dog API", "https://dogapi.dog/api/v2/facts?limit=1", dogProviderTimeoutMs)
        val resource = (response["data"] as? JsonArray)?.firstOrNull() as? JsonObject
        val attributes = resource?.get("attributes") as? JsonObject
        return checkedRemoteText(attributes?.get("body").stringValue(), "Dog API")
    }

    private suspend fun getKinduffDogFact(): String {
        val response = readProviderObject("Kinduff Dog Facts", "https://dog-api.kinduff.com/api/facts", dogProviderTimeoutMs)
        val success = response["success"] as? JsonPrimitive
        if (success?.isBoolean != true || !success.asBoolean) {
            throw FactServiceException("Kinduff Dog Facts returned an unsuccessful response.")
        }
        val facts = response["facts"] as? JsonArray
        return checkedRemoteText(facts?.firstOrNull().stringValue(), "Kinduff Dog Facts")
    }

    private suspend fun getAdviceSlip(): String {
        val response = readProviderObject("Advice Slip", "https://api.adviceslip.com/advice", yogiProviderTimeoutMs)
        val slip = response["slip"] as? JsonObject
        return cleanYogiText(slip?.get("advice").stringValue(), "Advice Slip")
    }

    private suspend fun getIcanhazDadJoke(): String {
        val response = readProviderObject("I Can Haz Dad Joke", "https://icanhazdadjoke.com/", yogiProviderTimeoutMs)
        return cleanYogiText(response["joke"].stringValue(), "I Can Haz Dad Joke")
    }

    private suspend fun readProviderObject(provider: String, url: String, timeoutMs: Long): JsonObject {
        try {
            // withTimeoutOrNull handles only this deadline, so caller cancellation propagates.
            return withTimeoutOrNull(timeoutMs) {
                client.prepareGet(url) { accept(ContentType.Application.Json) }.execute { response ->
                    if (response.status != HttpStatusCode.OK) {
                        throw FactServiceException("$provider failed with status ${response.status.value}.")
                    }

                    // Read one byte past the limit to distinguish a full valid body from truncation.
                    // This also bounds responses with absent or misleading Content-Length headers.
                    val channel = response.bodyAsChannel()
                    val bytes = ByteArray(MAX_RESPONSE_BYTES + 1)
                    var size = 0
                    while (size < bytes.size) {
                        val count = channel.readAvailable(bytes, size, bytes.size - size)
                        if (count < 0) {
                            channel.closedCause?.let { throw it }
                            break
                        }
                        size += count
                    }
                    if (size > MAX_RESPONSE_BYTES) {
                        throw FactServiceException("$provider returned an oversized response.")
                    }
                    val text = bytes.decodeToString(endIndex = size, throwOnInvalidSequence = true)
                    gson.fromJson(text, JsonObject::class.java)
                        ?: throw FactServiceException("$provider returned an empty response.")
                }
            } ?: throw FactServiceException("$provider request timed out.")
        } catch (e: FactServiceException) {
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw FactServiceException("$provider request failed.", e)
        }
    }

    private fun JsonElement?.stringValue(): String? =
        (this as? JsonPrimitive)?.takeIf { it.isString }?.asString

    private fun checkedRemoteText(value: String?, provider: String): String {
        if (value.isNullOrBlank()) {
            throw FactServiceException("$provider returned no valid quote or fact.")
        }
        if (value.length > MAX_REMOTE_TEXT_LENGTH) {
            throw FactServiceException("$provider returned oversized quote or fact text.")
        }
        return value
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

    private fun cleanYogiText(value: String?, provider: String): String {
        val normalized = checkedRemoteText(value, provider).trim().replace(whitespace, " ")
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
        KINDUFF_CLASSIC
    }

    @Synchronized
    private fun getEmergencyDogFact(): String {
        val fact = emergencyDogFacts[nextEmergencyDogFactIndex % emergencyDogFacts.size]
        nextEmergencyDogFactIndex += 1
        return fact
    }

    private companion object {
        private const val MAX_RESPONSE_BYTES = 32 * 1024
        private const val MAX_REMOTE_TEXT_LENGTH = 2_000
        private const val MAX_YOGI_TEXT_LENGTH = 280
        private val gson = GsonBuilder().setStrictness(Strictness.STRICT).create()
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
