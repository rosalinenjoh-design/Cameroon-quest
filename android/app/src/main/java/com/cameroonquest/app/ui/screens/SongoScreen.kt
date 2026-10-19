package com.cameroonquest.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.data.Characters
import com.cameroonquest.app.data.Difficulty
import com.cameroonquest.app.data.GameKind
import com.cameroonquest.app.data.Outcome
import com.cameroonquest.app.data.PlayMode
import com.cameroonquest.app.data.PlayerProfile
import com.cameroonquest.app.data.RoundReward
import com.cameroonquest.app.game.Songo
import com.cameroonquest.app.game.SongoState
import com.cameroonquest.app.ui.components.Avatar
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.CircleIconButton
import com.cameroonquest.app.ui.components.WovenMat
import com.cameroonquest.app.ui.components.rememberHaptics
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import com.cameroonquest.app.ui.theme.darken
import com.cameroonquest.app.ui.theme.lighten
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private val SeedBlue = Color(0xFF1E4FA8)
private val SeedWhite = Color(0xFFE9ECF1)

@Composable
fun SongoScreen(
    profile: PlayerProfile,
    difficulty: Difficulty,
    mode: PlayMode,
    onRecord: (score: Int, outcome: Outcome) -> RoundReward,
    onExit: () -> Unit,
) {
    val me = Characters.orDefault(profile.characterId)
    val rival = Characters.rivalOf(me)
    val names = listOf(profile.name.ifBlank { "You" }, if (mode == PlayMode.SOLO) rival.name else "Player 2")
    var round by remember { mutableIntStateOf(0) }
    var state by remember(round) { mutableStateOf(Songo.create()) }
    var reward by remember(round) { mutableStateOf<RoundReward?>(null) }
    var paused by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }
    val haptics = rememberHaptics()
    val computerTurn = mode == PlayMode.SOLO && state.turn == 1 && !state.over

    LaunchedEffect(state, paused, leaving) {
        if (computerTurn && !paused && !leaving) {
            delay(800)
            val current = state
            val pit = withContext(Dispatchers.Default) { Songo.chooseMove(current, difficulty) }
            state = Songo.move(current, pit)
            haptics()
        }
    }
    LaunchedEffect(state.over, round) {
        if (state.over && reward == null) reward = onRecord(state.captured[0] * 20, outcomeOf(state.winner))
    }
    BackHandler(enabled = !state.over) { leaving = true }

    val humanTurn = !state.over && !paused && (mode == PlayMode.LOCAL || state.turn == 0)
    val playable = if (humanTurn) Songo.legalMoves(state).toSet() else emptySet()
    val glow by rememberInfiniteTransition(label = "pits").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "glow",
    )

    Box(Modifier.fillMaxSize()) {
        WovenMat(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Quest.SongoWood, Quest.WoodDark)))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                CircleIconButton(
                    Icons.AutoMirrored.Rounded.ArrowBack, "Back",
                    { if (state.over) onExit() else leaving = true },
                    Modifier.align(Alignment.CenterStart),
                )
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Songo", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, style = ShadowedText)
                    Text("Level ${difficulty.level}", color = Color(0xFFFCE7C8), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                CircleIconButton(
                    Icons.Rounded.Pause, "Pause", { paused = true },
                    Modifier.align(Alignment.CenterEnd),
                    background = Quest.Yellow,
                    tint = Quest.YellowInk,
                    border = Color.White,
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayerChip(me.avatar, names[0], state.captured[0] * 20, state.turn == 0 && !state.over, Quest.PurpleTop)
                PlayerChip(rival.avatar, names[1], state.captured[1] * 20, state.turn == 1 && !state.over, rival.color, reversed = true)
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
                    state = state,
                    playable = playable,
                    pitSize = minOf(byHeight, byWidth).coerceAtLeast(40.dp),
                    names = names,
                    glow = { glow },
                    onPit = { pit ->
                        state = Songo.move(state, pit)
                        haptics()
                    },
                )
            }
            val captureNote = state.lastPit?.takeIf { state.capturedLast > 0 }?.let {
                "${names[Songo.owner(it)]} captured ${state.capturedLast} seeds!"
            }
            val (title, subtitle) = when {
                state.over -> "Round over" to state.reason
                computerTurn -> "${rival.name} is thinking\u2026" to (captureNote ?: "Watch where the seeds land.")
                mode == PlayMode.LOCAL -> "${names[state.turn]}'s turn!" to (captureNote ?: "Choose a pit to move your stones.")
                else -> "Your turn!" to (captureNote ?: "Choose a pit to move your stones.")
            }
            TurnBanner(title, subtitle)
            BottomKente()
        }
    }

    if (state.over) {
        val outcome = outcomeOf(state.winner)
        ResultDialog(
            outcome = outcome,
            title = resultTitle(outcome, mode, names),
            description = state.reason,
            lines = listOf(
                ScoreLine(names[0], "${state.captured[0]} seeds"),
                ScoreLine(names[1], "${state.captured[1]} seeds"),
            ),
            reward = reward,
            onPlayAgain = { round++ },
            onMenu = onExit,
        )
    } else if (paused) {
        PauseDialog(
            GameKind.SONGO,
            onResume = { paused = false },
            onRestart = {
                paused = false
                round++
            },
            onQuit = onExit,
        )
    } else if (leaving) {
        LeaveRoundDialog(onStay = { leaving = false }, onLeave = onExit)
    }
}

@Composable
fun PlayerChip(
    avatar: Int,
    name: String,
    score: Int,
    active: Boolean,
    ring: Color,
    modifier: Modifier = Modifier,
    reversed: Boolean = false,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (!reversed) Avatar(avatar, 50.dp, ring = ring, ringWidth = 3.dp)
        Column(
            Modifier
                .padding(horizontal = 6.dp)
                .width(96.dp)
                .clip(shape)
                .background(Quest.Cream)
                .border(2.5.dp, if (active) Quest.Gold else Quest.Line, shape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalAlignment = if (reversed) Alignment.End else Alignment.Start,
        ) {
            Text(name, color = Quest.Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = Quest.Yellow, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(3.dp))
                Text("$score", color = Quest.Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (reversed) Avatar(avatar, 50.dp, ring = ring, ringWidth = 3.dp)
    }
}

@Composable
private fun SongoBoard(
    state: SongoState,
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
        for (row in 0 until Songo.PITS) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                listOf(Songo.PITS + row, Songo.PITS - 1 - row).forEach { pit ->
                    Pit(
                        seeds = state.board[pit],
                        label = "${names[Songo.owner(pit)]} pit ${pit % Songo.PITS + 1}",
                        playable = pit in playable,
                        lastMove = pit == state.lastPit,
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
    label: String,
    playable: Boolean,
    lastMove: Boolean,
    size: Dp,
    glow: () -> Float,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(size)
            .semantics(mergeDescendants = true) { contentDescription = "$label, $seeds seeds" },
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(enabled = playable, onClickLabel = "Sow this pit", role = Role.Button, onClick = onClick),
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
                size = androidx.compose.ui.geometry.Size(r * 1.84f, r * 1.84f),
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

@Composable
fun TurnBanner(title: String, subtitle: String) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .shadow(8.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF0D5A38), Color(0xFF07402A))))
            .border(2.5.dp, Quest.Gold, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, style = ShadowedText, textAlign = TextAlign.Center)
        Text(subtitle, color = Color(0xFFE5F3E5), fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
