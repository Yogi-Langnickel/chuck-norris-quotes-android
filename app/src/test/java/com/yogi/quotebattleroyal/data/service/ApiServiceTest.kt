package com.yogi.quotebattleroyal.data.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ApiServiceTest {

    @Test
    fun getRandomJoke_returnsApiValue() = runBlocking {
        val service = ApiService(
            testClient("""{"value":"Chuck Norris can divide by zero."}""")
        )

        val quote = service.getRandomJoke()

        assertEquals("Chuck Norris can divide by zero.", quote)
    }

    @Test
    fun getRandomJoke_throwsWhenApiFails() {
        val service = ApiService(testClient(status = HttpStatusCode.InternalServerError))

        assertThrows(FactServiceException::class.java) {
            runBlocking { service.getRandomJoke() }
        }
    }

    @Test
    fun getRandomJoke_rethrowsCancellation() {
        val service = ApiService(cancelledClient())

        assertThrows(CancellationException::class.java) {
            runBlocking { service.getRandomJoke() }
        }
    }

    @Test
    fun getRandomCatFact_returnsApiFact() = runBlocking {
        val service = ApiService(
            testClient("""{"fact":"Cats have excellent night vision.","length":33}""")
        )

        val fact = service.getRandomCatFact()

        assertEquals("Cats have excellent night vision.", fact)
    }

    @Test
    fun getRandomCatFact_rotatesBetweenCatFactNinjaAndMeowFacts() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val service = ApiService(
            testClient { requestUrl ->
                requestedUrls += requestUrl
                when (requestUrl) {
                    "https://catfact.ninja/fact" -> """{"fact":"Cats have excellent night vision.","length":33}"""
                    "https://meowfacts.herokuapp.com/" -> """{"data":["Cats can rotate their ears."]}"""
                    else -> "{}"
                }
            }
        )

        val firstFact = service.getRandomCatFact()
        val secondFact = service.getRandomCatFact()

        assertEquals("Cats have excellent night vision.", firstFact)
        assertEquals("Cats can rotate their ears.", secondFact)
        assertEquals(
            listOf("https://catfact.ninja/fact", "https://meowfacts.herokuapp.com/"),
            requestedUrls
        )
    }

    @Test
    fun getRandomCatFact_throwsWhenApiFails() {
        val service = ApiService(testClient(status = HttpStatusCode.InternalServerError))

        assertThrows(FactServiceException::class.java) {
            runBlocking { service.getRandomCatFact() }
        }
    }

    @Test
    fun getRandomCatFact_rethrowsCancellation() {
        val service = ApiService(cancelledClient())

        assertThrows(CancellationException::class.java) {
            runBlocking { service.getRandomCatFact() }
        }
    }

    @Test
    fun getRandomDogFact_returnsApiFact() = runBlocking {
        val service = ApiService(
            testClient(
                """
                    {
                      "data": [
                        {
                          "attributes": {
                            "body": "Dogs can understand human pointing gestures."
                          }
                        }
                      ]
                    }
                """.trimIndent()
            )
        )

        val fact = service.getRandomDogFact()

        assertEquals("Dogs can understand human pointing gestures.", fact)
    }

    @Test
    fun getRandomDogFact_fallsBackToKinduffWhenDogApiFails() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val service = ApiService(
            testClient(
                statusForUrl = { requestUrl ->
                    requestedUrls += requestUrl
                    when (requestUrl) {
                        "https://dogapi.dog/api/v2/facts?limit=1" -> HttpStatusCode.ServiceUnavailable
                        "https://dog-api.kinduff.com/api/facts" -> HttpStatusCode.OK
                        else -> HttpStatusCode.NotFound
                    }
                },
                bodyForUrl = { requestUrl ->
                    when (requestUrl) {
                        "https://dog-api.kinduff.com/api/facts" ->
                            """{"facts":["Dogs can smell snacks through weak excuses."],"success":true}"""
                        else -> "{}"
                    }
                }
            )
        )

        val fact = service.getRandomDogFact()

        assertEquals("Dogs can smell snacks through weak excuses.", fact)
        assertEquals(
            listOf(
                "https://dogapi.dog/api/v2/facts?limit=1",
                "https://dog-api.kinduff.com/api/facts"
            ),
            requestedUrls
        )
    }

    @Test
    fun getRandomDogFact_fallsBackWhenPrimaryTimesOut() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val service = ApiService(
            client = testClient(
                statusForUrl = { HttpStatusCode.OK },
                bodyForUrl = { requestUrl ->
                    requestedUrls += requestUrl
                    when (requestUrl) {
                        "https://dogapi.dog/api/v2/facts?limit=1" ->
                            withTimeout(1) {
                                delay(50)
                                "This response should time out."
                            }
                        "https://dog-api.kinduff.com/api/facts" ->
                            """{"facts":["Fallback dogs arrived before the spinner got comfy."],"success":true}"""
                        else -> "{}"
                    }
                }
            )
        )

        val fact = service.getRandomDogFact()

        assertEquals("Fallback dogs arrived before the spinner got comfy.", fact)
        assertEquals(
            listOf(
                "https://dogapi.dog/api/v2/facts?limit=1",
                "https://dog-api.kinduff.com/api/facts"
            ),
            requestedUrls
        )
    }

    @Test
    fun getRandomDogFact_usesEmergencyFactWhenRemoteProvidersFail() = runBlocking {
        val service = ApiService(testClient(status = HttpStatusCode.InternalServerError))

        val fact = service.getRandomDogFact()

        assertEquals("Dogs have a sense of smell far stronger than humans.", fact)
    }

    @Test
    fun getRandomDogFact_rethrowsCancellation() {
        val service = ApiService(cancelledClient())

        assertThrows(CancellationException::class.java) {
            runBlocking { service.getRandomDogFact() }
        }
    }

    @Test
    fun getRandomYogiQuote_rotatesAcrossActiveProviders() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val acceptHeaders = mutableListOf<String?>()
        val service = ApiService(
            testClient(
                statusForUrl = { HttpStatusCode.OK },
                bodyForUrl = { requestUrl, acceptHeader ->
                    requestedUrls += requestUrl
                    acceptHeaders += acceptHeader
                    when (requestUrl) {
                        "https://api.adviceslip.com/advice" ->
                            """{"slip":{"id":1,"advice":"Build the thing before arguing about it."}}"""
                        "https://icanhazdadjoke.com/" ->
                            """{"id":"R7UvXnsearch","joke":"What do you call a fake noodle? An impasta.","status":200}"""
                        else -> "{}"
                    }
                }
            )
        )

        assertEquals("Build the thing before arguing about it.", service.getRandomYogiQuote())
        assertEquals("What do you call a fake noodle? An impasta.", service.getRandomYogiQuote())
        assertEquals(
            listOf(
                "https://api.adviceslip.com/advice",
                "https://icanhazdadjoke.com/"
            ),
            requestedUrls
        )
        assertEquals("application/json", acceptHeaders.last())
    }

    @Test
    fun getRandomYogiQuote_fallsBackToNextProviderWhenProviderFails() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val service = ApiService(
            testClient(
                statusForUrl = { requestUrl ->
                    requestedUrls += requestUrl
                    when (requestUrl) {
                        "https://api.adviceslip.com/advice" -> HttpStatusCode.ServiceUnavailable
                        "https://icanhazdadjoke.com/" -> HttpStatusCode.OK
                        else -> HttpStatusCode.NotFound
                    }
                },
                bodyForUrl = { requestUrl ->
                    when (requestUrl) {
                        "https://icanhazdadjoke.com/" ->
                            """{"id":"dad-1","joke":"Keep moving forward, but maybe stretch first.","status":200}"""
                        else -> "{}"
                    }
                }
            )
        )

        val quote = service.getRandomYogiQuote()

        assertEquals("Keep moving forward, but maybe stretch first.", quote)
        assertEquals(
            listOf("https://api.adviceslip.com/advice", "https://icanhazdadjoke.com/"),
            requestedUrls
        )
    }

    @Test
    fun getRandomYogiQuote_fallsBackOnEmptyProviderResponse() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val service = ApiService(
            testClient(
                statusForUrl = { HttpStatusCode.OK },
                bodyForUrl = { requestUrl ->
                    requestedUrls += requestUrl
                    when (requestUrl) {
                        "https://api.adviceslip.com/advice" -> """{"slip":{"id":1,"advice":""}}"""
                        "https://icanhazdadjoke.com/" ->
                            """{"id":"dad-2","joke":"Fallback joke arrived.","status":200}"""
                        else -> "{}"
                    }
                }
            )
        )

        val quote = service.getRandomYogiQuote()

        assertEquals("Fallback joke arrived.", quote)
        assertEquals(
            listOf("https://api.adviceslip.com/advice", "https://icanhazdadjoke.com/"),
            requestedUrls
        )
    }

    @Test
    fun getRandomYogiQuote_fallsBackWhenProviderTimesOut() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val service = ApiService(
            client = testClient(
                statusForUrl = { HttpStatusCode.OK },
                bodyForUrl = { requestUrl ->
                    requestedUrls += requestUrl
                    when (requestUrl) {
                        "https://api.adviceslip.com/advice" -> {
                            delay(100)
                            """{"slip":{"id":1,"advice":"Late advice."}}"""
                        }
                        "https://icanhazdadjoke.com/" ->
                            """{"id":"dad-3","joke":"Yogi did not wait for the slow one.","status":200}"""
                        else -> "{}"
                    }
                }
            ),
            yogiProviderTimeoutMs = 25
        )

        val quote = service.getRandomYogiQuote()

        assertEquals("Yogi did not wait for the slow one.", quote)
        assertEquals(
            listOf("https://api.adviceslip.com/advice", "https://icanhazdadjoke.com/"),
            requestedUrls
        )
    }

    @Test
    fun getRandomYogiQuote_usesLocalFallbackWhenAllRemoteProvidersFail() = runBlocking {
        val service = ApiService(testClient(status = HttpStatusCode.InternalServerError))

        val quote = service.getRandomYogiQuote()

        assertEquals("Even monkeys need a break sometimes. (API Error)", quote)
    }

    @Test
    fun getRandomYogiQuote_rethrowsCancellation() {
        val service = ApiService(cancelledClient())

        assertThrows(CancellationException::class.java) {
            runBlocking { service.getRandomYogiQuote() }
        }
    }

    private fun testClient(
        body: String = "{}",
        status: HttpStatusCode = HttpStatusCode.OK
    ): HttpClient {
        return testClient(status = status) { body }
    }

    private fun testClient(
        status: HttpStatusCode = HttpStatusCode.OK,
        bodyForUrl: (String) -> String
    ): HttpClient {
        return testClient(
            statusForUrl = { status },
            bodyForUrl = bodyForUrl
        )
    }

    private fun testClient(
        statusForUrl: (String) -> HttpStatusCode,
        bodyForUrl: suspend (String) -> String
    ): HttpClient {
        return testClient(
            statusForUrl = statusForUrl,
            bodyForUrl = { requestUrl, _ -> bodyForUrl(requestUrl) }
        )
    }

    private fun testClient(
        statusForUrl: (String) -> HttpStatusCode,
        bodyForUrl: suspend (String, String?) -> String
    ): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    val requestUrl = request.url.toString()
                    respond(
                        content = bodyForUrl(requestUrl, request.headers[HttpHeaders.Accept]),
                        status = statusForUrl(requestUrl),
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) {
                gson()
            }
        }
    }

    private fun cancelledClient(): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler {
                    throw CancellationException("request cancelled")
                }
            }
            install(ContentNegotiation) {
                gson()
            }
        }
    }
}
