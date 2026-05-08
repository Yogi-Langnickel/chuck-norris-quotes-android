package com.yogi.chucknorris.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yogi.chucknorris.data.model.Quote
import com.yogi.chucknorris.data.repository.QuoteRepository
import kotlinx.coroutines.launch

class QuoteViewModel(private val quoteRepository: QuoteRepository) : ViewModel() {

    private val _quote = MutableLiveData<Quote>()
    val quote: LiveData<Quote> get() = _quote

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

    // Add this Factory block
    companion object {
        fun provideFactory(repository: QuoteRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuoteViewModel(repository) as T
            }
        }
    }
}