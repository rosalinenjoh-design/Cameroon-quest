package com.cameroonquest.app.domain.progress

import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.LevelInfo
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.RoundResult

/** Strategy: how many XP a finished round is worth. */
fun interface RewardPolicy {
    fun xpFor(result: RoundResult): Int
}

/** Win, draw or loss decides the XP (Songo, Pirogue Racing, Dochi). */
class OutcomeRewardPolicy(
    private val win: Int = 50,
    private val draw: Int = 25,
    private val loss: Int = 10,
) : RewardPolicy {
    override fun xpFor(result: RoundResult): Int = when (result.outcome) {
        Outcome.WIN -> win
        Outcome.DRAW -> draw
        Outcome.LOSS -> loss
    }
}

/** Each correct quiz answer earns XP, whatever the outcome. */
class AnswerRewardPolicy(private val pointsPerAnswer: Int, private val xpPerAnswer: Int = 10) : RewardPolicy {
    init {
        require(pointsPerAnswer > 0) { "An answer must be worth some points." }
    }

    override fun xpFor(result: RoundResult): Int = result.score / pointsPerAnswer * xpPerAnswer
}

/** Chooses a policy per game, so adding a game adds a policy instead of changing existing ones. */
class GameRewardPolicy(
    private val fallback: RewardPolicy,
    private val byGame: Map<GameKind, RewardPolicy>,
) : RewardPolicy {
    override fun xpFor(result: RoundResult): Int = (byGame[result.setup.kind] ?: fallback).xpFor(result)
}

/** Strategy: turns total XP into a level. */
fun interface LevelPolicy {
    fun levelFor(xp: Int): LevelInfo
}

/** One level per [xpPerLevel] XP, starting at level 1 (the same rule as the web app). */
class LinearLevelPolicy(private val xpPerLevel: Int) : LevelPolicy {
    init {
        require(xpPerLevel > 0) { "A level needs a positive amount of XP." }
    }

    override fun levelFor(xp: Int): LevelInfo {
        require(xp >= 0) { "XP cannot be negative." }
        val level = 1 + xp / xpPerLevel
        return LevelInfo(level = level, earned = xp % xpPerLevel, required = xpPerLevel, nextLevelXp = level * xpPerLevel)
    }
}
