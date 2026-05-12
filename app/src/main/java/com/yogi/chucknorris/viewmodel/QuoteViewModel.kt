package com.yogi.chucknorris.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yogi.chucknorris.data.local.BattleScoreStore
import com.yogi.chucknorris.data.model.Quote
import com.yogi.chucknorris.data.repository.QuoteDataSource
import com.yogi.chucknorris.data.repository.QuoteRepository
import com.yogi.chucknorris.domain.BattlePeriod
import com.yogi.chucknorris.domain.BattleRound
import com.yogi.chucknorris.domain.BattleScore
import com.yogi.chucknorris.domain.BattleStreak
import com.yogi.chucknorris.domain.BattleWinner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface QuoteUiState {
    data object Loading : QuoteUiState
    data class Success(val quote: Quote) : QuoteUiState
    data class Error(val request: QuoteRequest) : QuoteUiState
}

enum class QuoteRequest {
    CHUCK_QUOTE,
    CAT_FACT,
    DOG_FACT,
    BATTLE_ROUND
}

class QuoteViewModel(
    private val quoteRepository: QuoteDataSource,
    private val battleScoreStore: BattleScoreStore
) : ViewModel() {

    private val _quoteUiState = MutableLiveData<QuoteUiState>(QuoteUiState.Loading)
    val quoteUiState: LiveData<QuoteUiState> get() = _quoteUiState

    private val _battleRound = MutableLiveData<BattleRound>()
    val battleRound: LiveData<BattleRound> get() = _battleRound

    private val _selectedBattleWinner = MutableLiveData<BattleWinner?>()
    val selectedBattleWinner: LiveData<BattleWinner?> get() = _selectedBattleWinner

    private val _battleScores = MutableLiveData(battleScoreStore.getScores())
    val battleScores: LiveData<Map<BattlePeriod, BattleScore>> get() = _battleScores

    private val _battleStreak = MutableLiveData(BattleStreak())
    val battleStreak: LiveData<BattleStreak> get() = _battleStreak

    private val _selectedPeriod = MutableLiveData(BattlePeriod.DAILY)
    val selectedPeriod: LiveData<BattlePeriod> get() = _selectedPeriod

    private val _isBattleLoading = MutableLiveData(false)
    val isBattleLoading: LiveData<Boolean> get() = _isBattleLoading

    private var activeQuoteRequest: Job? = null
    private var recordedBattleRound: BattleRound? = null
    private val standaloneQuoteStates = mutableMapOf<QuoteRequest, QuoteUiState>()

    fun fetchRandomQuote() {
        fetchStandaloneQuote(QuoteRequest.CHUCK_QUOTE) { quoteRepository.getRandomQuote() }
    }

    fun showOrFetchRandomQuote() {
        showCachedStandaloneQuote(QuoteRequest.CHUCK_QUOTE, ::fetchRandomQuote)
    }

    fun fetchRandomCatFact() {
        fetchStandaloneQuote(QuoteRequest.CAT_FACT) { quoteRepository.getRandomCatFact() }
    }

    fun showOrFetchRandomCatFact() {
        showCachedStandaloneQuote(QuoteRequest.CAT_FACT, ::fetchRandomCatFact)
    }

    fun fetchRandomDogFact() {
        fetchStandaloneQuote(QuoteRequest.DOG_FACT) { quoteRepository.getRandomDogFact() }
    }

    fun showOrFetchRandomDogFact() {
        showCachedStandaloneQuote(QuoteRequest.DOG_FACT, ::fetchRandomDogFact)
    }

    private fun showCachedStandaloneQuote(
        request: QuoteRequest,
        fetch: () -> Unit
    ) {
        val cachedState = standaloneQuoteStates[request]
        if (cachedState == null) {
            fetch()
        } else {
            activeQuoteRequest?.cancel()
            _quoteUiState.value = cachedState
        }
    }

    private fun fetchStandaloneQuote(
        request: QuoteRequest,
        fetch: suspend () -> Quote
    ) {
        activeQuoteRequest?.cancel()
        activeQuoteRequest = viewModelScope.launch {
            _quoteUiState.value = QuoteUiState.Loading
            try {
                val nextState = QuoteUiState.Success(fetch())
                standaloneQuoteStates[request] = nextState
                _quoteUiState.value = nextState
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val nextState = QuoteUiState.Error(request)
                standaloneQuoteStates[request] = nextState
                _quoteUiState.value = nextState
            }
        }
    }

    fun fetchBattleRound() {
        activeQuoteRequest?.cancel()
        activeQuoteRequest = viewModelScope.launch {
            _isBattleLoading.value = true
            _quoteUiState.value = QuoteUiState.Loading
            try {
                val round = quoteRepository.getBattleRound()
                _battleRound.value = round
                _selectedBattleWinner.value = null
                _battleStreak.value = BattleStreak()
                recordedBattleRound = null
                val featuredQuote = round.contenders
                    .maxBy { it.powerProfile.score }
                    .quote
                _quoteUiState.value = QuoteUiState.Success(featuredQuote)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _quoteUiState.value = QuoteUiState.Error(QuoteRequest.BATTLE_ROUND)
            } finally {
                _isBattleLoading.value = false
            }
        }
    }

    fun chooseBattleWinner(winner: BattleWinner) {
        if (winner == BattleWinner.DRAW) return
        val round = _battleRound.value ?: return
        if (recordedBattleRound == round) return

        recordedBattleRound = round
        _selectedBattleWinner.value = winner
        round.contenderFor(winner)?.let { winningContender ->
            _battleStreak.value = (_battleStreak.value ?: BattleStreak()).record(winningContender.source)
            _quoteUiState.value = QuoteUiState.Success(winningContender.quote)
        }
        _battleScores.value = battleScoreStore.recordBattle(winner)
    }

    fun continueBattleWithSelectedWinner() {
        val selectedWinner = _selectedBattleWinner.value ?: return
        if (selectedWinner == BattleWinner.DRAW) return

        activeQuoteRequest?.cancel()
        activeQuoteRequest = viewModelScope.launch {
            _isBattleLoading.value = true
            try {
                val nextRound = quoteRepository.getBattleRound()
                _battleRound.value = nextRound
                _selectedBattleWinner.value = null
                recordedBattleRound = null
                val featuredQuote = nextRound.contenders
                    .maxBy { it.powerProfile.score }
                    .quote
                _quoteUiState.value = QuoteUiState.Success(featuredQuote)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _quoteUiState.value = QuoteUiState.Error(QuoteRequest.BATTLE_ROUND)
            } finally {
                _isBattleLoading.value = false
            }
        }
    }

    fun retryQuoteLoad(request: QuoteRequest) {
        when (request) {
            QuoteRequest.CHUCK_QUOTE -> fetchRandomQuote()
            QuoteRequest.CAT_FACT -> fetchRandomCatFact()
            QuoteRequest.DOG_FACT -> fetchRandomDogFact()
            QuoteRequest.BATTLE_ROUND -> fetchBattleRound()
        }
    }

    fun selectPeriod(period: BattlePeriod) {
        _selectedPeriod.value = period
    }

    // Add this Factory block
    companion object {
        fun provideFactory(
            repository: QuoteRepository,
            battleScoreStore: BattleScoreStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuoteViewModel(repository, battleScoreStore) as T
            }
        }
    }
}
