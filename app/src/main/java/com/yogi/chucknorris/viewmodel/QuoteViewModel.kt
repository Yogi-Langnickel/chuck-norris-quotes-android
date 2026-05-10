package com.yogi.chucknorris.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yogi.chucknorris.data.local.BattleScoreStore
import com.yogi.chucknorris.data.model.Quote
import com.yogi.chucknorris.data.repository.QuoteRepository
import com.yogi.chucknorris.domain.BattlePeriod
import com.yogi.chucknorris.domain.BattleRound
import com.yogi.chucknorris.domain.BattleScore
import kotlinx.coroutines.launch

class QuoteViewModel(
    private val quoteRepository: QuoteRepository,
    private val battleScoreStore: BattleScoreStore
) : ViewModel() {

    private val _quote = MutableLiveData<Quote>()
    val quote: LiveData<Quote> get() = _quote

    private val _battleRound = MutableLiveData<BattleRound>()
    val battleRound: LiveData<BattleRound> get() = _battleRound

    private val _battleScores = MutableLiveData(battleScoreStore.getScores())
    val battleScores: LiveData<Map<BattlePeriod, BattleScore>> get() = _battleScores

    private val _selectedPeriod = MutableLiveData(BattlePeriod.DAILY)
    val selectedPeriod: LiveData<BattlePeriod> get() = _selectedPeriod

    private val _isBattleLoading = MutableLiveData(false)
    val isBattleLoading: LiveData<Boolean> get() = _isBattleLoading

    fun fetchRandomQuote() {
        viewModelScope.launch {
            try {
                val fetchedQuote = quoteRepository.getRandomQuote()
                _quote.value = fetchedQuote
            } catch (e: Exception) {
                // Handle network errors here
            }
        }
    }

    fun fetchRandomCatFact() {
        viewModelScope.launch {
            try {
                val fetchedQuote = quoteRepository.getRandomCatFact()
                _quote.value = fetchedQuote
            } catch (e: Exception) {
                // Handle network errors here
            }
        }
    }

    fun fetchBattleRound() {
        viewModelScope.launch {
            _isBattleLoading.value = true
            try {
                val round = quoteRepository.getBattleRound()
                _battleRound.value = round
                _quote.value = if (round.chuck.powerProfile.score >= round.cat.powerProfile.score) {
                    round.chuck.quote
                } else {
                    round.cat.quote
                }
                _battleScores.value = battleScoreStore.recordBattle(round.winner)
            } catch (e: Exception) {
                // Handle network errors here
            } finally {
                _isBattleLoading.value = false
            }
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
