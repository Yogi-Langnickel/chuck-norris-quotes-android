package com.yogi.chucknorris.data.service

interface FactService {
    suspend fun getRandomJoke(): String
    suspend fun getRandomCatFact(): String
    suspend fun getRandomDogFact(): String
}
