package com.cameroonquest.app.ui.screens.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.domain.model.QuizQuestion
import com.cameroonquest.app.domain.repository.CharacterCatalog
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.HeaderBar
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.RegionBadge
import com.cameroonquest.app.ui.components.WaveBand
import com.cameroonquest.app.ui.components.fadeEdges
import com.cameroonquest.app.ui.style.CharacterStyles
import com.cameroonquest.app.ui.style.QuizPictures
import com.cameroonquest.app.ui.style.RegionLabels
import com.cameroonquest.app.ui.style.localized
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import com.cameroonquest.app.ui.theme.darken
import com.cameroonquest.app.ui.theme.lighten
import com.cameroonquest.app.ui.viewmodel.QuizViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class Choice { OPEN, PICKED_RIGHT, PICKED_WRONG, REVEALED, DIMMED }

@Composable
fun QuizScreen(viewModel: QuizViewModel, catalog: CharacterCatalog, onExit: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    LaunchedEffect(state.round) { scroll.scrollTo(0) }

    GameRoundScaffold(
        viewModel = viewModel,
        onExit = onExit,
        resultContent = { finished ->
            val outcome = finished.result.outcome
            ResultContent(
                title = resultTitle(outcome, PlayMode.SOLO, emptyList()),
                description = if (outcome == Outcome.WIN) {
                    stringResource(R.string.quiz_win_description)
                } else {
                    stringResource(R.string.quiz_loss_description, state.winThreshold)
                },
                lines = listOf(
                    ScoreLine(stringResource(R.string.correct_label), stringResource(R.string.fraction, state.correctAnswers, state.total)),
                    ScoreLine(stringResource(R.string.score), "${finished.result.score}"),
                ),
            )
        },
    ) { onBack, _ ->
        val round = state.round
        val question = round.question
        val answered = state.selected
        Column(
            Modifier
                .fillMaxSize()
                .background(Quest.Parchment),
        ) {
            HeaderBar(
                stringResource(R.string.cultural_question),
                onBack = onBack,
                trailing = {
                    Text(
                        stringResource(R.string.fraction, state.number, state.total),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        style = ShadowedText,
                    )
                },
            )
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .shadow(6.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(scroll)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QuizPicture(question, catalog)
                    Text(question.question.localized(), color = Quest.Ink, fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold)
                    round.options.forEachIndexed { i, option ->
                        val choice = when {
                            answered == null -> Choice.OPEN
                            i == answered && i == round.correct -> Choice.PICKED_RIGHT
                            i == answered -> Choice.PICKED_WRONG
                            i == round.correct -> Choice.REVEALED
                            else -> Choice.DIMMED
                        }
                        OptionRow('A' + i, option.localized(), choice) { viewModel.answer(i) }
                    }
                    if (answered != null) {
                        val right = answered == round.correct
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (right) Color(0xFFE6F6EA) else Color(0xFFFDECEA))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                if (right) Icons.Rounded.CheckCircle else Icons.Rounded.Info,
                                contentDescription = null,
                                tint = if (right) Quest.Correct else Quest.Wrong,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(if (right) R.string.correct_feedback else R.string.wrong_feedback, question.fact.localized()),
                                color = Quest.Ink,
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                            )
                        }
                    }
                }
                QuestButton(
                    stringResource(if (state.isLast) R.string.finish else R.string.next),
                    { viewModel.next() },
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                    top = Quest.GreenTop,
                    bottom = Quest.Green,
                    height = 52.dp,
                    fontSize = 22.sp,
                    enabled = answered != null,
                )
            }
            BottomKente()
        }
    }
}

@Composable
private fun OptionRow(letter: Char, text: String, choice: Choice, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val background = when (choice) {
        Choice.PICKED_RIGHT -> Brush.verticalGradient(listOf(Color(0xFF34C25A), Quest.Correct.darken(0.1f)))
        Choice.PICKED_WRONG -> Brush.verticalGradient(listOf(Color(0xFFE76A60), Quest.Wrong))
        Choice.REVEALED -> Brush.verticalGradient(listOf(Color(0xFFE6F6EA), Color(0xFFD5F0DC)))
        else -> Brush.verticalGradient(listOf(Color.White, Color(0xFFF9F6F0)))
    }
    val border = when (choice) {
        Choice.PICKED_RIGHT, Choice.REVEALED -> Quest.Correct
        Choice.PICKED_WRONG -> Quest.Wrong
        else -> Quest.Line
    }
    val ink = when (choice) {
        Choice.PICKED_RIGHT, Choice.PICKED_WRONG -> Color.White
        Choice.DIMMED -> Quest.InkSoft.copy(alpha = 0.6f)
        else -> Quest.Ink
    }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .clip(shape)
            .background(background)
            .border(2.dp, border, shape)
            .clickable(enabled = choice == Choice.OPEN, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$letter.", color = ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Text(text, Modifier.weight(1f), color = ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        if (choice == Choice.PICKED_RIGHT || choice == Choice.REVEALED || choice == Choice.PICKED_WRONG) {
            val good = choice != Choice.PICKED_WRONG
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (choice == Choice.REVEALED) Quest.Correct else Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (good) Icons.Rounded.Check else Icons.Rounded.Close,
                    contentDescription = stringResource(if (good) R.string.correct_answer else R.string.wrong_answer),
                    tint = when (choice) {
                        Choice.REVEALED -> Color.White
                        Choice.PICKED_RIGHT -> Quest.Correct
                        else -> Quest.Wrong
                    },
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun QuizPicture(question: QuizQuestion, catalog: CharacterCatalog) {
    val shape = RoundedCornerShape(16.dp)
    val frame = Modifier
        .fillMaxWidth()
        .aspectRatio(1.9f)
        .clip(shape)
    val character = catalog.find(question.characterId)
    val picture = QuizPictures.of(question.pictureKey)
    when {
        picture != null -> Image(painterResource(picture), stringResource(R.string.quiz_picture), frame, contentScale = ContentScale.Crop)
        character != null -> {
            val style = CharacterStyles.of(character)
            Box(frame.background(Brush.linearGradient(listOf(style.color.lighten(0.25f), style.color.darken(0.35f))))) {
                WaveBand(
                    style.color,
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.42f),
                )
                Image(
                    painterResource(style.portrait),
                    contentDescription = character.name,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxHeight()
                        .aspectRatio(1.12f)
                        .fadeEdges(toBottom = true, from = 0.78f),
                    contentScale = ContentScale.Crop,
                )
                Column(
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp),
                ) {
                    RegionBadge(style, 44.dp)
                    Text(
                        stringResource(RegionLabels.of(character.region)),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        style = ShadowedText,
                    )
                    Text(
                        stringResource(R.string.region_of, character.name),
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 14.sp,
                        style = ShadowedText,
                    )
                }
            }
        }
        else -> Canvas(frame) {
            val third = size.width / 3
            drawRect(Color(0xFF007A5E), size = Size(third, size.height))
            drawRect(Color(0xFFCE1126), topLeft = Offset(third, 0f), size = Size(third, size.height))
            drawRect(Color(0xFFFCD116), topLeft = Offset(third * 2, 0f), size = Size(third, size.height))
            drawPath(star(center, size.height * 0.2f), Color(0xFFFCD116))
        }
    }
}

private fun star(center: Offset, radius: Float): Path {
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else radius * 0.382f
        val angle = -PI / 2 + i * PI / 5
        val x = center.x + r * cos(angle).toFloat()
        val y = center.y + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
