package com.cameroonquest.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import com.cameroonquest.app.game.Dochi
import com.cameroonquest.app.game.DochiState
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.CircleIconButton
import com.cameroonquest.app.ui.components.JungleBackground
import com.cameroonquest.app.ui.components.rememberHaptics
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DochiScreen(
    profile: PlayerProfile,
    difficulty: Difficulty,
    onRecord: (score: Int, outcome: Outcome) -> RoundReward,
    onExit: () -> Unit,
) {
    val me = Characters.orDefault(profile.characterId)
    val throwers = Characters.all.filter { it.id != me.id }.take(2).map { it.color }
    val avatar = ImageBitmap.imageResource(me.avatar)
    var round by remember { mutableIntStateOf(0) }
    val game = remember(round) { Dochi.create(difficulty) }
    val stick = remember(round) { FloatArray(2) }
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
        stick.fill(0f)
    }

    LaunchedEffect(running, round) {
        if (!running) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (isActive && !game.over) {
            val now = withFrameNanos { it }
            val seconds = ((now - last) / 1_000_000_000.0).coerceIn(0.0, 0.05)
            last = now
            Dochi.step(game, stick[0].toDouble(), stick[1].toDouble(), seconds)
            frame++
        }
        if (game.over) {
            if (!game.won) haptics()
            finished = true
            reward = onRecord(game.score, if (game.won) Outcome.WIN else Outcome.LOSS)
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

    Box(Modifier.fillMaxSize()) {
        JungleBackground(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Quest.Teal, Quest.TealDark)))
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
                    Text("Dochi", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, style = ShadowedText)
                    Text("Level ${difficulty.level}", color = Color(0xFFD7F2E8), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
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
            Canvas(
                Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .aspectRatio((Dochi.WIDTH / Dochi.HEIGHT).toFloat())
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .border(3.dp, Quest.CreamDeep, RoundedCornerShape(20.dp)),
            ) {
                frame
                drawCourt(game, avatar, throwers)
            }
            Text(
                "Move with the joystick. Stay away from the balls!",
                color = Color.White,
                fontSize = 15.sp,
                style = ShadowedText,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Joystick(onMove = { x, y ->
                    if (running) {
                        stick[0] = x
                        stick[1] = y
                    }
                })
            }
            BottomKente()
        }
    }

    when {
        finished -> {
            val outcome = if (game.won) Outcome.WIN else Outcome.LOSS
            ResultDialog(
                outcome = outcome,
                title = resultTitle(outcome, PlayMode.SOLO, listOf(profile.name, "")),
                description = if (game.won) {
                    "You survived all ${game.config.duration.toInt()} seconds!"
                } else {
                    String.format(Locale.US, "A thrower got you after %.1f seconds. Keep moving and try again.", game.time)
                },
                lines = listOf(ScoreLine("Survived", String.format(Locale.US, "%.1f s", game.time)), ScoreLine("Score", "${game.score}")),
                reward = reward,
                onPlayAgain = { round++ },
                onMenu = onExit,
            )
        }
        !started -> StartDialog(
            title = "Ready, set, dodge!",
            text = GameTexts.rules(GameKind.DOCHI),
            onStart = {
                started = true
                running = true
            },
            onExit = onExit,
        )
        paused -> PauseDialog(
            GameKind.DOCHI,
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
private fun Joystick(onMove: (Float, Float) -> Unit, modifier: Modifier = Modifier) {
    var knob by remember { mutableStateOf(Offset.Zero) }
    val current by rememberUpdatedState(onMove)
    Box(
        modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0x40FFFFFF), Color(0x66000000))))
            .border(3.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            .pointerInput(Unit) {
                val radius = size.width / 2f
                val reach = radius * 0.62f
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun update(position: Offset) {
                        val vector = position - Offset(radius, radius)
                        val length = vector.getDistance()
                        val clamped = if (length > reach) vector * (reach / length) else vector
                        knob = clamped
                        current(clamped.x / reach, clamped.y / reach)
                    }
                    update(down.position)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        update(change.position)
                        change.consume()
                    }
                    knob = Offset.Zero
                    current(0f, 0f)
                }
            }
            .semantics { contentDescription = "Movement joystick" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .offset { IntOffset(knob.x.roundToInt(), knob.y.roundToInt()) }
                .size(66.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Quest.YellowTop, Quest.YellowDark)))
                .border(3.dp, Color.White, CircleShape),
        )
    }
}

