package com.cameroonquest.app.domain.model

enum class Badge { FIRST_VICTORY, SONGO_MASTER, CULTURE_SCHOLAR }

/** The local player's saved progress. Every change returns a new copy, so a profile is never half-updated. */
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
) {
    val hasIdentity: Boolean get() = name.isNotBlank() && characterId != null

    fun bestScore(kind: GameKind): Int = best[kind] ?: 0

    fun timesPlayed(kind: GameKind): Int = played[kind] ?: 0

    fun renamed(rawName: String): PlayerProfile = copy(name = rawName.trim().take(MAX_NAME_LENGTH))

    fun withCharacter(id: String): PlayerProfile = copy(characterId = id)

    fun afterRound(result: RoundResult, xpEarned: Int, newBadges: Set<Badge>): PlayerProfile {
        require(xpEarned >= 0) { "XP cannot be negative." }
        val kind = result.setup.kind
        return copy(
            xp = xp + xpEarned,
            totalScore = totalScore + result.score,
            gamesPlayed = gamesPlayed + 1,
            wins = wins + if (result.outcome == Outcome.WIN) 1 else 0,
            best = best + (kind to maxOf(bestScore(kind), result.score)),
            played = played + (kind to timesPlayed(kind) + 1),
            badges = badges + newBadges,
        )
    }

    companion object {
        const val MAX_NAME_LENGTH = 16
    }
}

data class LevelInfo(val level: Int, val earned: Int, val required: Int, val nextLevelXp: Int) {
    val progress: Float get() = earned / required.toFloat()
    val remaining: Int get() = required - earned
}

data class RoundReward(
    val xp: Int,
    val score: Int,
    val newLevel: Int?,
    val newBest: Boolean,
    val newBadges: List<Badge>,
)

data class RankEntry(
    val rank: Int,
    val name: String,
    val character: QuestCharacter,
    val score: Int,
    val isPlayer: Boolean,
)
