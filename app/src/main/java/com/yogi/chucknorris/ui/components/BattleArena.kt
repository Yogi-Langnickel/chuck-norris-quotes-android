package com.yogi.chucknorris.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
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

    LaunchedEffect(battleRound?.chuck?.quote?.id, battleRound?.cat?.quote?.id, selectedWinner) {
        if (selectedWinner == BattleWinner.CHUCK || selectedWinner == BattleWinner.CAT) {
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
            Button(
                onClick = onRefreshBoth,
                enabled = !isLoading
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (battleRound == null) {
                            R.string.start_battle
                        } else {
                            R.string.rematch_battle
                        }
                    )
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
        PeriodLeaderBanner(score = score)

        if (battleRound == null) {
            EmptyBattleState()
        } else {
            BattleContenderCard(
                label = stringResource(R.string.chuck_contender),
                contender = battleRound.chuck,
                isWinner = selectedWinner == BattleWinner.CHUCK,
                isLoser = selectedWinner == BattleWinner.CAT,
                swipeDirection = -1f,
                canSelect = selectedWinner == null && !isLoading,
                actionLabel = stringResource(R.string.choose_chuck_winner),
                onSelect = { onWinnerSelected(BattleWinner.CHUCK) }
            )
            BattleContenderCard(
                label = stringResource(R.string.cat_contender),
                contender = battleRound.cat,
                isWinner = selectedWinner == BattleWinner.CAT,
                isLoser = selectedWinner == BattleWinner.CHUCK,
                swipeDirection = 1f,
                canSelect = selectedWinner == null && !isLoading,
                actionLabel = stringResource(R.string.choose_cat_winner),
                onSelect = { onWinnerSelected(BattleWinner.CAT) }
            )
            BattleChoiceStatus(selectedWinner = selectedWinner)
            AnimatedVisibility(visible = selectedWinner != null) {
                PostSelectionControls(
                    isLoading = isLoading,
                    onRefreshBoth = onRefreshBoth
                )
            }
        }
    }
}

@Composable
private fun BattleChoiceStatus(selectedWinner: BattleWinner?) {
    val message = when (selectedWinner) {
            BattleWinner.CHUCK -> stringResource(R.string.battle_choice_chuck)
            BattleWinner.CAT -> stringResource(R.string.battle_choice_cat)
            BattleWinner.DRAW, null -> stringResource(R.string.battle_choice_pending)
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (selectedWinner == null) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = if (selectedWinner == null) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PeriodLeaderBanner(score: BattleScore) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = score.periodLeaderText(),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ScoreStrip(period: BattlePeriod, score: BattleScore) {
    Text(
        text = stringResource(R.string.personal_score_heading, period.label()),
        style = MaterialTheme.typography.labelLarge,
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
    }
}

@Composable
private fun PostSelectionControls(
    isLoading: Boolean,
    onRefreshBoth: () -> Unit
) {
    OutlinedButton(
        onClick = onRefreshBoth,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
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
    label: String,
    contender: BattleContender,
    isWinner: Boolean,
    isLoser: Boolean,
    swipeDirection: Float,
    canSelect: Boolean,
    actionLabel: String,
    onSelect: () -> Unit
) {
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

    Box(modifier = Modifier.fillMaxWidth()) {
        WinnerCelebration(progress = celebrationProgress)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = swipeDirection * loserSwipeProgress * size.width * 1.2f
                    alpha = (1f - loserSwipeProgress).coerceAtLeast(0.12f)
                    rotationZ = swipeDirection * loserSwipeProgress * 5f
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
                                    text = label.take(2).uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column {
                            Text(
                                text = label,
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
                    onClick = onSelect,
                    enabled = canSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isWinner) {
                            stringResource(R.string.winner_selected)
                        } else {
                            actionLabel
                        }
                    )
                }
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

@Composable
private fun BattleScore.periodLeaderText(): String {
    if (totalBattles == 0) {
        return stringResource(R.string.battle_leader_empty)
    }

    return when (leader) {
        BattleWinner.CHUCK -> stringResource(R.string.battle_leader_chuck, leaderMargin)
        BattleWinner.CAT -> stringResource(R.string.battle_leader_cat, leaderMargin)
        BattleWinner.DRAW -> stringResource(R.string.battle_leader_draw)
    }
}