private fun DrawScope.drawCourt(game: DochiState, avatar: ImageBitmap, throwers: List<Color>) {
    val s = size.width / Dochi.WIDTH.toFloat()
    drawRect(Brush.verticalGradient(listOf(Color(0xFFDDA567), Color(0xFFC0814A))))
    val r = Dochi.PLAYER_RADIUS.toFloat()
    val courtLeft = (Dochi.MIN_X.toFloat() - r) * s
    val courtTop = (Dochi.MIN_Y.toFloat() - r) * s
    val courtSize = Size(
        (Dochi.MAX_X - Dochi.MIN_X).toFloat() * s + 2 * r * s,
        (Dochi.MAX_Y - Dochi.MIN_Y).toFloat() * s + 2 * r * s,
    )
    drawRect(Color(0x22FFFFFF), Offset(courtLeft, courtTop), courtSize)
    drawRect(Color.White.copy(alpha = 0.8f), Offset(courtLeft, courtTop), courtSize, style = Stroke(3.dp.toPx()))
    drawLine(Color.White.copy(alpha = 0.6f), Offset(size.width / 2, courtTop), Offset(size.width / 2, courtTop + courtSize.height), strokeWidth = 2.dp.toPx())
    drawCircle(Color.White.copy(alpha = 0.6f), 40f * s, Offset(size.width / 2, size.height / 2), style = Stroke(2.dp.toPx()))

    listOf(Dochi.LEFT_THROWER_X, Dochi.RIGHT_THROWER_X).forEachIndexed { i, x ->
        val at = Offset(x.toFloat() * s, Dochi.THROWER_Y.toFloat() * s)
        val ready = game.nextSide == i
        drawCircle(Color(0x55000000), 22f * s, at + Offset(2f * s, 4f * s))
        drawCircle(throwers.getOrElse(i) { Quest.KenteRed }, 20f * s, at)
        drawCircle(if (ready) Quest.Gold else Color.White, 20f * s, at, style = Stroke(3.dp.toPx()))
    }

    for (ball in game.balls) {
        val at = Offset(ball.x.toFloat() * s, ball.y.toFloat() * s)
        val radius = Dochi.BALL_RADIUS.toFloat() * s
        drawCircle(Color(0x44000000), radius, at + Offset(2f * s, 3f * s))
        drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFF08A24)), center = at - Offset(radius * 0.3f, radius * 0.3f), radius = radius * 1.4f), radius, at)
    }

    val player = Offset(game.x.toFloat() * s, game.y.toFloat() * s)
    val pr = 16f * s
    drawCircle(Color(0x55000000), pr, player + Offset(2f * s, 4f * s))
    clipPath(Path().apply { addOval(Rect(center = player, radius = pr)) }) {
        drawImage(
            avatar,
            dstOffset = IntOffset((player.x - pr).roundToInt(), (player.y - pr).roundToInt()),
            dstSize = IntSize((pr * 2).roundToInt(), (pr * 2).roundToInt()),
        )
    }
    drawCircle(Color.White, pr, player, style = Stroke(2.5.dp.toPx()))

    val progress = (game.time / game.config.duration).toFloat().coerceIn(0f, 1f)
    val barWidth = size.width - 24.dp.toPx()
    drawRoundRect(Color(0x66000000), Offset(12.dp.toPx(), 8.dp.toPx()), Size(barWidth, 10.dp.toPx()), CornerRadius(5.dp.toPx()))
    drawRoundRect(Quest.Green, Offset(12.dp.toPx(), 8.dp.toPx()), Size(barWidth * progress, 10.dp.toPx()), CornerRadius(5.dp.toPx()))
}
