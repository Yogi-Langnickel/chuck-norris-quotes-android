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

    private val _selectedPeriod = MutableLiveData(BattlePeriod.DAILY)
    val selectedPeriod: LiveData<BattlePeriod> get() = _selectedPeriod

    private val _isBattleLoading = MutableLiveData(false)
    val isBattleLoading: LiveData<Boolean> get() = _isBattleLoading

    private var activeQuoteRequest: Job? = null
    private var recordedBattleRound: BattleRound? = null

    fun fetchRandomQuote() {
        activeQuoteRequest?.cancel()
        activeQuoteRequest = viewModelScope.launch {
            _quoteUiState.value = QuoteUiState.Loading
            try {
                val fetchedQuote = quoteRepository.getRandomQuote()
                _quoteUiState.value = QuoteUiState.Success(fetchedQuote)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _quoteUiState.value = QuoteUiState.Error(QuoteRequest.CHUCK_QUOTE)
            }
        }
    }

    fun fetchRandomCatFact() {
        activeQuoteRequest?.cancel()
        activeQuoteRequest = viewModelScope.launch {
            _quoteUiState.value = QuoteUiState.Loading
            try {
                val fetchedQuote = quoteRepository.getRandomCatFact()
                _quoteUiState.value = QuoteUiState.Success(fetchedQuote)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _quoteUiState.value = QuoteUiState.Error(QuoteRequest.CAT_FACT)
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
                recordedBattleRound = null
                val featuredQuote = if (round.chuck.powerProfile.score >= round.cat.powerProfile.score) {
                    round.chuck.quote
                } else {
                    round.cat.quote
                }
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
        _quoteUiState.value = QuoteUiState.Success(
            when (winner) {
                BattleWinner.CHUCK -> round.chuck.quote
                BattleWinner.CAT -> round.cat.quote
                BattleWinner.DRAW -> round.chuck.quote
            }
        )
        _battleScores.value = battleScoreStore.recordBattle(winner)
    }

    fun continueBattleWithSelectedWinner() {
        val currentRound = _battleRound.value ?: return
        val selectedWinner = _selectedBattleWinner.value ?: return
        if (selectedWinner == BattleWinner.DRAW) return

        activeQuoteRequest?.cancel()
        activeQuoteRequest = viewModelScope.launch {
            _isBattleLoading.value = true
            try {
                val nextRound = when (selectedWinner) {
                    BattleWinner.CHUCK -> BattleRound.from(
                        chuckQuote = currentRound.chuck.quote,
                        catFact = quoteRepository.getRandomCatFact()
                    )
                    BattleWinner.CAT -> BattleRound.from(
                        chuckQuote = quoteRepository.getRandomQuote(),
                        catFact = currentRound.cat.quote
                    )
                    BattleWinner.DRAW -> currentRound
                }
                _battleRound.value = nextRound
                _selectedBattleWinner.value = null
                recordedBattleRound = null
                _quoteUiState.value = QuoteUiState.Success(
                    when (selectedWinner) {
                        BattleWinner.CHUCK -> nextRound.cat.quote
                        BattleWinner.CAT -> nextRound.chuck.quote
                        BattleWinner.DRAW -> nextRound.chuck.quote
                    }
                )
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
