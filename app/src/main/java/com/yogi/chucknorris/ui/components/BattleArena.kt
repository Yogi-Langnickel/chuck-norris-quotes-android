package com.yogi.chucknorris.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BattleArena(
    battleRound: BattleRound?,
    selectedPeriod: BattlePeriod,
    battleScores: Map<BattlePeriod, BattleScore>,
    isLoading: Boolean,
    onPeriodSelected: (BattlePeriod) -> Unit,
    onBattleClick: () -> Unit
) {
    val score = battleScores[selectedPeriod] ?: BattleScore()

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
                onClick = onBattleClick,
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

        ScoreStrip(score = score)
        PeriodLeaderBanner(score = score)

        if (battleRound == null) {
            EmptyBattleState()
        } else {
            BattleContenderCard(
                label = stringResource(R.string.chuck_contender),
                contender = battleRound.chuck,
                isWinner = battleRound.winner == BattleWinner.CHUCK
            )
            BattleContenderCard(
                label = stringResource(R.string.cat_contender),
                contender = battleRound.cat,
                isWinner = battleRound.winner == BattleWinner.CAT
            )
            Text(
                text = battleRound.resultText(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
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
private fun ScoreStrip(score: BattleScore) {
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
            label = stringResource(R.string.draw_score),
            value = score.draws,
            modifier = Modifier.weight(1f)
        )
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
    isWinner: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = contender.powerProfile.progress,
        animationSpec = tween(durationMillis = 700),
        label = "battleProgress"
    )
    val scale by animateFloatAsState(
        targetValue = if (isWinner) 1.02f else 0.98f,
        animationSpec = tween(durationMillis = 450),
        label = "battleScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
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
                        Text(
                            text = contender.quote.sourceLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.battle_power_value, contender.powerProfile.score),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Text(
                text = contender.quote.value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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

@Composable
private fun BattleRound.resultText(): String {
    return when (winner) {
        BattleWinner.CHUCK -> stringResource(R.string.battle_result_chuck, margin)
        BattleWinner.CAT -> stringResource(R.string.battle_result_cat, margin)
        BattleWinner.DRAW -> stringResource(R.string.battle_result_draw)
    }
}
