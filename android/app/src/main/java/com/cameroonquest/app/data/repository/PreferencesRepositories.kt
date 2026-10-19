package com.cameroonquest.app.data.repository

import com.cameroonquest.app.data.storage.KeyValueStore
import com.cameroonquest.app.domain.model.Badge
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.PlayerProfile
import com.cameroonquest.app.domain.repository.PlayerRepository
import com.cameroonquest.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Repository that keeps the profile in a key-value store and publishes every change (observer). */
class PreferencesPlayerRepository(private val store: KeyValueStore) : PlayerRepository {
    private val state = MutableStateFlow(load())

    override val profile: StateFlow<PlayerProfile> = state.asStateFlow()

    override fun save(updated: PlayerProfile) {
        store.edit {
            putString(NAME, updated.name)
            putString(CHARACTER, updated.characterId)
            putInt(XP, updated.xp)
            putInt(TOTAL_SCORE, updated.totalScore)
            putInt(GAMES_PLAYED, updated.gamesPlayed)
            putInt(WINS, updated.wins)
            GameKind.entries.forEach { kind ->
                putInt(BEST + kind.name, updated.bestScore(kind))
                putInt(PLAYED + kind.name, updated.timesPlayed(kind))
            }
            Badge.entries.forEach { badge -> putBoolean(BADGE + badge.name, badge in updated.badges) }
        }
        state.value = updated
    }

    override fun clear() {
        store.clear()
        state.value = PlayerProfile()
    }

    private fun load() = PlayerProfile(
        name = store.getString(NAME).orEmpty(),
        characterId = store.getString(CHARACTER),
        xp = store.getInt(XP, 0).coerceAtLeast(0),
        totalScore = store.getInt(TOTAL_SCORE, 0).coerceAtLeast(0),
        gamesPlayed = store.getInt(GAMES_PLAYED, 0).coerceAtLeast(0),
        wins = store.getInt(WINS, 0).coerceAtLeast(0),
        best = GameKind.entries.associateWith { store.getInt(BEST + it.name, 0) }.filterValues { it > 0 },
        played = GameKind.entries.associateWith { store.getInt(PLAYED + it.name, 0) }.filterValues { it > 0 },
        badges = Badge.entries.filter { store.getBoolean(BADGE + it.name, false) }.toSet(),
    )

    private companion object {
        const val NAME = "name"
        const val CHARACTER = "character"
        const val XP = "xp"
        const val TOTAL_SCORE = "total_score"
        const val GAMES_PLAYED = "games_played"
        const val WINS = "wins"
        const val BEST = "best_"
        const val PLAYED = "played_"
        const val BADGE = "badge_"
    }
}

class PreferencesSettingsRepository(private val store: KeyValueStore) : SettingsRepository {
    private val state = MutableStateFlow(store.getBoolean(HAPTICS, true))

    override val haptics: StateFlow<Boolean> = state.asStateFlow()

    override fun setHaptics(enabled: Boolean) {
        store.edit { putBoolean(HAPTICS, enabled) }
        state.value = enabled
    }

    private companion object {
        const val HAPTICS = "haptics"
    }
}
