package com.cameroonquest.app.ui.style

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import com.cameroonquest.app.R
import com.cameroonquest.app.domain.model.Badge
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.Language
import com.cameroonquest.app.domain.model.LocalizedText
import com.cameroonquest.app.domain.model.PictureKeys
import com.cameroonquest.app.domain.model.QuestCharacter
import com.cameroonquest.app.domain.model.Region
import com.cameroonquest.app.domain.model.Skill
import com.cameroonquest.app.ui.theme.Quest
import java.text.NumberFormat

/** Language of the screen, provided once at the top of the app from the current locale. */
val LocalLanguage = staticCompositionLocalOf { Language.ENGLISH }

@Composable
fun LocalizedText.localized(): String = resolve(LocalLanguage.current)

/** Groups digits the way the current language expects (7,890 in English, 7 890 in French). */
@Composable
fun rememberNumberFormat(): NumberFormat {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { NumberFormat.getIntegerInstance(locale) }
}

object LanguageLabels {
    fun of(language: Language): Int = when (language) {
        Language.ENGLISH -> R.string.language_english
        Language.FRENCH -> R.string.language_french
    }
}

/** How a character looks on screen, so the domain model stays free of Android resources. */
data class CharacterStyle(
    val color: Color,
    val icon: ImageVector,
    val portrait: Int,
    val avatar: Int,
    val backdrop: Int,
    val detailArt: Int? = null,
    val darkText: Boolean = false,
) {
    val onColor: Color get() = if (darkText) Quest.Ink else Color.White
}

object CharacterStyles {
    private val byId: Map<String, CharacterStyle> = mapOf(
        "njoh" to CharacterStyle(
            Color(0xFF026CC2), Icons.Rounded.Waves,
            R.drawable.char_njoh, R.drawable.avatar_njoh, R.drawable.charbg_njoh, detailArt = R.drawable.detail_njoh,
        ),
        "bakama" to CharacterStyle(
            Color(0xFFFBC024), Icons.Rounded.AccountBalance,
            R.drawable.char_bakama, R.drawable.avatar_bakama, R.drawable.charbg_bakama, darkText = true,
        ),
        "mbororo" to CharacterStyle(
            Color(0xFF9AD4F9), Icons.Rounded.WbSunny,
            R.drawable.char_mbororo, R.drawable.avatar_mbororo, R.drawable.charbg_mbororo, darkText = true,
        ),
        "wouri" to CharacterStyle(
            Color(0xFF8A23C8), Icons.Rounded.Landscape,
            R.drawable.char_wouri, R.drawable.avatar_wouri, R.drawable.charbg_wouri,
        ),
        "nkem" to CharacterStyle(
            Color(0xFF4C9B2E), Icons.Rounded.Park,
            R.drawable.char_nkem, R.drawable.avatar_nkem, R.drawable.charbg_nkem,
        ),
        "yeme" to CharacterStyle(
            Color(0xFFC72A5F), Icons.Rounded.Terrain,
            R.drawable.char_yeme, R.drawable.avatar_yeme, R.drawable.charbg_yeme,
        ),
        "zanga" to CharacterStyle(
            Color(0xFFED621D), Icons.Rounded.Grass,
            R.drawable.char_zanga, R.drawable.avatar_zanga, R.drawable.charbg_zanga,
        ),
        "tenda" to CharacterStyle(
            Color(0xFF05BA8D), Icons.Rounded.Eco,
            R.drawable.char_tenda, R.drawable.avatar_tenda, R.drawable.charbg_tenda,
        ),
        "foufou" to CharacterStyle(
            Color(0xFFDC1B20), Icons.Rounded.Pets,
            R.drawable.char_foufou, R.drawable.avatar_foufou, R.drawable.charbg_foufou,
        ),
        "lea" to CharacterStyle(
            Color(0xFF395FE8), Icons.Rounded.BeachAccess,
            R.drawable.char_lea, R.drawable.avatar_lea, R.drawable.charbg_lea,
        ),
    )

    fun of(character: QuestCharacter): CharacterStyle = byId.getValue(character.id)

    fun has(id: String): Boolean = id in byId
}

object RegionLabels {
    fun of(region: Region): Int = when (region) {
        Region.LITTORAL -> R.string.region_littoral
        Region.CENTRE -> R.string.region_centre
        Region.FAR_NORTH -> R.string.region_far_north
        Region.NORTH_WEST -> R.string.region_north_west
        Region.WEST -> R.string.region_west
        Region.SOUTH_WEST -> R.string.region_south_west
        Region.ADAMAWA -> R.string.region_adamawa
        Region.EAST -> R.string.region_east
        Region.NORTH -> R.string.region_north
        Region.SOUTH -> R.string.region_south
    }
}

