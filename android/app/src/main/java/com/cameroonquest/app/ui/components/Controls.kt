package com.cameroonquest.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cameroonquest.app.data.QuestCharacter
import com.cameroonquest.app.ui.theme.Quest
import com.cameroonquest.app.ui.theme.ShadowedText
import com.cameroonquest.app.ui.theme.darken
import com.cameroonquest.app.ui.theme.lighten
import kotlin.math.roundToInt

val LocalHapticsEnabled = staticCompositionLocalOf { true }

@Composable
fun rememberHaptics(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    val enabled = LocalHapticsEnabled.current
    return remember(haptic, enabled) { { if (enabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress) } }
}

/** Glossy 3D pill button like "Play", "Get Started" and "Select" in the mockup. */
@Composable
fun QuestButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    top: Color = Quest.GreenTop,
    bottom: Color = Quest.GreenDark,
    textColor: Color = Color.White,
    icon: ImageVector? = null,
    height: Dp = 60.dp,
    fontSize: TextUnit = 24.sp,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed && enabled) 1f else 0f, label = "press")
    val haptics = rememberHaptics()
    val shape = RoundedCornerShape(50)
    val depth = 5.dp
    Box(
        modifier
            .height(height + depth)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button) {
                haptics()
                onClick()
            },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .align(Alignment.BottomCenter)
                .clip(shape)
                .background(bottom.darken(0.4f)),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .offset { IntOffset(0, (depth.toPx() * press).roundToInt()) }
                .clip(shape)
                .background(Brush.verticalGradient(listOf(top, bottom)))
                .border(2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0.15f))), shape),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.46f)
                    .padding(horizontal = height * 0.35f, vertical = 3.dp)
                    .clip(shape)
                    .background(Color.White.copy(alpha = 0.22f)),
            )
            Row(Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                val bubble = height - 14.dp
                if (icon != null) {
                    Box(
                        Modifier
                            .size(bubble)
                            .clip(CircleShape)
                            .background(bottom.darken(0.2f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(bubble * 0.62f))
                    }
                }
                Text(
                    text,
                    Modifier.weight(1f),
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = if (textColor == Color.White) ShadowedText else LocalTextStyle.current,
                )
                if (icon != null) Spacer(Modifier.width(bubble))
            }
        }
    }
}

/** Green rounded-square shortcut (Profile / Settings / Help on the home screen). */
@Composable
fun SquareIconButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val haptics = rememberHaptics()
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .size(width = 84.dp, height = 78.dp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF22A577), Color(0xFF0D6B49))))
            .border(2.dp, Color.White.copy(alpha = 0.6f), shape)
            .clickable(role = Role.Button) {
                haptics()
                onClick()
            }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Color.White.copy(alpha = 0.2f),
    tint: Color = Color.White,
    size: Dp = 42.dp,
    border: Color? = Color.White.copy(alpha = 0.5f),
) {
    val haptics = rememberHaptics()
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .then(if (border != null) Modifier.border(1.5.dp, border, CircleShape) else Modifier)
            .clickable(onClickLabel = contentDescription, role = Role.Button) {
                haptics()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size * 0.58f))
    }
}

/** Teal header with back button and centred title (Game Selection, Quiz, Profile, Leaderboard). */
@Composable
fun HeaderBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    brush: Brush? = Brush.verticalGradient(listOf(Quest.Teal, Quest.TealDark)),
    trailing: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier
            .fillMaxWidth()
            .then(if (brush != null) Modifier.background(brush) else Modifier)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .heightIn(min = 46.dp),
    ) {
        if (onBack != null) {
            CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack, Modifier.align(Alignment.CenterStart))
        }
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(horizontal = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = ShadowedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(subtitle, color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }
        if (trailing != null) {
            Box(Modifier.align(Alignment.CenterEnd)) { trailing() }
        }
    }
}

@Composable
fun Avatar(
    image: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    ring: Color = Color.White,
    ringWidth: Dp = 3.dp,
    contentDescription: String? = null,
) {
    Image(
        painterResource(image),
        contentDescription,
        modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .border(ringWidth, ring, CircleShape),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun XpBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    label: String? = null,
    track: Color = Color(0x66000000),
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), animationSpec = tween(700), label = "xp")
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .height(height)
            .clip(shape)
            .background(track)
            .border(1.5.dp, Color.White.copy(alpha = 0.6f), shape),
    ) {
        if (animated > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(shape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF8CEB6E), Color(0xFF2DAF3F), Color(0xFF1E8C30)))),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .padding(horizontal = 4.dp, vertical = 1.dp)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.18f)),
        )
        if (label != null) {
            Text(
                label,
                Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = (height.value * 0.62f).sp,
                fontWeight = FontWeight.Bold,
                style = ShadowedText,
                maxLines = 1,
            )
        }
    }
}

/** Wooden sign used for "Choose Your Character". */
@Composable
fun WoodBanner(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .shadow(8.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Quest.WoodLight, Quest.Wood, Quest.WoodDark)))
            .drawBehind {
                val grain = Color(0x26000000)
                for (i in 1..5) {
                    val y = size.height * i / 6f
                    drawLine(grain, Offset(0f, y), Offset(size.width, y + (i % 2) * 5f), strokeWidth = 2f)
                }
            }
            .border(2.dp, Color(0xFF3A1204), shape)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, style = ShadowedText, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(subtitle, color = Color(0xFFFCE7C8), fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RegionBadge(character: QuestCharacter, size: Dp, modifier: Modifier = Modifier, ring: Color = Color.White) {
    val colors = if (character.darkText) {
        listOf(character.color.darken(0.3f), character.color.darken(0.5f))
    } else {
        listOf(character.color.lighten(0.12f), character.color.darken(0.25f))
    }
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.verticalGradient(colors))
            .border(size * 0.07f, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(character.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.56f))
    }
}

/** Cream dialog card used for setup, pause, results and confirmations. */
@Composable
fun QuestDialog(onDismiss: () -> Unit, dismissible: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Dialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible),
    ) {
        val shape = RoundedCornerShape(26.dp)
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(18.dp, shape)
                .clip(shape)
                .background(Quest.Cream)
                .border(3.dp, Quest.CreamDeep, shape)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) Brush.verticalGradient(listOf(Quest.GreenTop, Quest.GreenDark)) else Brush.verticalGradient(listOf(Color.White, Color.White)))
            .border(2.dp, if (selected) Quest.GreenDark else Quest.Line, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (selected) Color.White else Quest.Ink,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun WhiteCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(1.5.dp, Quest.Line, shape)
            .padding(14.dp),
        content = content,
    )
}
