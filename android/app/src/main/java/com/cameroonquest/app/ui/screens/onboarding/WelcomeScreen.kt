package com.cameroonquest.app.ui.screens.onboarding

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cameroonquest.app.R
import com.cameroonquest.app.ui.components.BottomKente
import com.cameroonquest.app.ui.components.QuestButton
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "welcome").animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(Modifier.fillMaxSize().background(Quest.JungleDark)) {
        Image(
            painterResource(R.drawable.welcome_bg),
            contentDescription = stringResource(R.string.welcome_art_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        0.72f to Quest.JungleDark.copy(alpha = 0.55f),
                        1f to Quest.JungleDark.copy(alpha = 0.95f),
                    ),
                ),
        )
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                stringResource(R.string.welcome_tagline),
                modifier = Modifier.padding(horizontal = 32.dp),
                color = Color.White,
                fontSize = 19.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                style = ShadowedText,
            )
            Spacer(Modifier.height(30.dp))
            QuestButton(
                stringResource(R.string.get_started),
                onStart,
                Modifier
                    .fillMaxWidth(0.72f)
                    .scale(pulse),
                top = Quest.YellowTop,
                bottom = Quest.YellowDark,
                textColor = Quest.YellowInk,
            )
            Spacer(Modifier.height(40.dp))
            BottomKente()
        }
    }
}
