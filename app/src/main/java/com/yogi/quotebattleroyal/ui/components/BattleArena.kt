package com.yogi.quotebattleroyal.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yogi.quotebattleroyal.R
import com.yogi.quotebattleroyal.domain.BattleContender
import com.yogi.quotebattleroyal.domain.BattlePeriod
import com.yogi.quotebattleroyal.domain.BattleRound
import com.yogi.quotebattleroyal.domain.BattleScore
import com.yogi.quotebattleroyal.domain.BattleStreak
import com.yogi.quotebattleroyal.domain.BattleWinner
import com.yogi.quotebattleroyal.domain.FactSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ProposalPanel = Color(0xFF1B1F22)
private val ProposalPhone = Color(0xFF111516)
private val ProposalTile = Color(0xFF20272B)
private val ProposalLine = Color(0xFF333B40)
private val ProposalPhoneLine = Color(0xFF3B4449)
private val ProposalTileLine = Color(0xFF354047)
private val ProposalAvatar = Color(0xFF2B3338)
private val ProposalAvatarLine = Color(0xFF465158)
private val ProposalText = Color(0xFFF2F4F3)
private val ProposalMuted = Color(0xFFA9B2AD)
private val ProposalYogi = Color(0xFFD7A6FF)
private val ProposalWin = Color(0xFF8FF0B0)
private val ProposalPodiumStart = Color(0xFF3D3549)
private val ProposalPodiumEnd = Color(0xFF202F29)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BattleArena(
    battleRound: BattleRound?,
    selectedWinner: BattleWinner?,
    selectedPeriod: BattlePeriod,
    battleScores: Map<BattlePeriod, BattleScore>,
    battleStreak: BattleStreak,
    isLoading: Boolean,
    onPeriodSelected: (BattlePeriod) -> Unit,
    onWinnerSelected: (BattleWinner) -> Unit,
    onLoserSwipedAway: () -> Unit,
    onRefreshBoth: () -> Unit
) {
    val score = battleScores[selectedPeriod] ?: BattleScore()
    var loserExitDirection by remember { mutableFloatStateOf(1f) }
    var firstEntryDirection by remember { mutableStateOf<Float?>(null) }
    var secondEntryDirection by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(battleRound?.first?.quote?.id, battleRound?.second?.quote?.id, selectedWinner) {
        if (selectedWinner != null && selectedWinner != BattleWinner.DRAW) {
            delay(620)
            onLoserSwipedAway()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BattlePeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodSelected(period) },
                    label = { Text(period.label()) }
                )
            }
        }

        ScoreStrip(period = selectedPeriod, score = score, streak = battleStreak)
        Text(
            text = stringResource(R.string.battle_selection_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (battleRound == null) {
            if (isLoading) {
                BattleLoadingState()
            } else {
                EmptyBattleState()
            }
        } else {
            BattleContenderCard(
                contender = battleRound.first,
                isWinner = selectedWinner == battleRound.first.source.winner,
                isLoser = selectedWinner != null &&
                    selectedWinner != BattleWinner.DRAW &&
                    selectedWinner != battleRound.first.source.winner,
                loserExitDirection = loserExitDirection,
                entryDirection = firstEntryDirection,
                canSelect = selectedWinner == null && !isLoading,
                onSelected = {
                    loserExitDirection = 1f
                    secondEntryDirection = -1f
                    onWinnerSelected(battleRound.first.source.winner)
                },
                onSwipedAway = { direction ->
                    loserExitDirection = direction
                    firstEntryDirection = -direction
                    onWinnerSelected(battleRound.second.source.winner)
                }
            )
            BattleContenderCard(
                contender = battleRound.second,
                isWinner = selectedWinner == battleRound.second.source.winner,
                isLoser = selectedWinner != null &&
                    selectedWinner != BattleWinner.DRAW &&
                    selectedWinner != battleRound.second.source.winner,
                loserExitDirection = loserExitDirection,
                entryDirection = secondEntryDirection,
                canSelect = selectedWinner == null && !isLoading,
                onSelected = {
                    loserExitDirection = -1f
                    firstEntryDirection = 1f
                    onWinnerSelected(battleRound.second.source.winner)
                },
                onSwipedAway = { direction ->
                    loserExitDirection = direction
                    secondEntryDirection = -direction
                    onWinnerSelected(battleRound.first.source.winner)
                }
            )
            TieBreakButton(
                isLoading = isLoading || selectedWinner != null,
                onRefreshBoth = onRefreshBoth
            )
        }
    }
}

