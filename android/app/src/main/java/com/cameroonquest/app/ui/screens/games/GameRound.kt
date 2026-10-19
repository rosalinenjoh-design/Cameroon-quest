package com.cameroonquest.app.ui.screens.games

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.domain.model.RoundReward
import com.cameroonquest.app.ui.components.Avatar
import com.cameroonquest.app.ui.components.CircleIconButton
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.QuestDialog
import com.cameroonquest.app.ui.components.rememberHaptics
import com.cameroonquest.app.ui.style.BadgeStyles
import com.cameroonquest.app.ui.style.GameStyles
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import com.cameroonquest.app.ui.theme.darken
import com.cameroonquest.app.ui.theme.lighten
import com.cameroonquest.app.ui.viewmodel.GameViewModel
import com.cameroonquest.app.ui.viewmodel.RealTimeGameViewModel
import com.cameroonquest.app.ui.viewmodel.RoundPhase
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive

data class ScoreLine(val label: String, val value: String)

data class ResultContent(val title: String, val description: String, val lines: List<ScoreLine>)

/**
 * Shared frame of every game: back button, pausing when the app goes to the background,
 * and the start, pause, leave and result dialogs. Each game only draws its own board in [content].
 */
@Composable
fun GameRoundScaffold(
    viewModel: GameViewModel<*>,
    onExit: () -> Unit,
    resultContent: @Composable (RoundPhase.Finished) -> ResultContent,
    startTitle: String? = null,
    startNote: String? = null,
    content: @Composable (onBack: () -> Unit, onPause: () -> Unit) -> Unit,
) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    var leaving by rememberSaveable { mutableStateOf(false) }
    val kind = viewModel.setup.kind
    VibrateOnGameEvents(viewModel.events)
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.pause() }

    val requestLeave: () -> Unit = {
        when (viewModel.phase.value) {
            RoundPhase.Playing, RoundPhase.Paused -> {
                viewModel.pause()
                leaving = true
            }
            else -> onExit()
        }
    }
    BackHandler(enabled = phase == RoundPhase.Playing || phase == RoundPhase.Paused) { requestLeave() }

    content(requestLeave) { viewModel.pause() }

    val current = phase
    when {
        current is RoundPhase.Finished -> {
            val text = resultContent(current)
            ResultDialog(
                outcome = current.result.outcome,
                content = text,
                reward = current.reward,
                onPlayAgain = { viewModel.restart() },
                onMenu = onExit,
            )
        }
        leaving -> LeaveRoundDialog(
            onStay = {
                leaving = false
                viewModel.resume()
            },
            onLeave = onExit,
        )
        current == RoundPhase.Ready && startTitle != null -> StartDialog(
            title = startTitle,
            text = listOfNotNull(stringResource(GameStyles.of(kind).rules), startNote).joinToString("\n\n"),
            onStart = { viewModel.start() },
            onExit = onExit,
        )
        current == RoundPhase.Paused -> PauseDialog(
            kind = kind,
            onResume = { viewModel.resume() },
            onRestart = { viewModel.restart() },
            onQuit = onExit,
        )
    }
}

/** Observer: vibrates on the events the player cares about. */
@Composable
fun VibrateOnGameEvents(events: SharedFlow<GameEvent>) {
    val haptics = rememberHaptics()
    LaunchedEffect(events, haptics) {
        events.collect { event ->
            val important = when (event) {
                is GameEvent.LogHit -> event.racer == 0
                GameEvent.RoundOver -> false
                else -> true
            }
            if (important) haptics()
        }
    }
}

/** Sends every display frame to a real-time game while it is being played. */
@Composable
fun FrameLoop(viewModel: RealTimeGameViewModel<*>) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    if (phase.acceptsMoves) {
        LaunchedEffect(viewModel) {
            var last = withFrameNanos { it }
            while (isActive) {
                val now = withFrameNanos { it }
                viewModel.onFrame((now - last) / NANOS_PER_SECOND)
                last = now
            }
        }
    }
}

private const val NANOS_PER_SECOND = 1_000_000_000.0

