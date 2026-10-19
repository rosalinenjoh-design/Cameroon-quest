package com.cameroonquest.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.data.Characters
import com.cameroonquest.app.data.Outcome
import com.cameroonquest.app.data.PlayMode
import com.cameroonquest.app.data.PlayerProfile
import com.cameroonquest.app.data.Questions
import com.cameroonquest.app.data.QuizQuestion
import com.cameroonquest.app.data.RoundReward
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.HeaderBar
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.RegionBadge
import com.cameroonquest.app.ui.components.WaveBand
import com.cameroonquest.app.ui.components.fadeEdges
import com.cameroonquest.app.ui.components.rememberHaptics
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import com.cameroonquest.app.ui.theme.darken
import com.cameroonquest.app.ui.theme.lighten
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val WIN_THRESHOLD = 6

private enum class Choice { OPEN, PICKED_RIGHT, PICKED_WRONG, REVEALED, DIMMED }

@Composable
fun QuizScreen(profile: PlayerProfile, onRecord: (score: Int, outcome: Outcome) -> RoundReward, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val questions = remember(round) { Questions.pickRound() }
    var index by remember(round) { mutableIntStateOf(0) }
    var chosen by remember(round) { mutableStateOf<Int?>(null) }
    var correct by remember(round) { mutableIntStateOf(0) }
    var finished by remember(round) { mutableStateOf(false) }
    var reward by remember(round) { mutableStateOf<RoundReward?>(null) }
    var leaving by remember { mutableStateOf(false) }
    val haptics = rememberHaptics()
    val question = questions[index]
    val answered = chosen
    val scroll = rememberScrollState()

    LaunchedEffect(round, index) { scroll.scrollTo(0) }
    BackHandler(enabled = !finished) { leaving = true }

    Column(
        Modifier
            .fillMaxSize()
            .background(Quest.Parchment),
    ) {
        HeaderBar(
            "Cultural Question",
            onBack = { if (finished) onExit() else leaving = true },
            trailing = {
                Text("${index + 1}/${questions.size}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, style = ShadowedText)
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
                QuizPicture(question.source)
                Text(question.source.question, color = Quest.Ink, fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold)
                question.options.forEachIndexed { i, option ->
                    val choice = when {
                        answered == null -> Choice.OPEN
                        i == answered && i == question.correct -> Choice.PICKED_RIGHT
                        i == answered -> Choice.PICKED_WRONG
                        i == question.correct -> Choice.REVEALED
                        else -> Choice.DIMMED
                    }
                    OptionRow('A' + i, option, choice) {
                        if (chosen == null) {
                            chosen = i
                            if (i == question.correct) correct++
                            haptics()
                        }
                    }
                }
                if (answered != null) {
                    val right = answered == question.correct
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
                            (if (right) "Correct! " else "Not quite. ") + question.source.fact,
                            color = Quest.Ink,
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
                        )
                    }
                }
            }
            QuestButton(
                if (index == questions.lastIndex) "Finish" else "Next",
                {
                    if (index == questions.lastIndex) {
                        finished = true
                        reward = onRecord(correct * 50, if (correct >= WIN_THRESHOLD) Outcome.WIN else Outcome.LOSS)
                    } else {
                        index++
                        chosen = null
                    }
                },
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

    if (finished) {
        val outcome = if (correct >= WIN_THRESHOLD) Outcome.WIN else Outcome.LOSS
        ResultDialog(
            outcome = outcome,
            title = resultTitle(outcome, PlayMode.SOLO, listOf(profile.name, "")),
            description = if (outcome == Outcome.WIN) {
                "Great knowledge of Cameroon! Every correct answer earns 10 XP."
            } else {
                "Get $WIN_THRESHOLD or more right to win. Every correct answer still earns 10 XP."
            },
            lines = listOf(ScoreLine("Correct", "$correct / ${questions.size}"), ScoreLine("Score", "${correct * 50}")),
            reward = reward,
            onPlayAgain = { round++ },
            onMenu = onExit,
        )
    } else if (leaving) {
        LeaveRoundDialog(onStay = { leaving = false }, onLeave = onExit)
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
                    contentDescription = if (good) "Correct answer" else "Wrong answer",
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
private fun QuizPicture(question: QuizQuestion) {
    val shape = RoundedCornerShape(16.dp)
    val frame = Modifier
        .fillMaxWidth()
        .aspectRatio(1.9f)
        .clip(shape)
    val character = Characters.byId(question.characterId)
    val picture = question.picture
    when {
        picture != null -> Image(painterResource(picture), "Illustration for the question", frame, contentScale = ContentScale.Crop)
        character != null -> Box(frame.background(Brush.linearGradient(listOf(character.color.lighten(0.25f), character.color.darken(0.35f))))) {
            WaveBand(
                character.color,
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.42f),
            )
            Image(
                painterResource(character.portrait),
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
                RegionBadge(character, 44.dp)
                Text(character.region, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, style = ShadowedText)
                Text("${character.name}'s region", color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp, style = ShadowedText)
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