@Composable
private fun ChampionStreakText(streak: BattleStreak) {
    val champion = streak.champion ?: return
    if (!streak.isActive) return

    Text(
        text = stringResource(
            R.string.champion_streak_inline,
            champion.sourceLabel,
            streak.wins
        ),
        modifier = Modifier.padding(start = 12.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun ScoreStrip(period: BattlePeriod, score: BattleScore, streak: BattleStreak) {
    val scoreEntries = remember(score) { score.toSourceScores() }
    val leaderEntry = scoreEntries.firstOrNull()
    val hasLeader = leaderEntry != null && score.leader != BattleWinner.DRAW && leaderEntry.wins > 0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ProposalPhoneLine),
        tonalElevation = 1.dp,
        color = ProposalPhone
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.scoreboard_current_leader),
                    style = MaterialTheme.typography.labelSmall,
                    color = ProposalMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.scoreboard_daily_standings, period.label()),
                    style = MaterialTheme.typography.labelSmall,
                    color = ProposalMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            ChampionPodiumLeader(
                leaderEntry = leaderEntry,
                hasLeader = hasLeader,
                leaderMargin = score.leaderMargin
            )

            MiniScoreGrid(
                entries = scoreEntries.filter { it.source != leaderEntry?.source || !hasLeader }
            )

            ChampionStreakText(streak = streak)
        }
    }
}

private data class SourceScore(
    val source: FactSource,
    val wins: Int
)

private fun BattleScore.toSourceScores(): List<SourceScore> {
    return listOf(
        SourceScore(FactSource.CHUCK, chuckWins),
        SourceScore(FactSource.CAT, catWins),
        SourceScore(FactSource.DOG, dogWins),
        SourceScore(FactSource.YOGI, yogiWins)
    ).sortedWith(
        compareByDescending<SourceScore> { it.wins }
            .thenBy { it.source.ordinal }
    )
}

@Composable
private fun ChampionPodiumLeader(
    leaderEntry: SourceScore?,
    hasLeader: Boolean,
    leaderMargin: Int
) {
    val source = leaderEntry?.source
    val title = if (hasLeader && source != null) {
        "${source.scoreLabel} ${stringResource(R.string.scoreboard_leads)}"
    } else if ((leaderEntry?.wins ?: 0) > 0) {
        stringResource(R.string.scoreboard_tied_leader)
    } else {
        stringResource(R.string.scoreboard_no_leader)
    }
    val detail = if (hasLeader && leaderMargin > 0) {
        stringResource(R.string.scoreboard_ahead_by, leaderMargin)
    } else {
        stringResource(R.string.scoreboard_no_margin)
    }
    val badgeText = if (hasLeader) source?.initials ?: "--" else "--"

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val shape = RoundedCornerShape(8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (hasLeader) {
                        Brush.linearGradient(
                            listOf(
                                ProposalPodiumStart,
                                ProposalPodiumEnd
                            )
                        )
                    } else {
                        Brush.linearGradient(listOf(ProposalPanel, ProposalPanel))
                    },
                    shape = shape
                )
                .border(
                    width = 1.dp,
                    color = if (hasLeader) {
                        ProposalYogi.copy(alpha = 0.55f)
                    } else {
                        ProposalLine
                    },
                    shape = shape
                )
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, ProposalAvatarLine),
                color = ProposalAvatar,
                contentColor = if (hasLeader) {
                    ProposalYogi
                } else {
                    ProposalMuted
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ProposalText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = ProposalMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${leaderEntry?.wins ?: 0}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = ProposalText
            )
        }
    }
}

