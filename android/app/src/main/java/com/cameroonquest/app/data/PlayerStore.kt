package com.cameroonquest.app.data

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

enum class GameKind(val title: String, val origin: String) {
    SONGO("Songo", "Beti / Centre"),
    PIROGUE("Pirogue Racing", "Sawa / Littoral"),
    DOCHI("Dochi", "Grassfields"),
    QUIZ("Cultural Quiz", "10 regions"),
}

enum class Difficulty(val level: Int, val label: String) { EASY(1, "Easy"), MEDIUM(2, "Medium"), HARD(3, "Hard") }

enum class PlayMode { SOLO, LOCAL }

enum class Outcome { WIN, DRAW, LOSS }

const val XP_PER_LEVEL = 250
const val MAX_NAME_LENGTH = 16

data class LevelInfo(val level: Int, val earned: Int, val nextLevelXp: Int) {
    val progress: Float get() = earned / XP_PER_LEVEL.toFloat()
}

/** One level per 250 XP, starting at level 1 (same rule as the web app). */
fun levelOf(xp: Int): LevelInfo {
    require(xp >= 0) { "XP cannot be negative." }
    val level = 1 + xp / XP_PER_LEVEL
    return LevelInfo(level, xp % XP_PER_LEVEL, level * XP_PER_LEVEL)
}

fun rewardXp(game: GameKind, score: Int, outcome: Outcome): Int = when {
    game == GameKind.QUIZ -> score / 50 * 10
    outcome == Outcome.WIN -> 50
    outcome == Outcome.DRAW -> 25
    else -> 10
}

enum class Badge(val title: String, val description: String, val icon: ImageVector, val color: Color) {
    FIRST_VICTORY("First Victory", "Win any game.", Icons.Rounded.EmojiEvents, Color(0xFFE8892B)),
    SONGO_MASTER("Songo Master", "Beat the computer at Songo on level 3.", Icons.Rounded.Star, Color(0xFFF2B21B)),
    CULTURE_SCHOLAR("Culture Scholar", "Answer every question of a quiz correctly.", Icons.Rounded.MilitaryTech, Color(0xFFD98E04)),
}

data class PlayerProfile(
    val name: String = "",
    val characterId: String? = null,
    val xp: Int = 0,
    val totalScore: Int = 0,
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val best: Map<GameKind, Int> = emptyMap(),
    val played: Map<GameKind, Int> = emptyMap(),
    val badges: Set<Badge> = emptySet(),
    val haptics: Boolean = true,
) {
    val level: LevelInfo get() = levelOf(xp)
    val isReady: Boolean get() = name.isNotBlank() && Characters.byId(characterId) != null
}

data class RoundReward(
    val xp: Int,
    val score: Int,
    val newLevel: Int?,
    val newBest: Boolean,
    val newBadges: List<Badge>,
)

/** Saves the single local player's progress on this device. */
class PlayerStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("cameroon_quest", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(load())
    val profile: StateFlow<PlayerProfile> = state.asStateFlow()

    fun setName(name: String) = update { it.copy(name = name.trim().take(MAX_NAME_LENGTH)) }

    fun setCharacter(id: String) {
        require(Characters.byId(id) != null) { "Unknown character." }
        update { it.copy(characterId = id) }
    }

    fun setHaptics(enabled: Boolean) = update { it.copy(haptics = enabled) }

    fun reset() {
        prefs.edit().clear().apply()
        state.value = PlayerProfile()
    }

    /** Only Player 1 earns progress; a local Player 2 is a guest. */
    fun recordRound(game: GameKind, score: Int, outcome: Outcome, mode: PlayMode, difficulty: Difficulty?): RoundReward {
        require(score >= 0) { "A score cannot be negative." }
        val before = state.value
        val xp = rewardXp(game, score, outcome)
        val earned = buildSet {
            if (outcome == Outcome.WIN) add(Badge.FIRST_VICTORY)
            if (game == GameKind.SONGO && outcome == Outcome.WIN && mode == PlayMode.SOLO && difficulty == Difficulty.HARD) {
                add(Badge.SONGO_MASTER)
            }
            if (game == GameKind.QUIZ && score >= Questions.PER_ROUND * 50) add(Badge.CULTURE_SCHOLAR)
        }
        val previousBest = before.best[game] ?: 0
        val after = before.copy(
            xp = before.xp + xp,
            totalScore = before.totalScore + score,
            gamesPlayed = before.gamesPlayed + 1,
            wins = before.wins + if (outcome == Outcome.WIN) 1 else 0,
            best = before.best + (game to max(previousBest, score)),
            played = before.played + (game to (before.played[game] ?: 0) + 1),
            badges = before.badges + earned,
        )
        save(after)
        return RoundReward(
            xp = xp,
            score = score,
            newLevel = after.level.level.takeIf { it > before.level.level },
            newBest = score > previousBest,
            newBadges = (earned - before.badges).toList(),
        )
    }

    private inline fun update(change: (PlayerProfile) -> PlayerProfile) = save(change(state.value))

    private fun load() = PlayerProfile(
        name = prefs.getString(KEY_NAME, "").orEmpty(),
        characterId = prefs.getString(KEY_CHARACTER, null),
        xp = prefs.getInt(KEY_XP, 0).coerceAtLeast(0),
        totalScore = prefs.getInt(KEY_SCORE, 0).coerceAtLeast(0),
        gamesPlayed = prefs.getInt(KEY_GAMES, 0),
        wins = prefs.getInt(KEY_WINS, 0),
        best = GameKind.entries.associateWith { prefs.getInt(KEY_BEST + it.name, 0) }.filterValues { it > 0 },
        played = GameKind.entries.associateWith { prefs.getInt(KEY_PLAYED + it.name, 0) }.filterValues { it > 0 },
        badges = Badge.entries.filter { prefs.getBoolean(KEY_BADGE + it.name, false) }.toSet(),
        haptics = prefs.getBoolean(KEY_HAPTICS, true),
    )

    private fun save(profile: PlayerProfile) {
        val editor = prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putInt(KEY_XP, profile.xp)
            .putInt(KEY_SCORE, profile.totalScore)
            .putInt(KEY_GAMES, profile.gamesPlayed)
            .putInt(KEY_WINS, profile.wins)
            .putBoolean(KEY_HAPTICS, profile.haptics)
        if (profile.characterId == null) editor.remove(KEY_CHARACTER) else editor.putString(KEY_CHARACTER, profile.characterId)
        GameKind.entries.forEach {
            editor.putInt(KEY_BEST + it.name, profile.best[it] ?: 0)
            editor.putInt(KEY_PLAYED + it.name, profile.played[it] ?: 0)
        }
        Badge.entries.forEach { editor.putBoolean(KEY_BADGE + it.name, it in profile.badges) }
        editor.apply()
        state.value = profile
    }

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_CHARACTER = "character"
        const val KEY_XP = "xp"
        const val KEY_SCORE = "total_score"
        const val KEY_GAMES = "games_played"
        const val KEY_WINS = "wins"
        const val KEY_HAPTICS = "haptics"
        const val KEY_BEST = "best_"
        const val KEY_PLAYED = "played_"
        const val KEY_BADGE = "badge_"
    }
}
