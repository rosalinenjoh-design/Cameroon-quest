package com.cameroonquest.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cameroonquest.app.di.AppContainer
import com.cameroonquest.app.domain.model.Language
import com.cameroonquest.app.domain.model.LevelInfo
import com.cameroonquest.app.domain.model.PlayerProfile
import com.cameroonquest.app.domain.model.QuestCharacter
import com.cameroonquest.app.domain.model.RankEntry
import com.cameroonquest.app.domain.progress.LevelPolicy
import com.cameroonquest.app.domain.repository.CharacterCatalog
import com.cameroonquest.app.domain.repository.LanguageManager
import com.cameroonquest.app.domain.repository.Leaderboard
import com.cameroonquest.app.domain.repository.PlayerRepository
import com.cameroonquest.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PlayerUiState(
    val profile: PlayerProfile,
    val level: LevelInfo,
    val character: QuestCharacter,
    val haptics: Boolean,
)

/** Shared by the player screens (home, profile, progress, settings); lives as long as the activity. */
class PlayerViewModel(
    private val players: PlayerRepository,
    private val settings: SettingsRepository,
    private val languages: LanguageManager,
    private val catalog: CharacterCatalog,
    private val levels: LevelPolicy,
) : ViewModel() {
    val characters: List<QuestCharacter> get() = catalog.characters

    val state: StateFlow<PlayerUiState> = combine(players.profile, settings.haptics) { profile, haptics -> toState(profile, haptics) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, toState(players.profile.value, settings.haptics.value))

    fun character(id: String?): QuestCharacter = catalog.findOrDefault(id)

    fun rivalFor(preferredId: String): QuestCharacter = catalog.rivalOf(state.value.character, preferredId)

    fun chooseCharacter(id: String, name: String) {
        require(catalog.find(id) != null) { "Unknown character." }
        players.save(players.profile.value.withCharacter(id).renamed(name))
    }

    fun rename(name: String) {
        players.save(players.profile.value.renamed(name))
    }

    fun setHaptics(enabled: Boolean) = settings.setHaptics(enabled)

    fun setLanguage(language: Language) = languages.change(language)

    fun resetProgress() = players.clear()

    private fun toState(profile: PlayerProfile, haptics: Boolean) = PlayerUiState(
        profile = profile,
        level = levels.levelFor(profile.xp),
        character = catalog.findOrDefault(profile.characterId),
        haptics = haptics,
    )

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                PlayerViewModel(container.players, container.settings, container.languages, container.characters, container.levels)
            }
        }
    }
}

class LeaderboardViewModel(players: PlayerRepository, leaderboard: Leaderboard) : ViewModel() {
    val profile: StateFlow<PlayerProfile> = players.profile

    val standings: StateFlow<List<RankEntry>> = players.profile
        .map { leaderboard.standings(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, leaderboard.standings(players.profile.value))

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { LeaderboardViewModel(container.players, container.leaderboard) }
        }
    }
}
