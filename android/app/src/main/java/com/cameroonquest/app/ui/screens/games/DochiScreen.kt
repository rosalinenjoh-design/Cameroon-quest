package com.cameroonquest.app.ui.screens.games

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.game.dochi.DochiGame
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.domain.model.QuestCharacter
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.JungleBackground
import com.cameroonquest.app.ui.style.CharacterStyles
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import com.cameroonquest.app.ui.viewmodel.DochiViewModel
import com.cameroonquest.app.ui.viewmodel.PlayerUiState
import kotlin.math.roundToInt

@Composable
fun DochiScreen(viewModel: DochiViewModel, player: PlayerUiState, throwers: List<QuestCharacter>, onExit: () -> Unit) {
    val setup = viewModel.setup
    val avatar = ImageBitmap.imageResource(CharacterStyles.of(player.character).avatar)
    val throwerColors = throwers.map { CharacterStyles.of(it).color }

    GameRoundScaffold(
        viewModel = viewModel,
        onExit = onExit,
        startTitle = stringResource(R.string.ready_dodge),
        resultContent = { finished ->
            val game = viewModel.session
            ResultContent(
                title = resultTitle(finished.result.outcome, PlayMode.SOLO, emptyList()),
                description = if (game.won) {
                    stringResource(R.string.dochi_survived_all, game.config.duration.toInt())
                } else {
                    stringResource(R.string.dochi_caught, game.time)
                },
                lines = listOf(
                    ScoreLine(stringResource(R.string.survived), stringResource(R.string.seconds_value, game.time)),
                    ScoreLine(stringResource(R.string.score), "${finished.result.score}"),
                ),
            )
        },
    ) { onBack, onPause ->
        FrameLoop(viewModel)
        Box(Modifier.fillMaxSize()) {
            JungleBackground(Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                GameHeader(
                    title = stringResource(R.string.game_dochi),
                    level = setup.difficulty.level,
                    brush = Brush.verticalGradient(listOf(Quest.Teal, Quest.TealDark)),
                    onBack = onBack,
                    onPause = onPause,
                )
                Canvas(
                    Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .aspectRatio((DochiGame.WIDTH / DochiGame.HEIGHT).toFloat())
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .border(3.dp, Quest.CreamDeep, RoundedCornerShape(20.dp)),
                ) {
                    viewModel.frame
                    drawCourt(viewModel.session, avatar, throwerColors)
                }
                Text(
                    stringResource(R.string.dochi_hint),
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
                    Joystick(stringResource(R.string.joystick), onMove = { x, y -> viewModel.moveStick(x, y) })
                }
                BottomKente()
            }
        }
    }
}

@Composable
private fun Joystick(description: String, onMove: (Float, Float) -> Unit, modifier: Modifier = Modifier) {
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
            .semantics { contentDescription = description },
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

private fun DrawScope.drawCourt(game: DochiGame, avatar: ImageBitmap, throwers: List<Color>) {
    val s = size.width / DochiGame.WIDTH.toFloat()
    drawRect(Brush.verticalGradient(listOf(Color(0xFFDDA567), Color(0xFFC0814A))))
    val r = DochiGame.PLAYER_RADIUS.toFloat()
    val courtLeft = (DochiGame.MIN_X.toFloat() - r) * s
    val courtTop = (DochiGame.MIN_Y.toFloat() - r) * s
    val courtSize = Size(
        (DochiGame.MAX_X - DochiGame.MIN_X).toFloat() * s + 2 * r * s,
        (DochiGame.MAX_Y - DochiGame.MIN_Y).toFloat() * s + 2 * r * s,
    )
    drawRect(Color(0x22FFFFFF), Offset(courtLeft, courtTop), courtSize)
    drawRect(Color.White.copy(alpha = 0.8f), Offset(courtLeft, courtTop), courtSize, style = Stroke(3.dp.toPx()))
    drawLine(
        Color.White.copy(alpha = 0.6f),
        Offset(size.width / 2, courtTop),
        Offset(size.width / 2, courtTop + courtSize.height),
        strokeWidth = 2.dp.toPx(),
    )
    drawCircle(Color.White.copy(alpha = 0.6f), 40f * s, Offset(size.width / 2, size.height / 2), style = Stroke(2.dp.toPx()))

    listOf(DochiGame.LEFT_THROWER_X, DochiGame.RIGHT_THROWER_X).forEachIndexed { i, x ->
        val at = Offset(x.toFloat() * s, DochiGame.THROWER_Y.toFloat() * s)
        val ready = game.nextSide == i
        drawCircle(Color(0x55000000), 22f * s, at + Offset(2f * s, 4f * s))
        drawCircle(throwers.getOrElse(i) { Quest.KenteRed }, 20f * s, at)
        drawCircle(if (ready) Quest.Gold else Color.White, 20f * s, at, style = Stroke(3.dp.toPx()))
    }

    for (ball in game.balls) {
        val at = Offset(ball.x.toFloat() * s, ball.y.toFloat() * s)
        val radius = DochiGame.BALL_RADIUS.toFloat() * s
        drawCircle(Color(0x44000000), radius, at + Offset(2f * s, 3f * s))
        drawCircle(
            Brush.radialGradient(listOf(Color.White, Color(0xFFF08A24)), center = at - Offset(radius * 0.3f, radius * 0.3f), radius = radius * 1.4f),
            radius,
            at,
        )
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
