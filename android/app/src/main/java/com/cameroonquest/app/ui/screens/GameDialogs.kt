package com.cameroonquest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.R
import com.cameroonquest.app.data.Difficulty
import com.cameroonquest.app.data.GameKind
import com.cameroonquest.app.data.Outcome
import com.cameroonquest.app.data.PlayMode
import com.cameroonquest.app.data.RoundReward
import com.cameroonquest.app.ui.components.ChoiceChip
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.QuestDialog
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.darken

object GameTexts {
    fun description(game: GameKind) = when (game) {
        GameKind.SONGO -> "A game of patience and strategy. Sow seeds and capture more than your opponent."
        GameKind.PIROGUE -> "Race a dugout canoe on the river. Steer around logs: each collision slows you down."
        GameKind.DOCHI -> "Dodge the balls thrown from both sides. Survive until the timer runs out to win."
        GameKind.QUIZ -> "Ten questions about Cameroon's regions, food, music and landmarks."
    }

    fun rules(game: GameKind) = when (game) {
        GameKind.SONGO ->
            "Each side has five pits with five seeds. Tap one of your pits to sow its seeds one by one: up your column, " +
                "then down the other one. If your last seed lands in an opposing pit that now holds 2 or 3 seeds, you capture " +
                "them, plus the opposing pits just before it that also hold 2 or 3. You may not capture every seed on the other " +
                "side, and you must feed an empty side when you can. More than 25 captured seeds wins. The round also ends when " +
                "no move is possible, a position repeats three times, or after 200 turns; each side then keeps its own seeds."
        GameKind.PIROGUE ->
            "Both canoes paddle by themselves at the same speed and race on the same river. Hold the arrow buttons to steer " +
                "around the floating logs. Each log you hit slows you down for a moment. The first canoe across the line wins."
        GameKind.DOCHI ->
            "Two throwers aim balls at you from the left and the right. Move with the joystick and stay alive until the timer " +
                "ends: 30 seconds on level 1, 40 on level 2 and 45 on level 3. Your score is the time you survive."
        GameKind.QUIZ ->
            "Answer ten questions. Each correct answer earns 50 points and 10 XP. Get at least 6 right to win the round."
    }
}

