package com.yogi.chucknorris.data.service

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
import kotlinx.coroutines.runBlocking
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
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = bodyForUrl(request.url.toString()),
                        status = status,
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
