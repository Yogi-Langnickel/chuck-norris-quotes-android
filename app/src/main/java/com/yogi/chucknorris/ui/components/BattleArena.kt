package com.yogi.chucknorris.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.yogi.chucknorris.domain.BattleStreak
import com.yogi.chucknorris.domain.BattleWinner
import com.yogi.chucknorris.domain.FactSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            delay(1_100)
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
        ChampionStreakBanner(streak = battleStreak)

        if (battleRound == null) {
            if (isLoading) {
                BattleLoadingState()
            } else {
                EmptyBattleState()
            }
        } else {
            selectedWinner
                ?.takeIf { it != BattleWinner.DRAW }
                ?.let(battleRound::contenderFor)
                ?.let { winningContender ->
                    VictoryBanner(source = winningContender.source)
                }
            BattleChoiceStatus(round = battleRound, selectedWinner = selectedWinner)
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
private fun BattleChoiceStatus(round: BattleRound, selectedWinner: BattleWinner?) {
    val statusText = if (selectedWinner == null) {
        stringResource(R.string.battle_choice_pending)
    } else {
        val winner = round.contenderFor(selectedWinner)
        val loser = round.loserFor(selectedWinner)
        if (winner == null || loser == null) {
            stringResource(R.string.battle_choice_pending)
        } else {
            stringResource(
                R.string.battle_choice_selected,
                winner.source.scoreLabel,
                loser.source.scoreLabel
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChampionStreakBanner(streak: BattleStreak) {
    val champion = streak.champion ?: return
    if (!streak.isActive) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.champion_streak_heading),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(
                    R.string.champion_streak_body,
                    champion.sourceLabel,
                    streak.wins
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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
        animationSpec = tween(durationMillis = 1_000, easing = FastOutSlowInEasing),
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
                }
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
        WinnerCelebration(
            source = contender.source,
            progress = victoryProgress
        )
    }
}

@Composable
private fun BoxScope.WinnerCelebration(source: FactSource, progress: Float) {
    if (progress <= 0f) return

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surface
    val ink = MaterialTheme.colorScheme.onSurface
    val accent = when (source) {
        FactSource.CHUCK -> primary
        FactSource.CAT -> tertiary
        FactSource.DOG -> secondary
    }

    Canvas(
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer { alpha = 0.95f }
    ) {
        val burstAlpha = (1f - progress).coerceIn(0f, 0.82f)
        val center = Offset(size.width - 60.dp.toPx(), 52.dp.toPx())
        val bounce = kotlin.math.sin(progress * Math.PI).toFloat()
        val mascotRadius = 26.dp.toPx() * (0.76f + 0.22f * bounce + 0.1f * progress)

        repeat(16) { index ->
            val angle = (index / 16f) * (Math.PI * 2).toFloat()
            val distance = progress * size.minDimension * 0.44f
            val start = Offset(
                x = center.x + kotlin.math.cos(angle) * distance,
                y = center.y + kotlin.math.sin(angle) * distance
            )
            val end = Offset(
                x = start.x + kotlin.math.cos(angle) * 13.dp.toPx(),
                y = start.y + kotlin.math.sin(angle) * 13.dp.toPx()
            )
            val color = when (index % 3) {
                0 -> primary
                1 -> secondary
                else -> tertiary
            }
            drawLine(
                color = color.copy(alpha = burstAlpha),
                start = start,
                end = end,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawCircle(
            color = accent.copy(alpha = 0.25f * (1f - progress * 0.2f)),
            radius = mascotRadius * (1.45f + 0.28f * bounce),
            center = center
        )
        drawCircle(
            color = accent,
            radius = mascotRadius,
            center = center
        )
        drawCircle(
            color = surface,
            radius = mascotRadius * 0.78f,
            center = center
        )

        when (source) {
            FactSource.CHUCK -> drawChuckCelebration(center, mascotRadius, ink, accent)
            FactSource.CAT -> drawCatCelebration(center, mascotRadius, ink, accent)
            FactSource.DOG -> drawDogCelebration(center, mascotRadius, ink, accent)
        }
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

@Composable
private fun BattlePeriod.label(): String {
    return when (this) {
        BattlePeriod.DAILY -> stringResource(R.string.period_daily)
        BattlePeriod.WEEKLY -> stringResource(R.string.period_weekly)
        BattlePeriod.MONTHLY -> stringResource(R.string.period_monthly)
    }
}
