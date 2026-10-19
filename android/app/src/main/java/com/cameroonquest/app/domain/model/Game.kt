package com.cameroonquest.app.domain.model

enum class GameKind {
    SONGO,
    PIROGUE,
    DOCHI,
    QUIZ;

    val hasLevels: Boolean get() = this != QUIZ
    val allowsTwoPlayers: Boolean get() = this == SONGO || this == PIROGUE
}

enum class Difficulty(val level: Int) { EASY(1), MEDIUM(2), HARD(3) }

enum class PlayMode { SOLO, LOCAL }

enum class Outcome { WIN, DRAW, LOSS }

/** Everything needed to start a round. */
data class GameSetup(
    val kind: GameKind,
    val difficulty: Difficulty = Difficulty.EASY,
    val mode: PlayMode = PlayMode.SOLO,
) {
    init {
        require(mode == PlayMode.SOLO || kind.allowsTwoPlayers) { "$kind cannot be played by two players." }
    }
}

/** What a finished round reports for player 1, the only player whose progress is saved. */
data class RoundResult(val setup: GameSetup, val score: Int, val outcome: Outcome) {
    init {
        require(score >= 0) { "A score cannot be negative." }
    }
}
