package com.cameroonquest.app.ui.screens.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.game.pirogue.PirogueRace
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.domain.model.QuestCharacter
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.style.CharacterStyles
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import com.cameroonquest.app.ui.viewmodel.PirogueViewModel
import com.cameroonquest.app.ui.viewmodel.PlayerUiState
import com.cameroonquest.app.ui.viewmodel.SteerButton
import kotlin.math.sin

@Composable
fun PirogueScreen(viewModel: PirogueViewModel, player: PlayerUiState, rival: QuestCharacter, onExit: () -> Unit) {
    val setup = viewModel.setup
    val local = setup.mode == PlayMode.LOCAL
    val names = listOf(
        player.profile.name.ifBlank { stringResource(R.string.you) },
        if (local) stringResource(R.string.player_two) else rival.name,
    )
    val colors = listOf(CharacterStyles.of(player.character).color, CharacterStyles.of(rival).color)

    GameRoundScaffold(
        viewModel = viewModel,
        onExit = onExit,
        startTitle = stringResource(R.string.ready_race),
        startNote = if (local) stringResource(R.string.local_controls_note) else null,
        resultContent = { finished ->
            val race = viewModel.session
            ResultContent(
                title = resultTitle(finished.result.outcome, setup.mode, names),
                description = stringResource(R.string.pirogue_result_description),
                lines = race.racers.mapIndexed { index, racer ->
                    ScoreLine(
                        names[index],
                        stringResource(
                            R.string.pirogue_score_line,
                            race.scoreOf(index),
                            pluralStringResource(R.plurals.logs_hit, racer.hits, racer.hits),
                        ),
                    )
                },
            )
        },
    ) { onBack, onPause ->
        FrameLoop(viewModel)
        Column(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0B4F6E), Quest.JungleDark))),
        ) {
            GameHeader(
                title = stringResource(R.string.game_pirogue),
                level = setup.difficulty.level,
                brush = Brush.verticalGradient(listOf(Color(0xFF0F6FA0), Color(0xFF0A4C70))),
                onBack = onBack,
                onPause = onPause,
            )
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                names.forEachIndexed { index, name ->
                    val shape = RoundedCornerShape(18.dp)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .shadow(8.dp, shape)
                            .clip(shape)
                            .border(3.dp, Quest.CreamDeep, shape),
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            viewModel.frame
                            drawRiver(viewModel.session, index, colors[index])
                        }
                        Text(
                            name,
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
                val left = Icons.AutoMirrored.Rounded.KeyboardArrowLeft
                val right = Icons.AutoMirrored.Rounded.KeyboardArrowRight
                HoldButton(
                    left, stringResource(R.string.steer_left, names[0]), colors[0],
                    { viewModel.hold(SteerButton.PLAYER_ONE_LEFT, it) }, Modifier.weight(1f),
                )
                HoldButton(
                    right, stringResource(R.string.steer_right, names[0]), colors[0],
                    { viewModel.hold(SteerButton.PLAYER_ONE_RIGHT, it) }, Modifier.weight(1f),
                )
                if (local) {
                    Spacer(Modifier.size(6.dp))
                    HoldButton(
                        left, stringResource(R.string.steer_left, names[1]), colors[1],
                        { viewModel.hold(SteerButton.PLAYER_TWO_LEFT, it) }, Modifier.weight(1f),
                    )
                    HoldButton(
                        right, stringResource(R.string.steer_right, names[1]), colors[1],
                        { viewModel.hold(SteerButton.PLAYER_TWO_RIGHT, it) }, Modifier.weight(1f),
                    )
                }
            }
            BottomKente()
        }
    }
}

private fun DrawScope.drawRiver(race: PirogueRace, index: Int, boatColor: Color) {
    val racer = race.racers[index]
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
    for (obstacle in race.course) {
        val y = (boatY - (obstacle.distance - racer.distance) * unit).toFloat()
        if (y < -logHeight || y > h + logHeight) continue
        val cx = left + river * obstacle.x.toFloat()
        drawRoundRect(Color(0xFF6B3A17), Offset(cx - logWidth / 2, y - logHeight / 2), Size(logWidth, logHeight), CornerRadius(logHeight / 2))
        drawRoundRect(
            Color(0xFF8B5426),
            Offset(cx - logWidth / 2 + 4f, y - logHeight / 2 + 3f),
            Size(logWidth - 8f, logHeight * 0.32f),
            CornerRadius(logHeight / 4),
        )
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
    if (racer.isSlowed) {
        drawCircle(Color.White.copy(alpha = 0.5f), beam * 0.9f, Offset(bx, boatY - length * 0.45f), style = Stroke(3.dp.toPx()))
    }

    val progress = (racer.distance / race.config.length).toFloat().coerceIn(0f, 1f)
    val barTop = 30.dp.toPx()
    val barWidth = river - 16.dp.toPx()
    drawRoundRect(Color(0x66000000), Offset(left + 8.dp.toPx(), barTop), Size(barWidth, 8.dp.toPx()), CornerRadius(4.dp.toPx()))
    drawRoundRect(Quest.Gold, Offset(left + 8.dp.toPx(), barTop), Size(barWidth * progress, 8.dp.toPx()), CornerRadius(4.dp.toPx()))
}
