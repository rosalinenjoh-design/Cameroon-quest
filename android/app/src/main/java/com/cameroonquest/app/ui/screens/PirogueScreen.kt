package com.cameroonquest.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.cameroonquest.app.data.Characters
import com.cameroonquest.app.data.Difficulty
import com.cameroonquest.app.data.GameKind
import com.cameroonquest.app.data.Outcome
import com.cameroonquest.app.data.PlayMode
import com.cameroonquest.app.data.PlayerProfile
import com.cameroonquest.app.data.RoundReward
import com.cameroonquest.app.game.Pirogue
import com.cameroonquest.app.game.RaceState
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.CircleIconButton
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.QuestDialog
import com.cameroonquest.app.ui.components.rememberHaptics
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import com.cameroonquest.app.ui.theme.darken
import com.cameroonquest.app.ui.theme.lighten
import kotlinx.coroutines.isActive
import kotlin.math.sin

private fun steer(left: Boolean, right: Boolean): Double = (if (right) 1.0 else 0.0) - (if (left) 1.0 else 0.0)

@Composable
fun PirogueScreen(
    profile: PlayerProfile,
    difficulty: Difficulty,
    mode: PlayMode,
    onRecord: (score: Int, outcome: Outcome) -> RoundReward,
    onExit: () -> Unit,
) {
    val me = Characters.orDefault(profile.characterId)
    val rival = Characters.rivalOf(me, preferred = "njoh")
    val names = listOf(profile.name.ifBlank { "You" }, if (mode == PlayMode.SOLO) rival.name else "Player 2")
    var round by remember { mutableIntStateOf(0) }
    val race = remember(round) { Pirogue.create(difficulty, mode) }
    val held = remember(round) { BooleanArray(4) }
    var frame by remember { mutableLongStateOf(0L) }
    var started by remember(round) { mutableStateOf(false) }
    var running by remember(round) { mutableStateOf(false) }
    var finished by remember(round) { mutableStateOf(false) }
    var reward by remember(round) { mutableStateOf<RoundReward?>(null) }
    var paused by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }
    val haptics = rememberHaptics()

    fun stop() {
        running = false
        held.fill(false)
    }

    LaunchedEffect(running, round) {
        if (!running) return@LaunchedEffect
        var last = withFrameNanos { it }
        var hits = race.players[0].hits
        while (isActive && !race.over) {
            val now = withFrameNanos { it }
            val seconds = ((now - last) / 1_000_000_000.0).coerceIn(0.0, 0.05)
            last = now
            Pirogue.step(race, steer(held[0], held[1]), steer(held[2], held[3]), seconds)
            if (race.players[0].hits > hits) {
                hits = race.players[0].hits
                haptics()
            }
            frame++
        }
        if (race.over) {
            finished = true
            reward = onRecord(Pirogue.score(race, 0), outcomeOf(race.winner))
            stop()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        if (running) {
            stop()
            paused = true
        }
    }
    BackHandler(enabled = started && !finished) {
        stop()
        leaving = true
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0B4F6E), Quest.JungleDark))),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F6FA0), Color(0xFF0A4C70))))
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            CircleIconButton(
                Icons.AutoMirrored.Rounded.ArrowBack, "Back",
                {
                    if (!started || finished) {
                        onExit()
                    } else {
                        stop()
                        leaving = true
                    }
                },
                Modifier.align(Alignment.CenterStart),
            )
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Pirogue Racing", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, style = ShadowedText)
                Text("Level ${difficulty.level}", color = Color(0xFFD7EEFA), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            CircleIconButton(
                Icons.Rounded.Pause, "Pause",
                {
                    if (running) {
                        stop()
                        paused = true
                    }
                },
                Modifier.align(Alignment.CenterEnd),
                background = Quest.Yellow,
                tint = Quest.YellowInk,
                border = Color.White,
            )
        }
        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf(me, rival).forEachIndexed { index, character ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .shadow(8.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .border(3.dp, Quest.CreamDeep, RoundedCornerShape(18.dp)),
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        frame
                        drawRiver(race, index, character.color)
                    }
                    Text(
                        names[index],
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        style = ShadowedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HoldButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "${names[0]} steer left", me.color, { held[0] = it }, Modifier.weight(1f))
            HoldButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "${names[0]} steer right", me.color, { held[1] = it }, Modifier.weight(1f))
            if (mode == PlayMode.LOCAL) {
                Spacer(Modifier.size(6.dp))
                HoldButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Player 2 steer left", rival.color, { held[2] = it }, Modifier.weight(1f))
                HoldButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Player 2 steer right", rival.color, { held[3] = it }, Modifier.weight(1f))
            }
        }
        BottomKente()
    }

    when {
        finished -> {
            val outcome = outcomeOf(race.winner)
            ResultDialog(
                outcome = outcome,
                title = resultTitle(outcome, mode, names),
                description = "First across the line wins. Both canoes raced on the same river.",
                lines = race.players.mapIndexed { index, racer ->
                    ScoreLine(names[index], "${Pirogue.score(race, index)} pts\n${racer.hits} logs hit")
                },
                reward = reward,
                onPlayAgain = { round++ },
                onMenu = onExit,
            )
        }
        !started -> StartDialog(
            title = "Ready to race?",
            text = GameTexts.rules(GameKind.PIROGUE) +
                if (mode == PlayMode.LOCAL) "\n\nPlayer 1 uses the two left buttons, Player 2 the two right buttons." else "",
            onStart = {
                started = true
                running = true
            },
            onExit = onExit,
        )
        paused -> PauseDialog(
            GameKind.PIROGUE,
            onResume = {
                paused = false
                running = true
            },
            onRestart = {
                paused = false
                round++
            },
            onQuit = onExit,
        )
        leaving -> LeaveRoundDialog(
            onStay = {
                leaving = false
                running = true
            },
            onLeave = onExit,
        )
    }
}