@Composable
private fun MiniScoreGrid(entries: List<SourceScore>) {
    val rows = if (entries.size > 3) entries.chunked(2) else listOf(entries)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowEntries ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowEntries.forEach { entry ->
                    MiniScoreTile(
                        scoreEntry = entry,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowEntries.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MiniScoreTile(scoreEntry: SourceScore, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ProposalTileLine),
        color = ProposalTile
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = scoreEntry.source.scoreLabel,
                style = MaterialTheme.typography.labelMedium,
                color = ProposalMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${scoreEntry.wins}",
                style = MaterialTheme.typography.titleMedium,
                color = ProposalText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TieBreakButton(
    isLoading: Boolean,
    onRefreshBoth: () -> Unit
) {
    OutlinedButton(
        onClick = onRefreshBoth,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.tie_break_refresh_both))
    }
}

@Composable
private fun BattleLoadingState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
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
                text = stringResource(R.string.loading_battle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyBattleState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.battle_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VictoryBanner(source: FactSource) {
    var animationTarget by remember(source) { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 1_000, easing = FastOutSlowInEasing),
        label = "victoryBanner"
    )
    LaunchedEffect(source) {
        animationTarget = 1f
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VictoryBadge(source = source, progress = progress)
            Text(
                text = stringResource(R.string.victory_message, source.scoreLabel),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VictoryBadge(source: FactSource, progress: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surface
    val ink = MaterialTheme.colorScheme.onSurface
    val accent = when (source) {
        FactSource.CHUCK -> primary
        FactSource.CAT -> tertiary
        FactSource.DOG -> secondary
        FactSource.YOGI -> primary
    }

    Canvas(modifier = Modifier.size(64.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val bounce = kotlin.math.sin(progress * Math.PI).toFloat()
        val radius = size.minDimension * (0.25f + 0.06f * bounce)
        val burstAlpha = (1f - progress * 0.2f).coerceIn(0f, 0.8f)

        repeat(10) { index ->
            val angle = (index / 10f) * (Math.PI * 2).toFloat()
            val startDistance = size.minDimension * 0.22f
            val endDistance = size.minDimension * (0.28f + progress * 0.16f)
            val start = Offset(
                x = center.x + kotlin.math.cos(angle) * startDistance,
                y = center.y + kotlin.math.sin(angle) * startDistance
            )
            val end = Offset(
                x = center.x + kotlin.math.cos(angle) * endDistance,
                y = center.y + kotlin.math.sin(angle) * endDistance
            )
            drawLine(
                color = listOf(primary, secondary, tertiary)[index % 3].copy(alpha = burstAlpha),
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawCircle(accent.copy(alpha = 0.24f), radius * 1.7f, center)
        drawCircle(accent, radius * 1.16f, center)
        drawCircle(surface, radius, center)

        when (source) {
            FactSource.CHUCK -> drawChuckCelebration(center, radius, ink, accent)
            FactSource.CAT -> drawCatCelebration(center, radius, ink, accent)
            FactSource.DOG -> drawDogCelebration(center, radius, ink, accent)
            FactSource.YOGI -> drawYogiCelebration(center, radius, ink, accent)
        }
    }
}

@Composable
private fun BattleContenderCard(
    contender: BattleContender,
    isWinner: Boolean,
    isLoser: Boolean,
    loserExitDirection: Float,
    entryDirection: Float?,
    canSelect: Boolean,
    onSelected: () -> Unit,
    onSwipedAway: (Float) -> Unit
) {
    val entryOffset = remember { Animatable(0f) }
    val settleScope = rememberCoroutineScope()
    var dragOffset by remember(contender.quote.id) { mutableFloatStateOf(0f) }
    var isSettling by remember(contender.quote.id) { mutableStateOf(false) }

    fun settleDragOffset() {
        if (kotlin.math.abs(dragOffset) < 0.5f || isSettling) return

        isSettling = true
        settleScope.launch {
            val start = dragOffset
            Animatable(start).animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) {
                dragOffset = value
            }
            dragOffset = 0f
            isSettling = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isWinner) 1.01f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "battleScale"
    )
    val victoryProgress by animateFloatAsState(
        targetValue = if (isWinner) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "victoryAnimation"
    )
    val loserSwipeProgress by animateFloatAsState(
        targetValue = if (isLoser) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "loserSwipe"
    )

    LaunchedEffect(contender.quote.id, entryDirection) {
        val direction = entryDirection
        if (direction == null) {
            entryOffset.snapTo(0f)
        } else {
            entryOffset.snapTo(direction)
            entryOffset.animateTo(0f, animationSpec = tween(durationMillis = 420))
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = dragOffset +
                        (entryOffset.value * size.width * 1.2f) +
                        (loserExitDirection * loserSwipeProgress * size.width * 1.2f)
                    alpha = (1f - loserSwipeProgress).coerceAtLeast(0.12f)
                    rotationZ = 0f
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isWinner) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(canSelect, isSettling, contender.quote.id) {
                            if (!canSelect || isSettling) return@pointerInput
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    val threshold = size.width * 0.22f
                                    if (kotlin.math.abs(dragOffset) >= threshold) {
                                        val direction = if (dragOffset >= 0f) 1f else -1f
                                        onSwipedAway(direction)
                                    } else {
                                        settleDragOffset()
                                    }
                                },
                                onDragCancel = {
                                    settleDragOffset()
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset = (dragOffset + dragAmount).coerceIn(
                                        minimumValue = -size.width * 0.62f,
                                        maximumValue = size.width * 0.62f
                                    )
                                }
                            )
                        },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contender.source.initials,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column {
                            Text(
                                text = contender.source.scoreLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = contender.quote.value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Button(
                    onClick = onSelected,
                    enabled = canSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            R.string.choose_contender_winner,
                            contender.source.scoreLabel
                        )
                    )
                }
            }
        }
        WinnerStamp(progress = victoryProgress)
    }
}

@Composable
private fun BoxScope.WinnerStamp(progress: Float) {
    if (progress <= 0f) return

    Surface(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 30.dp, end = 18.dp)
            .graphicsLayer {
                alpha = progress
                rotationZ = -8f
                scaleX = 0.82f + progress * 0.18f
                scaleY = 0.82f + progress * 0.18f
            },
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = stringResource(R.string.victory_stamp_label),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChuckCelebration(
    center: Offset,
    radius: Float,
    ink: Color,
    accent: Color
) {
    drawLine(
        color = ink,
        start = center.copy(x = center.x - radius * 0.42f, y = center.y - radius * 0.08f),
        end = center.copy(x = center.x - radius * 0.1f, y = center.y - radius * 0.08f),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = ink,
        start = center.copy(x = center.x + radius * 0.1f, y = center.y - radius * 0.08f),
        end = center.copy(x = center.x + radius * 0.42f, y = center.y - radius * 0.08f),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = ink,
        start = center.copy(x = center.x - radius * 0.28f, y = center.y + radius * 0.28f),
        end = center.copy(x = center.x + radius * 0.28f, y = center.y + radius * 0.28f),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = accent,
        start = center.copy(x = center.x - radius * 0.55f, y = center.y - radius * 0.72f),
        end = center.copy(x = center.x + radius * 0.55f, y = center.y - radius * 0.72f),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCatCelebration(
    center: Offset,
    radius: Float,
    ink: Color,
    accent: Color
) {
    val leftEar = Path().apply {
        moveTo(center.x - radius * 0.55f, center.y - radius * 0.45f)
        lineTo(center.x - radius * 0.28f, center.y - radius * 1.1f)
        lineTo(center.x - radius * 0.05f, center.y - radius * 0.5f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(center.x + radius * 0.55f, center.y - radius * 0.45f)
        lineTo(center.x + radius * 0.28f, center.y - radius * 1.1f)
        lineTo(center.x + radius * 0.05f, center.y - radius * 0.5f)
        close()
    }
    drawPath(leftEar, accent)
    drawPath(rightEar, accent)
    drawCircle(ink, radius * 0.08f, center.copy(x = center.x - radius * 0.24f, y = center.y - radius * 0.04f))
    drawCircle(ink, radius * 0.08f, center.copy(x = center.x + radius * 0.24f, y = center.y - radius * 0.04f))
    drawLine(
        color = ink,
        start = center.copy(x = center.x - radius * 0.52f, y = center.y + radius * 0.26f),
        end = center.copy(x = center.x + radius * 0.52f, y = center.y + radius * 0.26f),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDogCelebration(
    center: Offset,
    radius: Float,
    ink: Color,
    accent: Color
) {
    drawCircle(
        color = accent,
        radius = radius * 0.33f,
        center = center.copy(x = center.x - radius * 0.65f, y = center.y - radius * 0.18f)
    )
    drawCircle(
        color = accent,
        radius = radius * 0.33f,
        center = center.copy(x = center.x + radius * 0.65f, y = center.y - radius * 0.18f)
    )
    drawCircle(ink, radius * 0.08f, center.copy(x = center.x - radius * 0.22f, y = center.y - radius * 0.02f))
    drawCircle(ink, radius * 0.08f, center.copy(x = center.x + radius * 0.22f, y = center.y - radius * 0.02f))
    drawCircle(ink, radius * 0.1f, center.copy(x = center.x, y = center.y + radius * 0.2f))
    drawArc(
        color = ink,
        startAngle = 18f,
        sweepAngle = 144f,
        useCenter = false,
        topLeft = center.copy(x = center.x - radius * 0.28f, y = center.y + radius * 0.08f),
        size = androidx.compose.ui.geometry.Size(radius * 0.56f, radius * 0.38f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawYogiCelebration(
    center: Offset,
    radius: Float,
    ink: Color,
    accent: Color
) {
    drawCircle(ink, radius * 0.08f, center.copy(x = center.x - radius * 0.22f, y = center.y - radius * 0.08f))
    drawCircle(ink, radius * 0.08f, center.copy(x = center.x + radius * 0.22f, y = center.y - radius * 0.08f))
    drawArc(
        color = ink,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = center.copy(x = center.x - radius * 0.28f, y = center.y + radius * 0.1f),
        size = androidx.compose.ui.geometry.Size(radius * 0.56f, radius * 0.34f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
    drawLine(
        color = accent,
        start = center.copy(x = center.x - radius * 0.5f, y = center.y - radius * 0.72f),
        end = center.copy(x = center.x + radius * 0.5f, y = center.y - radius * 0.72f),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = accent,
        start = center.copy(x = center.x, y = center.y - radius * 0.95f),
        end = center.copy(x = center.x, y = center.y - radius * 0.5f),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
}

@Composable
private fun BattlePeriod.label(): String {
    return when (this) {
        BattlePeriod.DAILY -> stringResource(R.string.period_daily)
        BattlePeriod.WEEKLY -> stringResource(R.string.period_weekly)
        BattlePeriod.MONTHLY -> stringResource(R.string.period_monthly)
    }
}
