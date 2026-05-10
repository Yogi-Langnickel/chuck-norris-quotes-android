package com.yogi.chucknorris.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yogi.chucknorris.R
import com.yogi.chucknorris.domain.BattleContender
import com.yogi.chucknorris.domain.BattlePeriod
import com.yogi.chucknorris.domain.BattleRound
import com.yogi.chucknorris.domain.BattleScore
import com.yogi.chucknorris.domain.BattleWinner
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BattleArena(
    battleRound: BattleRound?,
    selectedWinner: BattleWinner?,
    selectedPeriod: BattlePeriod,
    battleScores: Map<BattlePeriod, BattleScore>,
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
            delay(720)
            onLoserSwipedAway()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.battle_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.battle_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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

        ScoreStrip(period = selectedPeriod, score = score)

        if (battleRound == null) {
            EmptyBattleState()
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
private fun ScoreStrip(period: BattlePeriod, score: BattleScore) {
    Text(
        text = stringResource(R.string.personal_score_heading, period.label()),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScorePill(
            label = stringResource(R.string.chuck_score),
            value = score.chuckWins,
            modifier = Modifier.weight(1f)
        )
        ScorePill(
            label = stringResource(R.string.cat_score),
            value = score.catWins,
            modifier = Modifier.weight(1f)
        )
        ScorePill(
            label = stringResource(R.string.dog_score),
            value = score.dogWins,
            modifier = Modifier.weight(1f)
        )
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
private fun ScorePill(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
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
private fun BattleContenderCard(
    contender: BattleContender,
    isWinner: Boolean,
    isLoser: Boolean,
    loserExitDirection: Float,
    entryDirection: Float?,
    canSelect: Boolean,
    onSwipedAway: (Float) -> Unit
) {
    val entryOffset = remember { Animatable(0f) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val scale by animateFloatAsState(
        targetValue = if (isWinner) 1.02f else 1f,
        animationSpec = tween(durationMillis = 450),
        label = "battleScale"
    )
    val celebrationProgress by animateFloatAsState(
        targetValue = if (isWinner) 1f else 0f,
        animationSpec = tween(durationMillis = 850),
        label = "winnerCelebration"
    )
    val loserSwipeProgress by animateFloatAsState(
        targetValue = if (isLoser) 1f else 0f,
        animationSpec = tween(durationMillis = 650),
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
        WinnerCelebration(progress = celebrationProgress)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(canSelect) {
                    if (!canSelect) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val threshold = size.width * 0.22f
                            if (kotlin.math.abs(dragOffset) >= threshold) {
                                val direction = if (dragOffset >= 0f) 1f else -1f
                                onSwipedAway(direction)
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = dragOffset +
                        (entryOffset.value * size.width * 1.2f) +
                        (loserExitDirection * loserSwipeProgress * size.width * 1.2f)
                    alpha = (1f - loserSwipeProgress).coerceAtLeast(0.12f)
                    rotationZ = (dragOffset / size.width * 4f) +
                        loserExitDirection * loserSwipeProgress * 5f
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                }

                Text(
                    text = contender.quote.value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun BoxScope.WinnerCelebration(progress: Float) {
    if (progress <= 0f) return

    val celebrationColor = MaterialTheme.colorScheme.tertiary
    Canvas(modifier = Modifier.matchParentSize()) {
        val alpha = (1f - progress).coerceIn(0f, 0.55f)
        val center = Offset(size.width / 2f, size.height / 2f)
        repeat(10) { index ->
            val angle = (index / 10f) * (Math.PI * 2).toFloat()
            val distance = progress * size.minDimension * 0.42f
            val offset = Offset(
                x = center.x + kotlin.math.cos(angle) * distance,
                y = center.y + kotlin.math.sin(angle) * distance
            )
            drawCircle(
                color = celebrationColor.copy(alpha = alpha),
                radius = 8.dp.toPx() * (1f - progress * 0.45f),
                center = offset
            )
        }
        drawCircle(
            color = celebrationColor.copy(alpha = alpha * 0.45f),
            radius = progress * size.minDimension * 0.48f,
            center = center
        )
    }
}

@Composable
private fun BattlePeriod.label(): String {
    return when (this) {
        BattlePeriod.DAILY -> stringResource(R.string.period_daily)
        BattlePeriod.WEEKLY -> stringResource(R.string.period_weekly)
        BattlePeriod.MONTHLY -> stringResource(R.string.period_monthly)
    }
}
