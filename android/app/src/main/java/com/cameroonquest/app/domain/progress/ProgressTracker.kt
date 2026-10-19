package com.cameroonquest.app.domain.progress

import com.cameroonquest.app.domain.model.RoundResult
import com.cameroonquest.app.domain.model.RoundReward
import com.cameroonquest.app.domain.repository.PlayerRepository

/** Facade: one call records a finished round; rewards, levels, badges and storage stay hidden behind it. */
class ProgressTracker(
    private val players: PlayerRepository,
    private val rewards: RewardPolicy,
    private val levels: LevelPolicy,
    private val badgeRules: List<BadgeRule>,
) {
    fun record(result: RoundResult): RoundReward {
        val before = players.profile.value
        val xp = rewards.xpFor(result)
        val newBadges = badgeRules.filter { it.isEarnedBy(result) }.map { it.badge }.toSet() - before.badges
        val after = before.afterRound(result, xp, newBadges)
        players.save(after)
        val levelBefore = levels.levelFor(before.xp).level
        val levelAfter = levels.levelFor(after.xp).level
        return RoundReward(
            xp = xp,
            score = result.score,
            newLevel = levelAfter.takeIf { it > levelBefore },
            newBest = result.score > before.bestScore(result.setup.kind),
            newBadges = newBadges.toList(),
        )
    }
}
