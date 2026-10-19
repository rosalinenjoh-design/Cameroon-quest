package com.cameroonquest.app.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.R
import com.cameroonquest.app.data.Difficulty
import com.cameroonquest.app.data.GameKind
import com.cameroonquest.app.data.PlayMode
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.HeaderBar
import com.cameroonquest.app.ui.components.JungleBackground
import com.cameroonquest.app.ui.components.rememberHaptics
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText

@Composable
fun GameSelectScreen(onBack: () -> Unit, onStart: (GameKind, Difficulty, PlayMode) -> Unit) {
    var setup by remember { mutableStateOf<GameKind?>(null) }
    Box(Modifier.fillMaxSize()) {
        JungleBackground(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize()) {
            HeaderBar("Game Selection", onBack, subtitle = "Choose a traditional game to play", brush = null)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GameCard(R.drawable.game_songo, "Songo, Beti and Centre") { setup = GameKind.SONGO }
                GameCard(R.drawable.game_pirogue, "Pirogue Racing, Sawa and Littoral") { setup = GameKind.PIROGUE }
                GameCard(R.drawable.game_dochi, "Dochi, Grassfields") { setup = GameKind.DOCHI }
                GameCard(R.drawable.quiz_grassfields, "Cultural Quiz", overlayTitle = "Cultural Quiz", overlaySubtitle = "(10 regions)") {
                    setup = GameKind.QUIZ
                }
            }
            BottomKente()
        }
    }
    setup?.let { game ->
        GameSetupDialog(
            game,
            onDismiss = { setup = null },
            onStart = { difficulty, mode ->
                setup = null
                onStart(game, difficulty, mode)
            },
        )
    }
}

/** The Songo, Pirogue and Dochi pictures already contain their painted titles from the mockup. */
@Composable
private fun GameCard(
    image: Int,
    description: String,
    overlayTitle: String? = null,
    overlaySubtitle: String? = null,
    onClick: () -> Unit,
) {
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
            .clickable(interaction, indication = null, onClickLabel = "Play $description", role = Role.Button) {
                haptics()
                onClick()
            },
    ) {
        Image(
            painterResource(image),
            contentDescription = description,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (overlayTitle != null) {
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
                    Text(overlayTitle, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, style = ShadowedText)
                    if (overlaySubtitle != null) {
                        Text(overlaySubtitle, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = ShadowedText)
                    }
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
