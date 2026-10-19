package com.cameroonquest.app.ui.screens.games

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.game.songo.SongoEnding
import com.cameroonquest.app.domain.game.songo.SongoGame
import com.cameroonquest.app.domain.game.songo.SongoPosition
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.domain.model.QuestCharacter
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.WovenMat
import com.cameroonquest.app.ui.style.CharacterStyles
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.darken
import com.cameroonquest.app.ui.theme.lighten
import com.cameroonquest.app.ui.viewmodel.PlayerUiState
import com.cameroonquest.app.ui.viewmodel.SongoViewModel
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private val SeedBlue = Color(0xFF1E4FA8)
private val SeedWhite = Color(0xFFE9ECF1)

@Composable
fun SongoScreen(viewModel: SongoViewModel, player: PlayerUiState, rival: QuestCharacter, onExit: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val setup = viewModel.setup
    val position = state.position
    val solo = setup.mode == PlayMode.SOLO
    val names = listOf(
        player.profile.name.ifBlank { stringResource(R.string.you) },
        if (solo) rival.name else stringResource(R.string.player_two),
    )
    val mine = CharacterStyles.of(player.character)
    val theirs = CharacterStyles.of(rival)
    val playable = if (phase.acceptsMoves) state.playablePits else emptySet()
    val glow by rememberInfiniteTransition(label = "pits").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "glow",
    )

    GameRoundScaffold(
        viewModel = viewModel,
        onExit = onExit,
        resultContent = { finished ->
            ResultContent(
                title = resultTitle(finished.result.outcome, setup.mode, names),
                description = endingText(position.ending),
                lines = listOf(
                    ScoreLine(names[0], seedsText(position.captured[0])),
                    ScoreLine(names[1], seedsText(position.captured[1])),
                ),
            )
        },
    ) { onBack, onPause ->
        Box(Modifier.fillMaxSize()) {
            WovenMat(Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize()) {
                GameHeader(
                    title = stringResource(R.string.game_songo),
                    level = setup.difficulty.level,
                    brush = Brush.verticalGradient(listOf(Quest.SongoWood, Quest.WoodDark)),
                    onBack = onBack,
                    onPause = onPause,
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerChip(
                        mine.avatar, names[0], position.captured[0] * SongoGame.POINTS_PER_SEED,
                        position.turn == 0 && !position.isOver, Quest.PurpleTop,
                    )
                    PlayerChip(
                        theirs.avatar, names[1], position.captured[1] * SongoGame.POINTS_PER_SEED,
                        position.turn == 1 && !position.isOver, theirs.color, reversed = true,
                    )
                }
                BoxWithConstraints(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val padding = 14.dp
                    val gapX = 18.dp
                    val gapY = 8.dp
                    val labels = 30.dp
                    val byHeight = (maxHeight - padding * 2 - gapY * 5 - labels) / 5
                    val byWidth = (maxWidth.coerceAtMost(340.dp) - padding * 2 - gapX) / 2
                    SongoBoard(
                        position = position,
                        playable = playable,
                        pitSize = minOf(byHeight, byWidth).coerceAtLeast(40.dp),
                        names = names,
                        glow = { glow },
                        onPit = { viewModel.play(it) },
                    )
                }
                val captureNote = position.lastPit?.takeIf { position.capturedLast > 0 }?.let {
                    stringResource(R.string.songo_captured, names[SongoPosition.ownerOf(it)], seedsText(position.capturedLast))
                }
                val chooseNote = captureNote ?: stringResource(R.string.songo_choose_pit)
                val (title, subtitle) = when {
                    position.isOver -> stringResource(R.string.songo_round_over) to endingText(position.ending)
                    state.computerTurn -> stringResource(R.string.songo_thinking, rival.name) to
                        (captureNote ?: stringResource(R.string.songo_watch))
                    !solo -> stringResource(R.string.songo_named_turn, names[position.turn]) to chooseNote
                    else -> stringResource(R.string.songo_your_turn) to chooseNote
                }
                TurnBanner(title, subtitle)
                BottomKente()
            }
        }
    }
}

@Composable
private fun seedsText(count: Int): String = pluralStringResource(R.plurals.seeds_count, count, count)

@Composable
private fun endingText(ending: SongoEnding?): String = stringResource(
    when (ending) {
        SongoEnding.MAJORITY, null -> R.string.songo_end_majority
        SongoEnding.NO_MOVES -> R.string.songo_end_no_moves
        SongoEnding.REPETITION -> R.string.songo_end_repetition
        SongoEnding.MOVE_LIMIT -> R.string.songo_end_move_limit
    },
)

