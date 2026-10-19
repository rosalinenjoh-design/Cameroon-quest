package com.cameroonquest.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.cameroonquest.app.R

/** Colours sampled from design/mockup.png.png. */
object Quest {
    val Teal = Color(0xFF056E61)
    val TealDark = Color(0xFF014A44)
    val TealDeep = Color(0xFF013B36)
    val GreenTop = Color(0xFF1DBA58)
    val Green = Color(0xFF039E42)
    val GreenDark = Color(0xFF03763B)
    val BlueTop = Color(0xFF3AA2F5)
    val BlueDark = Color(0xFF0262C9)
    val PurpleTop = Color(0xFFB04BE0)
    val PurpleDark = Color(0xFF6A10AE)
    val YellowTop = Color(0xFFFFDA55)
    val Yellow = Color(0xFFFBB414)
    val YellowDark = Color(0xFFE89304)
    val YellowInk = Color(0xFF4A2A06)
    val Wood = Color(0xFF762A0B)
    val WoodLight = Color(0xFF9C4A1E)
    val WoodDark = Color(0xFF4A1A06)
    val SongoWood = Color(0xFF673315)
    val Parchment = Color(0xFFFBEAD4)
    val Cream = Color(0xFFFCF8EE)
    val CreamDeep = Color(0xFFF3E3C3)
    val Line = Color(0xFFE6D3B0)
    val Ink = Color(0xFF2A1E14)
    val InkSoft = Color(0xFF6B5A48)
    val Navy = Color(0xFF14284A)
    val Sky = Color(0xFF2F8FDF)
    val SkyLight = Color(0xFF72C5F7)
    val Jungle = Color(0xFF0B6A44)
    val JungleDark = Color(0xFF0A3F28)
    val KenteRed = Color(0xFFC8261C)
    val KenteGold = Color(0xFFF2B705)
    val KenteGreen = Color(0xFF138A3E)
    val KenteBlack = Color(0xFF1C1208)
    val KenteOrange = Color(0xFFE86A10)
    val Correct = Color(0xFF22AE47)
    val Wrong = Color(0xFFD93B3B)
    val Gold = Color(0xFFF6C343)
    val Silver = Color(0xFFC3CBD6)
    val Bronze = Color(0xFFD08A4E)
}

fun Color.darken(fraction: Float): Color = lerp(this, Color.Black, fraction)

fun Color.lighten(fraction: Float): Color = lerp(this, Color.White, fraction)

val Fredoka = FontFamily(
    Font(R.font.fredoka, FontWeight.Normal),
    Font(R.font.fredoka, FontWeight.Medium),
    Font(R.font.fredoka, FontWeight.SemiBold),
    Font(R.font.fredoka, FontWeight.Bold),
)

val TextShadow = Shadow(color = Color(0x99000000), offset = Offset(0f, 3f), blurRadius = 6f)

val ShadowedText = TextStyle(shadow = TextShadow)

private fun TextStyle.fredoka() = copy(fontFamily = Fredoka)

private val Base = Typography()

private val QuestTypography = Typography(
    displayLarge = Base.displayLarge.fredoka(),
    displayMedium = Base.displayMedium.fredoka(),
    displaySmall = Base.displaySmall.fredoka(),
    headlineLarge = Base.headlineLarge.fredoka(),
    headlineMedium = Base.headlineMedium.fredoka(),
    headlineSmall = Base.headlineSmall.fredoka(),
    titleLarge = Base.titleLarge.fredoka(),
    titleMedium = Base.titleMedium.fredoka(),
    titleSmall = Base.titleSmall.fredoka(),
    bodyLarge = Base.bodyLarge.fredoka(),
    bodyMedium = Base.bodyMedium.fredoka(),
    bodySmall = Base.bodySmall.fredoka(),
    labelLarge = Base.labelLarge.fredoka(),
    labelMedium = Base.labelMedium.fredoka(),
    labelSmall = Base.labelSmall.fredoka(),
)

@Composable
fun CameroonQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Quest.Teal,
            onPrimary = Color.White,
            secondary = Quest.Green,
            onSecondary = Color.White,
            tertiary = Quest.Yellow,
            background = Quest.Parchment,
            onBackground = Quest.Ink,
            surface = Quest.Cream,
            onSurface = Quest.Ink,
            error = Quest.Wrong,
        ),
        typography = QuestTypography,
        content = content,
    )
}
