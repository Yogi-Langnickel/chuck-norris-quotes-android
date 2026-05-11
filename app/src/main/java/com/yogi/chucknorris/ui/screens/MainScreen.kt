package com.yogi.chucknorris.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import com.yogi.chucknorris.viewmodel.QuoteRequest
import com.yogi.chucknorris.viewmodel.QuoteUiState
import com.yogi.chucknorris.viewmodel.QuoteViewModel
import com.yogi.chucknorris.ui.components.QuoteCard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import com.yogi.chucknorris.domain.BattlePeriod
import com.yogi.chucknorris.domain.BattleScore
import com.yogi.chucknorris.ui.components.BattleArena

private enum class AppTab {
    BATTLE,
    CHUCK,
    CAT,
    DOG
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    quoteViewModel: QuoteViewModel = viewModel()
) {
    val quoteUiState by quoteViewModel.quoteUiState.observeAsState(QuoteUiState.Loading)
    val battleRound by quoteViewModel.battleRound.observeAsState()
    val selectedWinner by quoteViewModel.selectedBattleWinner.observeAsState()
    val selectedPeriod by quoteViewModel.selectedPeriod.observeAsState(BattlePeriod.DAILY)
    val battleScores by quoteViewModel.battleScores.observeAsState(emptyMap<BattlePeriod, BattleScore>())
    val isBattleLoading by quoteViewModel.isBattleLoading.observeAsState(false)
    val isQuoteLoading = quoteUiState is QuoteUiState.Loading
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(AppTab.BATTLE) }
    val copiedMessage = stringResource(R.string.quote_copied)
    val shareUnavailableMessage = stringResource(R.string.share_unavailable)
    val shareChooserTitle = stringResource(R.string.share_chooser_title)
    val updateUnavailableMessage = stringResource(R.string.update_unavailable)
    val latestReleaseUrl = stringResource(R.string.latest_release_url)

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            AppTab.BATTLE -> if (battleRound == null) quoteViewModel.fetchBattleRound()
            AppTab.CHUCK -> quoteViewModel.fetchRandomQuote()
            AppTab.CAT -> quoteViewModel.fetchRandomCatFact()
            AppTab.DOG -> quoteViewModel.fetchRandomDogFact()
        }
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
                    IconButton(
                        onClick = {
                            openLatestRelease(
                                context = context,
                                latestReleaseUrl = latestReleaseUrl,
                                onUnavailable = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(updateUnavailableMessage)
                                    }
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = stringResource(R.string.open_latest_release)
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
                PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                    AppTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    when (tab) {
                                        AppTab.BATTLE -> stringResource(R.string.tab_battle_mode)
                                        AppTab.CHUCK -> stringResource(R.string.tab_chuck_facts)
                                        AppTab.CAT -> stringResource(R.string.tab_cat_facts)
                                        AppTab.DOG -> stringResource(R.string.tab_dog_facts)
                                    }
                                )
                            }
                        )
                    }
                }

                when (selectedTab) {
                    AppTab.BATTLE -> {
                        BattleArena(
                            battleRound = battleRound,
                            selectedWinner = selectedWinner,
                            selectedPeriod = selectedPeriod,
                            battleScores = battleScores,
                            isLoading = isBattleLoading,
                            onPeriodSelected = quoteViewModel::selectPeriod,
                            onWinnerSelected = quoteViewModel::chooseBattleWinner,
                            onLoserSwipedAway = quoteViewModel::continueBattleWithSelectedWinner,
                            onRefreshBoth = quoteViewModel::fetchBattleRound
                        )
                        if (quoteUiState is QuoteUiState.Error &&
                            (quoteUiState as QuoteUiState.Error).request == QuoteRequest.BATTLE_ROUND
                        ) {
                            QuoteErrorCard(
                                request = QuoteRequest.BATTLE_ROUND,
                                onRetry = quoteViewModel::fetchBattleRound
                            )
                        }
                    }
                    AppTab.CHUCK -> FactTabContent(
                        quoteUiState = quoteUiState,
                        expectedSourceLabel = "Chuck Norris",
                        isQuoteLoading = isQuoteLoading,
                        loadingLabel = stringResource(R.string.loading_chuck),
                        onRefresh = quoteViewModel::fetchRandomQuote,
                        onRetry = { quoteViewModel.retryQuoteLoad(QuoteRequest.CHUCK_QUOTE) },
                        onCopy = { quoteData ->
                            clipboardManager.setText(AnnotatedString(quoteData.value))
                            scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                        },
                        onShare = { quoteData ->
                            shareQuote(
                                quote = quoteData.value,
                                chooserTitle = shareChooserTitle,
                                context = context,
                                onShareUnavailable = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(shareUnavailableMessage)
                                    }
                                }
                            )
                        }
                    )
                    AppTab.CAT -> FactTabContent(
                        quoteUiState = quoteUiState,
                        expectedSourceLabel = "Cat Fact",
                        isQuoteLoading = isQuoteLoading,
                        loadingLabel = stringResource(R.string.loading_cat),
                        onRefresh = quoteViewModel::fetchRandomCatFact,
                        onRetry = { quoteViewModel.retryQuoteLoad(QuoteRequest.CAT_FACT) },
                        onCopy = { quoteData ->
                            clipboardManager.setText(AnnotatedString(quoteData.value))
                            scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                        },
                        onShare = { quoteData ->
                            shareQuote(
                                quote = quoteData.value,
                                chooserTitle = shareChooserTitle,
                                context = context,
                                onShareUnavailable = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(shareUnavailableMessage)
                                    }
                                }
                            )
                        }
                    )
                    AppTab.DOG -> FactTabContent(
                        quoteUiState = quoteUiState,
                        expectedSourceLabel = "Dog Fact",
                        isQuoteLoading = isQuoteLoading,
                        loadingLabel = stringResource(R.string.loading_dog),
                        onRefresh = quoteViewModel::fetchRandomDogFact,
                        onRetry = { quoteViewModel.retryQuoteLoad(QuoteRequest.DOG_FACT) },
                        onCopy = { quoteData ->
                            clipboardManager.setText(AnnotatedString(quoteData.value))
                            scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                        },
                        onShare = { quoteData ->
                            shareQuote(
                                quote = quoteData.value,
                                chooserTitle = shareChooserTitle,
                                context = context,
                                onShareUnavailable = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(shareUnavailableMessage)
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FactTabContent(
    quoteUiState: QuoteUiState,
    expectedSourceLabel: String,
    isQuoteLoading: Boolean,
    loadingLabel: String,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onCopy: (com.yogi.chucknorris.data.model.Quote) -> Unit,
    onShare: (com.yogi.chucknorris.data.model.Quote) -> Unit
) {
    val currentQuote = (quoteUiState as? QuoteUiState.Success)
        ?.quote
        ?.takeIf { it.sourceLabel == expectedSourceLabel }

    when {
        isQuoteLoading ||
            quoteUiState is QuoteUiState.Loading ||
            (currentQuote == null && quoteUiState !is QuoteUiState.Error) -> {
            LoadingState(label = loadingLabel)
        }
        quoteUiState is QuoteUiState.Error -> {
            QuoteErrorCard(request = quoteUiState.request, onRetry = onRetry)
        }
        currentQuote != null -> {
            QuoteCard(quote = currentQuote)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRefresh,
                    enabled = !isQuoteLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.refresh_fact))
                }
                FilledTonalIconButton(onClick = { onCopy(currentQuote) }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.copy_quote)
                    )
                }
                FilledTonalIconButton(onClick = { onShare(currentQuote) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share_quote)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(label: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun shareQuote(
    quote: String,
    chooserTitle: String,
    context: android.content.Context,
    onShareUnavailable: () -> Unit
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, quote)
    }
    try {
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    } catch (e: ActivityNotFoundException) {
        onShareUnavailable()
    }
}

private fun openLatestRelease(
    context: android.content.Context,
    latestReleaseUrl: String,
    onUnavailable: () -> Unit
) {
    val updateIntent = Intent(Intent.ACTION_VIEW, Uri.parse(latestReleaseUrl))
    try {
        context.startActivity(updateIntent)
    } catch (e: ActivityNotFoundException) {
        onUnavailable()
    }
}

@Composable
private fun QuoteErrorCard(
    request: QuoteRequest,
    onRetry: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (request) {
                    QuoteRequest.CHUCK_QUOTE -> stringResource(R.string.quote_error_chuck)
                    QuoteRequest.CAT_FACT -> stringResource(R.string.quote_error_cat)
                    QuoteRequest.DOG_FACT -> stringResource(R.string.quote_error_dog)
                    QuoteRequest.BATTLE_ROUND -> stringResource(R.string.quote_error_battle)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.retry))
            }
        }
    }
}
