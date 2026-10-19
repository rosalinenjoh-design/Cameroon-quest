package com.cameroonquest.app.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.cameroonquest.app.domain.model.Language
import com.cameroonquest.app.domain.model.PlayerProfile
import com.cameroonquest.app.domain.model.RankEntry
import com.cameroonquest.app.domain.repository.CharacterCatalog
import com.cameroonquest.app.domain.repository.LanguageManager
import com.cameroonquest.app.domain.repository.Leaderboard
import java.util.Locale

/** Adapter: exposes AppCompat's per-app language API through the [LanguageManager] interface. */
class AppCompatLanguageManager : LanguageManager {
    override val current: Language
        get() {
            val chosen = AppCompatDelegate.getApplicationLocales()
            val tag = if (chosen.isEmpty()) Locale.getDefault().language else chosen[0]?.language
            return Language.fromTag(tag)
        }

    override fun change(language: Language) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }
}

/** Offline leaderboard: the player against the champions of the nine other regions, with shared ranks for ties. */
class ChampionLeaderboard(private val catalog: CharacterCatalog) : Leaderboard {
    override fun standings(profile: PlayerProfile): List<RankEntry> {
        val mine = catalog.findOrDefault(profile.characterId)
        val champions = catalog.characters
            .filter { it.id != mine.id }
            .map { RankEntry(0, it.name, it, it.championScore, isPlayer = false) }
        val entries = champions + RankEntry(0, profile.name, mine, profile.totalScore, isPlayer = true)
        val sorted = entries.sortedWith(compareByDescending<RankEntry> { it.score }.thenBy { it.isPlayer })
        return sorted.map { entry -> entry.copy(rank = 1 + sorted.count { it.score > entry.score }) }
    }
}
