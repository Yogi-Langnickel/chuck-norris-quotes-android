package com.yogi.chucknorris.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yogi.chucknorris.R
import com.yogi.chucknorris.viewmodel.QuoteViewModel
import com.yogi.chucknorris.ui.components.QuoteCard
import com.yogi.chucknorris.data.model.Quote
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import com.yogi.chucknorris.domain.BattlePeriod
import com.yogi.chucknorris.domain.BattleScore
import com.yogi.chucknorris.ui.components.BattleArena


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    quoteViewModel: QuoteViewModel = viewModel(),
    onCloseApp: () -> Unit
) {
    val quote: Quote? by quoteViewModel.quote.observeAsState()
    val battleRound by quoteViewModel.battleRound.observeAsState()
    val selectedPeriod by quoteViewModel.selectedPeriod.observeAsState(BattlePeriod.DAILY)
    val battleScores by quoteViewModel.battleScores.observeAsState(emptyMap<BattlePeriod, BattleScore>())
    val isBattleLoading by quoteViewModel.isBattleLoading.observeAsState(false)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.quote_copied)
    val shareChooserTitle = stringResource(R.string.share_chooser_title)

    LaunchedEffect(Unit) {
        quoteViewModel.fetchBattleRound()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name))
                        Text(
                            text = stringResource(R.string.app_tagline),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onCloseApp() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_app)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                BattleArena(
                    battleRound = battleRound,
                    selectedPeriod = selectedPeriod,
                    battleScores = battleScores,
                    isLoading = isBattleLoading,
                    onPeriodSelected = quoteViewModel::selectPeriod,
                    onBattleClick = quoteViewModel::fetchBattleRound
                )
                quote?.let { quoteData ->
                    QuoteCard(quote = quoteData)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { quoteViewModel.fetchRandomQuote() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.get_new_quote))
                            }
                            Button(
                                onClick = { quoteViewModel.fetchRandomCatFact() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.get_cat_fact))
                            }
                        }
                        FilledTonalIconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(quoteData.value))
                                scope.launch {
                                    snackbarHostState.showSnackbar(copiedMessage)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.copy_quote)
                            )
                        }
                        FilledTonalIconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, quoteData.value)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, shareChooserTitle)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.share_quote)
                            )
                        }
                    }
                } ?: CircularProgressIndicator()
            }
        }
    }
}