@Composable
fun GameSetupDialog(game: GameKind, onDismiss: () -> Unit, onStart: (Difficulty, PlayMode) -> Unit) {
    var difficulty by remember { mutableStateOf(Difficulty.EASY) }
    var mode by remember { mutableStateOf(PlayMode.SOLO) }
    QuestDialog(onDismiss) {
        Text(game.title, color = Quest.Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(game.origin, color = Quest.InkSoft, fontSize = 15.sp)
        Text(GameTexts.description(game), color = Quest.Ink, fontSize = 15.sp, textAlign = TextAlign.Center)
        if (game != GameKind.QUIZ) {
            Text("Level", Modifier.fillMaxWidth(), color = Quest.Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { level ->
                    ChoiceChip("${level.level} \u00B7 ${level.label}", level == difficulty, { difficulty = level }, Modifier.weight(1f))
                }
            }
        }
        if (game == GameKind.SONGO || game == GameKind.PIROGUE) {
            Text("Players", Modifier.fillMaxWidth(), color = Quest.Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip("vs Computer", mode == PlayMode.SOLO, { mode = PlayMode.SOLO }, Modifier.weight(1f))
                ChoiceChip("2 Players", mode == PlayMode.LOCAL, { mode = PlayMode.LOCAL }, Modifier.weight(1f))
            }
            if (mode == PlayMode.LOCAL) {
                Text(
                    "Two players share this phone. Only your progress is saved.",
                    color = Quest.InkSoft,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
        QuestButton("Start", { onStart(difficulty, mode) }, Modifier.fillMaxWidth().padding(top = 6.dp), height = 54.dp)
        TextButton(onClick = onDismiss) { Text("Cancel", color = Quest.InkSoft, fontSize = 16.sp) }
    }
}

@Composable
fun PauseDialog(game: GameKind, onResume: () -> Unit, onRestart: () -> Unit, onQuit: () -> Unit) {
    var showRules by remember { mutableStateOf(false) }
    QuestDialog(onResume) {
        Text("Paused", color = Quest.Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        QuestButton("Resume", onResume, Modifier.fillMaxWidth(), height = 52.dp, fontSize = 21.sp)
        QuestButton(
            "Restart", onRestart, Modifier.fillMaxWidth(),
            top = Quest.BlueTop, bottom = Quest.BlueDark, height = 52.dp, fontSize = 21.sp,
        )
        QuestButton(
            "Quit", onQuit, Modifier.fillMaxWidth(),
            top = Color(0xFFE5564B), bottom = Color(0xFFA7231B), height = 52.dp, fontSize = 21.sp,
        )
        TextButton(onClick = { showRules = !showRules }) {
            Text(if (showRules) "Hide rules" else "How to play", color = Quest.Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        if (showRules) Text(GameTexts.rules(game), color = Quest.Ink, fontSize = 14.sp, lineHeight = 20.sp)
        Text("Unfinished rounds are not scored.", color = Quest.InkSoft, fontSize = 13.sp)
    }
}

@Composable
fun LeaveRoundDialog(onStay: () -> Unit, onLeave: () -> Unit) {
    QuestDialog(onStay) {
        Text("Leave this round?", color = Quest.Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("This unfinished round will not be scored.", color = Quest.InkSoft, fontSize = 15.sp, textAlign = TextAlign.Center)
        QuestButton("Keep playing", onStay, Modifier.fillMaxWidth(), height = 52.dp, fontSize = 21.sp)
        TextButton(onClick = onLeave) { Text("Leave round", color = Quest.Wrong, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
    }
}

data class ScoreLine(val label: String, val value: String)

@Composable
fun ResultDialog(
    outcome: Outcome,
    title: String,
    description: String,
    lines: List<ScoreLine>,
    reward: RoundReward?,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
) {
    val (icon, color) = when (outcome) {
        Outcome.WIN -> Icons.Rounded.EmojiEvents to Quest.Gold
        Outcome.DRAW -> Icons.Rounded.Star to Quest.BlueTop
        Outcome.LOSS -> Icons.Rounded.Eco to Quest.GreenTop
    }
    QuestDialog(onMenu, dismissible = false) {
        Box(
            Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(color, color.darken(0.3f))))
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp))
        }
        Text(title, color = Quest.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(description, color = Quest.InkSoft, fontSize = 15.sp, textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            lines.forEach { line ->
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.5.dp, Quest.Line, RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(line.label, color = Quest.InkSoft, fontSize = 13.sp, maxLines = 1)
                    Text(line.value, color = Quest.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
        if (reward == null) {
            Text("Saving your progress\u2026", color = Quest.InkSoft, fontSize = 14.sp)
        } else {
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Quest.Green)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text("+${reward.xp} XP \u00B7 +${reward.score} points", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            if (reward.newBest && reward.score > 0) Text("New best score!", color = Quest.GreenDark, fontWeight = FontWeight.Bold)
            reward.newLevel?.let { Text("Level up! You are now Level $it.", color = Quest.Teal, fontWeight = FontWeight.Bold) }
            reward.newBadges.forEach { Text("Badge unlocked: ${it.title}", color = Quest.YellowDark, fontWeight = FontWeight.Bold) }
        }
        QuestButton("Play again", onPlayAgain, Modifier.fillMaxWidth(), height = 52.dp, fontSize = 21.sp, enabled = reward != null)
        TextButton(onClick = onMenu, enabled = reward != null) {
            Text("Back to games", color = Quest.Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun outcomeOf(winner: Int?): Outcome = when (winner) {
    0 -> Outcome.WIN
    null -> Outcome.DRAW
    else -> Outcome.LOSS
}

fun gameImage(game: GameKind): Int = when (game) {
    GameKind.SONGO -> R.drawable.game_songo
    GameKind.PIROGUE -> R.drawable.game_pirogue
    GameKind.DOCHI -> R.drawable.game_dochi
    GameKind.QUIZ -> R.drawable.quiz_grassfields
}

fun resultTitle(outcome: Outcome, mode: PlayMode, names: List<String>): String = when {
    mode == PlayMode.LOCAL && outcome == Outcome.WIN -> "${names[0]} wins!"
    mode == PlayMode.LOCAL && outcome == Outcome.LOSS -> "${names[1]} wins!"
    outcome == Outcome.WIN -> "Well played!"
    outcome == Outcome.DRAW -> "An even match!"
    else -> "Keep exploring!"
}