object SkillStyles {
    fun label(skill: Skill): Int = when (skill) {
        Skill.SPEED -> R.string.skill_speed
        Skill.FOCUS -> R.string.skill_focus
        Skill.TEAMWORK -> R.string.skill_teamwork
        Skill.STRATEGY -> R.string.skill_strategy
        Skill.WISDOM -> R.string.skill_wisdom
        Skill.PATIENCE -> R.string.skill_patience
        Skill.ENDURANCE -> R.string.skill_endurance
        Skill.CREATIVITY -> R.string.skill_creativity
        Skill.RHYTHM -> R.string.skill_rhythm
        Skill.COURAGE -> R.string.skill_courage
        Skill.LEADERSHIP -> R.string.skill_leadership
        Skill.AGILITY -> R.string.skill_agility
    }

    fun icon(skill: Skill): ImageVector = when (skill) {
        Skill.SPEED -> Icons.AutoMirrored.Rounded.DirectionsRun
        Skill.FOCUS -> Icons.Rounded.GpsFixed
        Skill.TEAMWORK -> Icons.Rounded.Groups
        Skill.STRATEGY -> Icons.Rounded.Psychology
        Skill.WISDOM -> Icons.Rounded.Lightbulb
        Skill.PATIENCE -> Icons.Rounded.Timer
        Skill.ENDURANCE -> Icons.Rounded.FitnessCenter
        Skill.CREATIVITY -> Icons.Rounded.Palette
        Skill.RHYTHM -> Icons.Rounded.MusicNote
        Skill.COURAGE -> Icons.Rounded.Shield
        Skill.LEADERSHIP -> Icons.Rounded.Flag
        Skill.AGILITY -> Icons.Rounded.Bolt
    }
}

data class GameStyle(
    val title: Int,
    val origin: Int,
    val description: Int,
    val rules: Int,
    val image: Int,
    /** Character shown as the computer or as player 2. */
    val rivalId: String,
)

object GameStyles {
    fun of(kind: GameKind): GameStyle = when (kind) {
        GameKind.SONGO -> GameStyle(
            R.string.game_songo, R.string.origin_songo, R.string.description_songo, R.string.rules_songo,
            R.drawable.game_songo, rivalId = "bakama",
        )
        GameKind.PIROGUE -> GameStyle(
            R.string.game_pirogue, R.string.origin_pirogue, R.string.description_pirogue, R.string.rules_pirogue,
            R.drawable.game_pirogue, rivalId = "njoh",
        )
        GameKind.DOCHI -> GameStyle(
            R.string.game_dochi, R.string.origin_dochi, R.string.description_dochi, R.string.rules_dochi,
            R.drawable.game_dochi, rivalId = "nkem",
        )
        GameKind.QUIZ -> GameStyle(
            R.string.game_quiz, R.string.origin_quiz, R.string.description_quiz, R.string.rules_quiz,
            R.drawable.quiz_grassfields, rivalId = "bakama",
        )
    }
}

data class BadgeStyle(val title: Int, val description: Int, val icon: ImageVector, val color: Color)

object BadgeStyles {
    fun of(badge: Badge): BadgeStyle = when (badge) {
        Badge.FIRST_VICTORY -> BadgeStyle(
            R.string.badge_first_victory, R.string.badge_first_victory_description, Icons.Rounded.EmojiEvents, Color(0xFFE8892B),
        )
        Badge.SONGO_MASTER -> BadgeStyle(
            R.string.badge_songo_master, R.string.badge_songo_master_description, Icons.Rounded.Star, Color(0xFFF2B21B),
        )
        Badge.CULTURE_SCHOLAR -> BadgeStyle(
            R.string.badge_culture_scholar, R.string.badge_culture_scholar_description, Icons.Rounded.MilitaryTech, Color(0xFFD98E04),
        )
    }
}

object DifficultyLabels {
    fun of(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EASY -> R.string.difficulty_easy
        Difficulty.MEDIUM -> R.string.difficulty_medium
        Difficulty.HARD -> R.string.difficulty_hard
    }
}

object QuizPictures {
    fun of(key: String?): Int? = when (key) {
        PictureKeys.GRASSFIELDS_HOUSE -> R.drawable.quiz_grassfields
        else -> null
    }
}