@Composable
private fun SongoBoard(
    position: SongoPosition,
    playable: Set<Int>,
    pitSize: Dp,
    names: List<String>,
    glow: () -> Float,
    onPit: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(30.dp)
    Column(
        Modifier
            .shadow(16.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFFB8682E), Color(0xFF8E471A), Color(0xFF6E3412))))
            .drawBehind {
                val grain = Color(0x1F000000)
                for (i in 1..9) {
                    val x = size.width * i / 10f
                    drawLine(grain, Offset(x, 0f), Offset(x + (i % 3 - 1) * 12f, size.height), strokeWidth = 3f)
                }
            }
            .border(4.dp, Color(0xFF4A2008), shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            ColumnLabel(names[1], Icons.Rounded.KeyboardArrowDown, Modifier.width(pitSize))
            ColumnLabel(names[0], Icons.Rounded.KeyboardArrowUp, Modifier.width(pitSize))
        }
        // Rotated layout: player 1 owns the right column (pits 0-4, bottom to top), seeds then travel down the left column.
        for (row in 0 until SongoPosition.PITS) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                listOf(SongoPosition.PITS + row, SongoPosition.PITS - 1 - row).forEach { pit ->
                    val seeds = position.board[pit]
                    Pit(
                        seeds = seeds,
                        description = stringResource(
                            R.string.songo_pit_description,
                            names[SongoPosition.ownerOf(pit)],
                            pit % SongoPosition.PITS + 1,
                            seedsText(seeds),
                        ),
                        playable = pit in playable,
                        lastMove = pit == position.lastPit,
                        size = pitSize,
                        glow = glow,
                        onClick = { onPit(pit) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnLabel(text: String, icon: ImageVector, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            color = Color(0xFFFCE7C8),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(icon, contentDescription = null, tint = Quest.Gold, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun Pit(
    seeds: Int,
    description: String,
    playable: Boolean,
    lastMove: Boolean,
    size: Dp,
    glow: () -> Float,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(size)
            .semantics(mergeDescendants = true) { contentDescription = description },
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(enabled = playable, onClickLabel = stringResource(R.string.sow_pit), role = Role.Button, onClick = onClick),
        ) {
            val r = this.size.minDimension / 2
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFF2E1406), Color(0xFF55280E), Color(0xFF7C3F18)), center = center, radius = r),
                r,
            )
            drawCircle(Color(0x66000000), r * 0.96f, style = Stroke(r * 0.08f))
            drawArc(
                Color(0x40FFFFFF), startAngle = 20f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(r * 0.08f, r * 0.08f),
                size = Size(r * 1.84f, r * 1.84f),
                style = Stroke(r * 0.05f),
            )
            val count = min(seeds, 16)
            for (i in 0 until count) {
                val angle = i * 2.39996f
                val distance = r * 0.56f * sqrt((i + 0.5f) / max(count, 1))
                drawSeed(center + Offset(cos(angle) * distance, sin(angle) * distance), r * 0.2f, if (i % 2 == 0) SeedBlue else SeedWhite)
            }
            when {
                playable -> drawCircle(Quest.Gold.copy(alpha = glow()), r - 2.dp.toPx(), style = Stroke(4.dp.toPx()))
                lastMove -> drawCircle(Color.White.copy(alpha = 0.55f), r - 2.dp.toPx(), style = Stroke(3.dp.toPx()))
            }
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(size * 0.32f)
                .clip(CircleShape)
                .background(Color(0xFFFCE7C8))
                .border(1.5.dp, Quest.WoodDark, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$seeds", color = Quest.WoodDark, fontSize = (size.value * 0.15f).sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

private fun DrawScope.drawSeed(center: Offset, radius: Float, base: Color) {
    drawCircle(Color(0x66000000), radius, center + Offset(radius * 0.15f, radius * 0.28f))
    drawCircle(
        Brush.radialGradient(
            listOf(base.lighten(0.45f), base, base.darken(0.35f)),
            center = center - Offset(radius * 0.35f, radius * 0.35f),
            radius = radius * 1.6f,
        ),
        radius,
        center,
    )
    drawCircle(Color.White.copy(alpha = 0.8f), radius * 0.25f, center - Offset(radius * 0.35f, radius * 0.38f))
}