@Composable
fun StartDialog(title: String, text: String, onStart: () -> Unit, onExit: () -> Unit) {
    QuestDialog(onExit, dismissible = false) {
        Text(title, color = Quest.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(text, color = Quest.Ink, fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
        QuestButton("Start", onStart, Modifier.fillMaxWidth(), height = 54.dp)
        TextButton(onClick = onExit) { Text("Back to games", color = Quest.InkSoft, fontSize = 16.sp) }
    }
}

/** Reports press and release so a finger can hold a direction; several buttons work at once (multi-touch). */
@Composable
fun HoldButton(icon: ImageVector, description: String, color: Color, onHold: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val current by rememberUpdatedState(onHold)
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .height(72.dp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    if (pressed) listOf(color.darken(0.25f), color.darken(0.4f)) else listOf(color.lighten(0.1f), color.darken(0.2f)),
                ),
            )
            .border(2.dp, Color.White.copy(alpha = 0.6f), shape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    current(true)
                    waitForUpOrCancellation()
                    pressed = false
                    current(false)
                }
            }
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
    }
}

private fun DrawScope.drawRiver(race: RaceState, index: Int, boatColor: Color) {
    val racer = race.players[index]
    val w = size.width
    val h = size.height
    val bank = w * 0.08f
    val left = bank
    val right = w - bank
    val river = right - left
    drawRect(Color(0xFF2F8F3A), size = Size(bank, h))
    drawRect(Color(0xFF2F8F3A), topLeft = Offset(right, 0f), size = Size(bank, h))
    drawRect(
        Brush.horizontalGradient(listOf(Color(0xFF175F99), Color(0xFF2C93D6), Color(0xFF175F99)), startX = left, endX = right),
        topLeft = Offset(left, 0f),
        size = Size(river, h),
    )
    val unit = h / 380f
    val boatY = h * 0.8f

    val spacing = h / 7f
    val drift = ((racer.distance * unit) % spacing).toFloat()
    for (k in -1..8) {
        val y = k * spacing + drift
        val x = left + river * (0.12f + 0.3f * ((k + 9) % 3))
        drawLine(Color.White.copy(alpha = 0.25f), Offset(x, y), Offset(x + river * 0.18f, y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(Color(0xFF247A2E), bank * 0.55f, Offset(bank * 0.4f, y + spacing * 0.4f))
        drawCircle(Color(0xFF247A2E), bank * 0.55f, Offset(w - bank * 0.4f, y + spacing * 0.1f))
    }

    val finishY = (boatY - (race.config.length - racer.distance) * unit).toFloat()
    if (finishY > -40f && finishY < h) {
        val square = river / 10f
        for (i in 0 until 10) {
            for (j in 0 until 2) {
                drawRect(
                    if ((i + j) % 2 == 0) Color.White else Color(0xFF1C1208),
                    Offset(left + i * square, finishY - square + j * square),
                    Size(square, square),
                )
            }
        }
    }

    val logWidth = river * 0.28f
    val logHeight = 16.dp.toPx()
    for (obstacle in race.obstacles) {
        val y = (boatY - (obstacle.distance - racer.distance) * unit).toFloat()
        if (y < -logHeight || y > h + logHeight) continue
        val cx = left + river * obstacle.x.toFloat()
        drawRoundRect(Color(0xFF6B3A17), Offset(cx - logWidth / 2, y - logHeight / 2), Size(logWidth, logHeight), CornerRadius(logHeight / 2))
        drawRoundRect(Color(0xFF8B5426), Offset(cx - logWidth / 2 + 4f, y - logHeight / 2 + 3f), Size(logWidth - 8f, logHeight * 0.32f), CornerRadius(logHeight / 4))
        drawCircle(Color(0xFFC08A55), logHeight * 0.4f, Offset(cx + logWidth / 2 - logHeight * 0.42f, y))
        drawCircle(Color(0xFF8B5426), logHeight * 0.18f, Offset(cx + logWidth / 2 - logHeight * 0.42f, y))
    }

    val bx = left + river * racer.x.toFloat()
    val beam = river * 0.12f
    val length = beam * 3.4f
    val hull = Path().apply {
        moveTo(bx, boatY - length / 2)
        cubicTo(bx + beam * 0.75f, boatY - length * 0.3f, bx + beam * 0.75f, boatY + length * 0.3f, bx, boatY + length / 2)
        cubicTo(bx - beam * 0.75f, boatY + length * 0.3f, bx - beam * 0.75f, boatY - length * 0.3f, bx, boatY - length / 2)
        close()
    }
    drawPath(hull, Color(0xFF8A4A1C))
    drawPath(hull, Color(0xFF4A2008), style = Stroke(2.dp.toPx()))
    drawCircle(boatColor, beam * 0.34f, Offset(bx, boatY))
    drawCircle(Color.White, beam * 0.34f, Offset(bx, boatY), style = Stroke(1.5.dp.toPx()))
    val swing = sin(race.time * 6).toFloat()
    drawLine(
        Color(0xFFE6B877),
        Offset(bx - beam * 0.95f, boatY + swing * beam * 0.6f),
        Offset(bx + beam * 0.95f, boatY - swing * beam * 0.6f),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round,
    )
    if (racer.slowFor > 0) {
        drawCircle(Color.White.copy(alpha = 0.5f), beam * 0.9f, Offset(bx, boatY - length * 0.45f), style = Stroke(3.dp.toPx()))
    }

    val progress = (racer.distance / race.config.length).toFloat().coerceIn(0f, 1f)
    val barTop = 30.dp.toPx()
    val barWidth = river - 16.dp.toPx()
    drawRoundRect(Color(0x66000000), Offset(left + 8.dp.toPx(), barTop), Size(barWidth, 8.dp.toPx()), CornerRadius(4.dp.toPx()))
    drawRoundRect(Quest.Gold, Offset(left + 8.dp.toPx(), barTop), Size(barWidth * progress, 8.dp.toPx()), CornerRadius(4.dp.toPx()))
}