@Composable
fun GameHeader(
    title: String,
    level: Int?,
    brush: Brush,
    onBack: () -> Unit,
    onPause: (() -> Unit)?,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(brush)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        CircleIconButton(
            Icons.AutoMirrored.Rounded.ArrowBack,
            stringResource(R.string.back),
            onBack,
            Modifier.align(Alignment.CenterStart),
        )
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, style = ShadowedText, maxLines = 1)
            if (level != null) {
                Text(
                    stringResource(R.string.level_number, level),
                    color = Color(0xFFFCE7C8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (onPause != null) {
            CircleIconButton(
                Icons.Rounded.Pause,
                stringResource(R.string.pause),
                onPause,
                Modifier.align(Alignment.CenterEnd),
                background = Quest.Yellow,
                tint = Quest.YellowInk,
                border = Color.White,
            )
        }
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

@Composable
fun StartDialog(title: String, text: String, onStart: () -> Unit, onExit: () -> Unit) {
    QuestDialog(onExit, dismissible = false) {
        Text(title, color = Quest.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(text, color = Quest.Ink, fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
        QuestButton(stringResource(R.string.start), onStart, Modifier.fillMaxWidth(), height = 54.dp)
        TextButton(onClick = onExit) { Text(stringResource(R.string.back_to_games), color = Quest.InkSoft, fontSize = 16.sp) }
    }
}

@Composable
fun PauseDialog(kind: GameKind, onResume: () -> Unit, onRestart: () -> Unit, onQuit: () -> Unit) {
    var showRules by remember { mutableStateOf(false) }
    QuestDialog(onResume) {
        Text(stringResource(R.string.paused), color = Quest.Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        QuestButton(stringResource(R.string.resume), onResume, Modifier.fillMaxWidth(), height = 52.dp, fontSize = 21.sp)
        QuestButton(
            stringResource(R.string.restart), onRestart, Modifier.fillMaxWidth(),
            top = Quest.BlueTop, bottom = Quest.BlueDark, height = 52.dp, fontSize = 21.sp,
        )
        QuestButton(
            stringResource(R.string.quit), onQuit, Modifier.fillMaxWidth(),
            top = Color(0xFFE5564B), bottom = Color(0xFFA7231B), height = 52.dp, fontSize = 21.sp,
        )
        TextButton(onClick = { showRules = !showRules }) {
            Text(
                stringResource(if (showRules) R.string.hide_rules else R.string.how_to_play),
                color = Quest.Teal,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (showRules) Text(stringResource(GameStyles.of(kind).rules), color = Quest.Ink, fontSize = 14.sp, lineHeight = 20.sp)
        Text(stringResource(R.string.unfinished_not_scored), color = Quest.InkSoft, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun LeaveRoundDialog(onStay: () -> Unit, onLeave: () -> Unit) {
    QuestDialog(onStay) {
        Text(stringResource(R.string.leave_title), color = Quest.Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(stringResource(R.string.leave_message), color = Quest.InkSoft, fontSize = 15.sp, textAlign = TextAlign.Center)
        QuestButton(stringResource(R.string.keep_playing), onStay, Modifier.fillMaxWidth(), height = 52.dp, fontSize = 21.sp)
        TextButton(onClick = onLeave) {
            Text(stringResource(R.string.leave_round), color = Quest.Wrong, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ResultDialog(
    outcome: Outcome,
    content: ResultContent,
    reward: RoundReward,
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
        Text(content.title, color = Quest.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(content.description, color = Quest.InkSoft, fontSize = 15.sp, textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            content.lines.forEach { line ->
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.5.dp, Quest.Line, RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(line.label, color = Quest.InkSoft, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(line.value, color = Quest.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(Quest.Green)
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Text(stringResource(R.string.reward_line, reward.xp, reward.score), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        if (reward.newBest && reward.score > 0) {
            Text(stringResource(R.string.new_best), color = Quest.GreenDark, fontWeight = FontWeight.Bold)
        }
        reward.newLevel?.let { Text(stringResource(R.string.level_up, it), color = Quest.Teal, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        reward.newBadges.forEach { badge ->
            Text(
                stringResource(R.string.badge_unlocked, stringResource(BadgeStyles.of(badge).title)),
                color = Quest.YellowDark,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        QuestButton(stringResource(R.string.play_again), onPlayAgain, Modifier.fillMaxWidth(), height = 52.dp, fontSize = 21.sp)
        TextButton(onClick = onMenu) {
            Text(stringResource(R.string.back_to_games), color = Quest.Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Title of the result dialog; in two-player rounds it names the winner. */
@Composable
fun resultTitle(outcome: Outcome, mode: PlayMode, names: List<String>): String = when {
    mode == PlayMode.LOCAL && outcome == Outcome.WIN -> stringResource(R.string.result_player_wins, names[0])
    mode == PlayMode.LOCAL && outcome == Outcome.LOSS -> stringResource(R.string.result_player_wins, names[1])
    outcome == Outcome.WIN -> stringResource(R.string.result_win)
    outcome == Outcome.DRAW -> stringResource(R.string.result_draw)
    else -> stringResource(R.string.result_loss)
}
