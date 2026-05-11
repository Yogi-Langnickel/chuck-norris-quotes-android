package com.yogi.chucknorris.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.yogi.chucknorris.data.local.BattleScoreStore
import com.yogi.chucknorris.data.model.Quote
import com.yogi.chucknorris.data.repository.QuoteDataSource
import com.yogi.chucknorris.domain.BattleContender
import com.yogi.chucknorris.domain.BattlePeriod
import com.yogi.chucknorris.domain.BattleRound
import com.yogi.chucknorris.domain.BattleScore
import com.yogi.chucknorris.domain.BattleWinner
import com.yogi.chucknorris.domain.FactSource
import com.yogi.chucknorris.domain.QuotePowerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialQuoteUiState_isLoading() {
        val viewModel = QuoteViewModel(FakeQuoteDataSource(), FakeBattleScoreStore())

        assertEquals(QuoteUiState.Loading, viewModel.quoteUiState.value)
    }

    @Test
    fun fetchRandomQuote_emitsLoadingThenSuccess() = runTest {
        val chuckQuote = Quote("chuck-1", "Chuck Norris can divide by zero.", "Chuck Norris")
        val viewModel = QuoteViewModel(
            FakeQuoteDataSource(quoteResult = Result.success(chuckQuote)),
            FakeBattleScoreStore()
        )

        viewModel.recordQuoteStates { states ->
            viewModel.fetchRandomQuote()
            advanceUntilIdle()

            assertEquals(
                listOf(QuoteUiState.Loading, QuoteUiState.Success(chuckQuote)),
                states
            )
        }
    }

    @Test
    fun fetchRandomQuote_emitsLoadingThenErrorWhenApiFails() = runTest {
        val viewModel = QuoteViewModel(
            FakeQuoteDataSource(quoteResult = Result.failure(RuntimeException("Chuck failed"))),
            FakeBattleScoreStore()
        )

        viewModel.recordQuoteStates { states ->
            viewModel.fetchRandomQuote()
            advanceUntilIdle()

            assertEquals(
                listOf(QuoteUiState.Loading, QuoteUiState.Error(QuoteRequest.CHUCK_QUOTE)),
                states
            )
        }
    }

    @Test
    fun fetchRandomCatFact_emitsLoadingThenSuccess() = runTest {
        val catFact = Quote("cat-1", "Cats have excellent night vision.", "Cat Fact")
        val viewModel = QuoteViewModel(
            FakeQuoteDataSource(catFactResult = Result.success(catFact)),
            FakeBattleScoreStore()
        )

        viewModel.recordQuoteStates { states ->
            viewModel.fetchRandomCatFact()
            advanceUntilIdle()

            assertEquals(
                listOf(QuoteUiState.Loading, QuoteUiState.Success(catFact)),
                states
            )
        }
    }

    @Test
    fun fetchRandomCatFact_emitsLoadingThenErrorWhenApiFails() = runTest {
        val viewModel = QuoteViewModel(
            FakeQuoteDataSource(catFactResult = Result.failure(RuntimeException("Cat failed"))),
            FakeBattleScoreStore()
        )

        viewModel.recordQuoteStates { states ->
            viewModel.fetchRandomCatFact()
            advanceUntilIdle()

            assertEquals(
                listOf(QuoteUiState.Loading, QuoteUiState.Error(QuoteRequest.CAT_FACT)),
                states
            )
        }
    }

    @Test
    fun fetchRandomDogFact_emitsLoadingThenSuccess() = runTest {
        val dogFact = Quote("dog-1", "Dogs can understand human pointing gestures.", "Dog Fact")
        val viewModel = QuoteViewModel(
            FakeQuoteDataSource(dogFactResult = Result.success(dogFact)),
            FakeBattleScoreStore()
        )

        viewModel.recordQuoteStates { states ->
            viewModel.fetchRandomDogFact()
            advanceUntilIdle()

            assertEquals(
                listOf(QuoteUiState.Loading, QuoteUiState.Success(dogFact)),
                states
            )
        }
    }

    @Test
    fun fetchRandomDogFact_emitsLoadingThenErrorWhenApiFails() = runTest {
        val viewModel = QuoteViewModel(
            FakeQuoteDataSource(dogFactResult = Result.failure(RuntimeException("Dog failed"))),
            FakeBattleScoreStore()
        )

        viewModel.recordQuoteStates { states ->
            viewModel.fetchRandomDogFact()
            advanceUntilIdle()

            assertEquals(
                listOf(QuoteUiState.Loading, QuoteUiState.Error(QuoteRequest.DOG_FACT)),
                states
            )
        }
    }

    @Test
    fun fetchBattleRound_featuresPowerLeaderWithoutRecordingScore() = runTest {
        val chuckQuote = Quote("chuck-1", "Chuck Norris can slam a revolving door.", "Chuck Norris")
        val catFact = Quote("cat-1", "Cats sleep for many hours each day.", "Cat Fact")
        val round = BattleRound.from(chuckQuote, catFact)
        val scoreStore = FakeBattleScoreStore()
        val viewModel = QuoteViewModel(
            FakeQuoteDataSource(battleRoundResult = Result.success(round)),
            scoreStore
        )

        viewModel.fetchBattleRound()
        advanceUntilIdle()

        val expectedFeaturedQuote = if (round.chuck.powerProfile.score >= round.cat.powerProfile.score) {
            round.chuck.quote
        } else {
            round.cat.quote
        }
        assertEquals(QuoteUiState.Success(expectedFeaturedQuote), viewModel.quoteUiState.value)
        assertEquals(round, viewModel.battleRound.value)
        assertEquals(null, viewModel.selectedBattleWinner.value)
        assertEquals(emptyList<BattleWinner>(), scoreStore.recordedWinners)
    }

    @Test
    fun chooseBattleWinner_recordsSelectedWinnerOnce() = runTest {
        val round = BattleRound.from(
            Quote("chuck-1", "Chuck Norris can slam a revolving door.", "Chuck Norris"),
            Quote("cat-1", "Cats sleep for many hours each day.", "Cat Fact")
        )
        val scoreStore = FakeBattleScoreStore()
        val viewModel = QuoteViewModel(
            FakeQuoteDataSource(battleRoundResult = Result.success(round)),
            scoreStore
        )

        viewModel.fetchBattleRound()
        advanceUntilIdle()
        viewModel.chooseBattleWinner(BattleWinner.CAT)
        viewModel.chooseBattleWinner(BattleWinner.CHUCK)

        assertEquals(BattleWinner.CAT, viewModel.selectedBattleWinner.value)
        assertEquals(QuoteUiState.Success(round.cat.quote), viewModel.quoteUiState.value)
        assertEquals(listOf(BattleWinner.CAT), scoreStore.recordedWinners)
        assertEquals(1, viewModel.battleScores.value?.get(BattlePeriod.DAILY)?.catWins)
        assertEquals(FactSource.CAT, viewModel.battleStreak.value?.champion)
        assertEquals(1, viewModel.battleStreak.value?.wins)
    }

    @Test
    fun championStreakIncrementsAcrossContinuedBattles() = runTest {
        val firstRound = BattleRound.from(
            Quote("chuck-1", "Chuck Norris can divide by zero.", "Chuck Norris"),
            Quote("cat-1", "Cats can rotate their ears.", "Cat Fact")
        )
        val nextCatFact = Quote("cat-2", "Cats have excellent night vision.", "Cat Fact")
        val viewModel = QuoteViewModel(
            FakeQuoteDataSource(
                catFactResult = Result.success(nextCatFact),
                battleRoundResult = Result.success(firstRound)
            ),
            FakeBattleScoreStore()
        )

        viewModel.fetchBattleRound()
        advanceUntilIdle()
        viewModel.chooseBattleWinner(BattleWinner.CHUCK)
        viewModel.continueBattleWithSelectedWinner()
        advanceUntilIdle()
        viewModel.chooseBattleWinner(BattleWinner.CHUCK)

        assertEquals(FactSource.CHUCK, viewModel.battleStreak.value?.champion)
        assertEquals(2, viewModel.battleStreak.value?.wins)
    }

    @Test
    fun chooseBattleWinner_afterNewRoundRecordsAgain() = runTest {
        val firstRound = BattleRound.from(
            Quote("chuck-1", "Chuck Norris can divide by zero.", "Chuck Norris"),
            Quote("cat-1", "Cats can rotate their ears.", "Cat Fact")
        )
        val secondRound = BattleRound.from(
            Quote("chuck-2", "Chuck Norris counted to infinity. Twice.", "Chuck Norris"),
            Quote("cat-2", "Cats sleep for many hours each day.", "Cat Fact")
        )
        val dataSource = FakeQuoteDataSource(battleRoundResult = Result.success(firstRound))
        val scoreStore = FakeBattleScoreStore()
        val viewModel = QuoteViewModel(dataSource, scoreStore)

        viewModel.fetchBattleRound()
        advanceUntilIdle()
        viewModel.chooseBattleWinner(BattleWinner.CHUCK)
        dataSource.battleRoundResult = Result.success(secondRound)
        viewModel.fetchBattleRound()
        advanceUntilIdle()
        viewModel.chooseBattleWinner(BattleWinner.CAT)

        assertEquals(listOf(BattleWinner.CHUCK, BattleWinner.CAT), scoreStore.recordedWinners)
        assertEquals(BattleWinner.CAT, viewModel.selectedBattleWinner.value)
        assertEquals(FactSource.CAT, viewModel.battleStreak.value?.champion)
        assertEquals(1, viewModel.battleStreak.value?.wins)
    }

    @Test
    fun continueBattleWithSelectedWinner_refreshesOnlyLoserSide() = runTest {
        val firstRound = BattleRound.from(
            Quote("chuck-1", "Chuck Norris can divide by zero.", "Chuck Norris"),
            Quote("cat-1", "Cats can rotate their ears.", "Cat Fact")
        )
        val nextCatFact = Quote("cat-2", "Cats have excellent night vision.", "Cat Fact")
        val dataSource = FakeQuoteDataSource(
            catFactResult = Result.success(nextCatFact),
            battleRoundResult = Result.success(firstRound)
        )
        val viewModel = QuoteViewModel(dataSource, FakeBattleScoreStore())

        viewModel.fetchBattleRound()
        advanceUntilIdle()
        viewModel.chooseBattleWinner(BattleWinner.CHUCK)
        viewModel.continueBattleWithSelectedWinner()
        advanceUntilIdle()

        val nextRound = viewModel.battleRound.value
        assertEquals(firstRound.chuck.quote, nextRound?.chuck?.quote)
        assertEquals(nextCatFact, nextRound?.cat?.quote)
        assertEquals(null, viewModel.selectedBattleWinner.value)
    }

    @Test
    fun newerQuoteRequestWinsWhenPreviousRequestIsStillRunning() = runTest {
        val slowQuote = CompletableDeferred<Quote>()
        val catFact = Quote("cat-1", "Cats have excellent night vision.", "Cat Fact")
        val viewModel = QuoteViewModel(
            SuspendedQuoteDataSource(
                quote = slowQuote,
                catFact = CompletableDeferred(catFact)
            ),
            FakeBattleScoreStore()
        )

        viewModel.fetchRandomQuote()
        advanceUntilIdle()
        viewModel.fetchRandomCatFact()
        advanceUntilIdle()
        slowQuote.complete(Quote("chuck-1", "Late Chuck quote.", "Chuck Norris"))
        advanceUntilIdle()

        assertEquals(QuoteUiState.Success(catFact), viewModel.quoteUiState.value)
    }

    private suspend fun QuoteViewModel.recordQuoteStates(
        block: suspend (MutableList<QuoteUiState>) -> Unit
    ) {
        val states = mutableListOf<QuoteUiState>()
        val observer = Observer<QuoteUiState> { states.add(it) }
        quoteUiState.observeForever(observer)
        states.clear()
        try {
            block(states)
        } finally {
            quoteUiState.removeObserver(observer)
        }
    }

    private class FakeQuoteDataSource(
        var quoteResult: Result<Quote> = Result.success(
            Quote("chuck-default", "Chuck Norris counted to infinity. Twice.", "Chuck Norris")
        ),
        var catFactResult: Result<Quote> = Result.success(
            Quote("cat-default", "Cats can rotate their ears.", "Cat Fact")
        ),
        var dogFactResult: Result<Quote> = Result.success(
            Quote("dog-default", "Dogs have a strong sense of smell.", "Dog Fact")
        ),
        var battleRoundResult: Result<BattleRound> = Result.success(
            BattleRound.from(
                Quote("chuck-battle", "Chuck Norris can divide by zero.", "Chuck Norris"),
                Quote("cat-battle", "Cats have excellent night vision.", "Cat Fact")
            )
        )
    ) : QuoteDataSource {
        override suspend fun getRandomQuote(): Quote = quoteResult.getOrThrow()
        override suspend fun getRandomCatFact(): Quote = catFactResult.getOrThrow()
        override suspend fun getRandomDogFact(): Quote = dogFactResult.getOrThrow()
        override suspend fun getBattleRound(): BattleRound = battleRoundResult.getOrThrow()
        override suspend fun getBattleChallenger(excludedSources: Set<FactSource>): BattleContender {
            val source = FactSource.entries.first { it !in excludedSources }
            val quote = when (source) {
                FactSource.CHUCK -> quoteResult.getOrThrow()
                FactSource.CAT -> catFactResult.getOrThrow()
                FactSource.DOG -> dogFactResult.getOrThrow()
            }
            return BattleContender(source, quote, QuotePowerProfile.from(quote.value))
        }
    }

    private class SuspendedQuoteDataSource(
        private val quote: CompletableDeferred<Quote> = CompletableDeferred(
            Quote("chuck-default", "Chuck Norris counted to infinity. Twice.", "Chuck Norris")
        ),
        private val catFact: CompletableDeferred<Quote> = CompletableDeferred(
            Quote("cat-default", "Cats can rotate their ears.", "Cat Fact")
        ),
        private val dogFact: CompletableDeferred<Quote> = CompletableDeferred(
            Quote("dog-default", "Dogs have a strong sense of smell.", "Dog Fact")
        ),
        private val battleRound: CompletableDeferred<BattleRound> = CompletableDeferred(
            BattleRound.from(
                Quote("chuck-battle", "Chuck Norris can divide by zero.", "Chuck Norris"),
                Quote("cat-battle", "Cats have excellent night vision.", "Cat Fact")
            )
        )
    ) : QuoteDataSource {
        override suspend fun getRandomQuote(): Quote = quote.await()
        override suspend fun getRandomCatFact(): Quote = catFact.await()
        override suspend fun getRandomDogFact(): Quote = dogFact.await()
        override suspend fun getBattleRound(): BattleRound = battleRound.await()
        override suspend fun getBattleChallenger(excludedSources: Set<FactSource>): BattleContender {
            val source = FactSource.entries.first { it !in excludedSources }
            val quote = when (source) {
                FactSource.CHUCK -> quote.await()
                FactSource.CAT -> catFact.await()
                FactSource.DOG -> dogFact.await()
            }
            return BattleContender(source, quote, QuotePowerProfile.from(quote.value))
        }
    }

    private class FakeBattleScoreStore : BattleScoreStore {
        val recordedWinners = mutableListOf<BattleWinner>()
        private var scores = BattlePeriod.entries.associateWith { BattleScore() }

        override fun getScore(period: BattlePeriod): BattleScore = scores.getValue(period)

        override fun getScores(): Map<BattlePeriod, BattleScore> {
            return scores
        }

        override fun recordBattle(winner: BattleWinner): Map<BattlePeriod, BattleScore> {
            recordedWinners += winner
            scores = BattlePeriod.entries.associateWith { period ->
                scores.getValue(period).record(winner)
            }
            return scores
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
