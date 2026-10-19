package com.cameroonquest.app.domain.repository

import com.cameroonquest.app.domain.model.Language
import com.cameroonquest.app.domain.model.PlayerProfile
import com.cameroonquest.app.domain.model.QuestCharacter
import com.cameroonquest.app.domain.model.QuizQuestion
import com.cameroonquest.app.domain.model.RankEntry
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

/** Repository: the only door to the saved progress, so the rest of the app never touches storage. */
interface PlayerRepository {
    val profile: StateFlow<PlayerProfile>

    fun save(updated: PlayerProfile)

    fun clear()
}

interface SettingsRepository {
    val haptics: StateFlow<Boolean>

    fun setHaptics(enabled: Boolean)
}

interface LanguageManager {
    val current: Language

    fun change(language: Language)
}

interface CharacterCatalog {
    val characters: List<QuestCharacter>

    fun find(id: String?): QuestCharacter? = characters.firstOrNull { it.id == id }

    fun findOrDefault(id: String?): QuestCharacter = find(id) ?: characters.first()

    /** The opponent shown for [player]: [preferredId], unless that is the player's own character. */
    fun rivalOf(player: QuestCharacter, preferredId: String): QuestCharacter =
        find(preferredId)?.takeIf { it.id != player.id } ?: characters.first { it.id != player.id }
}

interface QuestionBank {
    val questions: List<QuizQuestion>

    fun drawRound(count: Int, random: Random): List<QuizQuestion> {
        require(count in 1..questions.size) { "A round needs between 1 and ${questions.size} questions." }
        return questions.shuffled(random).take(count)
    }
}

interface Leaderboard {
    fun standings(profile: PlayerProfile): List<RankEntry>
}
