package com.cameroonquest.app.ui.screens.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.ChoiceChip
import com.cameroonquest.app.ui.components.HeaderBar
import com.cameroonquest.app.ui.components.JungleBackground
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.components.QuestDialog
import com.cameroonquest.app.ui.components.rememberHaptics
import com.cameroonquest.app.ui.style.DifficultyLabels
import com.cameroonquest.app.ui.style.GameStyles
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText

@Composable
fun GameSelectScreen(onBack: () -> Unit, onStart: (GameSetup) -> Unit) {
    var chosen by rememberSaveable { mutableStateOf<GameKind?>(null) }
    Box(Modifier.fillMaxSize()) {
        JungleBackground(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize()) {
            HeaderBar(
                stringResource(R.string.game_selection),
                onBack,
                subtitle = stringResource(R.string.game_selection_subtitle),
                brush = null,
            )
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GameKind.entries.forEach { kind ->
                    GameCard(kind) { chosen = kind }
                }
            }
            BottomKente()
        }
    }
    chosen?.let { kind ->
        GameSetupDialog(
            kind,
            onDismiss = { chosen = null },
            onStart = { setup ->
                chosen = null
                onStart(setup)
            },
        )
    }
}

/** The Songo, Pirogue and Dochi pictures already contain their painted titles from the mockup; the quiz card adds its own. */
@Composable
private fun GameCard(kind: GameKind, onClick: () -> Unit) {
    val style = GameStyles.of(kind)
    val title = stringResource(style.title)
    val shape = RoundedCornerShape(20.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "game card")
    val haptics = rememberHaptics()
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(2.64f)
            .scale(scale)
            .shadow(10.dp, shape)
            .clip(shape)
            .border(3.dp, Quest.CreamDeep, shape)
            .clickable(interaction, indication = null, onClickLabel = stringResource(R.string.play_named, title), role = Role.Button) {
                haptics()
                onClick()
            },
    ) {
        Image(
            painterResource(style.image),
            contentDescription = "$title, ${stringResource(style.origin)}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (kind == GameKind.QUIZ) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Brush.horizontalGradient(0.3f to Color.Transparent, 1f to Color(0xB31C2A10))),
            )
            Row(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, style = ShadowedText)
                    Text(
                        stringResource(R.string.quiz_card_subtitle),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        style = ShadowedText,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Quest.Teal)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun GameSetupDialog(kind: GameKind, onDismiss: () -> Unit, onStart: (GameSetup) -> Unit) {
    val style = GameStyles.of(kind)
    var difficulty by rememberSaveable { mutableStateOf(Difficulty.EASY) }
    var mode by rememberSaveable { mutableStateOf(PlayMode.SOLO) }
    QuestDialog(onDismiss) {
        Text(stringResource(style.title), color = Quest.Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(stringResource(style.origin), color = Quest.InkSoft, fontSize = 15.sp)
        Text(stringResource(style.description), color = Quest.Ink, fontSize = 15.sp, textAlign = TextAlign.Center)
        if (kind.hasLevels) {
            Text(stringResource(R.string.level_label), Modifier.fillMaxWidth(), color = Quest.Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { level ->
                    ChoiceChip(
                        stringResource(DifficultyLabels.of(level)),
                        level == difficulty,
                        { difficulty = level },
                        Modifier.weight(1f),
                    )
                }
            }
        }
        if (kind.allowsTwoPlayers) {
            Text(stringResource(R.string.players_label), Modifier.fillMaxWidth(), color = Quest.Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(stringResource(R.string.vs_computer), mode == PlayMode.SOLO, { mode = PlayMode.SOLO }, Modifier.weight(1f))
                ChoiceChip(stringResource(R.string.two_players), mode == PlayMode.LOCAL, { mode = PlayMode.LOCAL }, Modifier.weight(1f))
            }
            if (mode == PlayMode.LOCAL) {
                Text(stringResource(R.string.two_players_note), color = Quest.InkSoft, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        QuestButton(
            stringResource(R.string.start),
            { onStart(GameSetup(kind, difficulty, if (kind.allowsTwoPlayers) mode else PlayMode.SOLO)) },
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            height = 54.dp,
        )
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Quest.InkSoft, fontSize = 16.sp) }
    }
}
