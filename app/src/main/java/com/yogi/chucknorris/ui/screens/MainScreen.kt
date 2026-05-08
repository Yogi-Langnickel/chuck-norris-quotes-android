package com.yogi.chucknorris.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yogi.chucknorris.viewmodel.QuoteViewModel
import com.yogi.chucknorris.ui.components.QuoteCard
import com.yogi.chucknorris.data.model.Quote
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    quoteViewModel: QuoteViewModel = viewModel(),
    onCloseApp: () -> Unit // Add this parameter
) {
    val quote: Quote? by quoteViewModel.quote.observeAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        quoteViewModel.fetchRandomQuote()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Chuck Norris Quotes") },
                actions = {
                    IconButton(onClick = { onCloseApp() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                quote?.let { quoteData ->
                    QuoteCard(quote = quoteData)
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(quoteData.value))
                            scope.launch {
                                snackbarHostState.showSnackbar("Quote copied to clipboard!")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Quote")
                    }
                } ?: CircularProgressIndicator()
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { quoteViewModel.fetchRandomQuote() },
                    modifier = Modifier.fillMaxWidth(0.7f)) {
                    Text("Get New Quote")
                }
            }
        }
    }
}