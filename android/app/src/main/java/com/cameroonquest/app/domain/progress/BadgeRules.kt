package com.cameroonquest.app.domain.progress

import com.cameroonquest.app.domain.model.Badge
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.domain.model.RoundResult

/** One rule per badge: a new badge is a new class, existing rules never change (open/closed principle). */
interface BadgeRule {
    val badge: Badge

    fun isEarnedBy(result: RoundResult): Boolean
}

object FirstVictoryRule : BadgeRule {
    override val badge = Badge.FIRST_VICTORY

    override fun isEarnedBy(result: RoundResult): Boolean = result.outcome == Outcome.WIN
}

object SongoMasterRule : BadgeRule {
    override val badge = Badge.SONGO_MASTER

    override fun isEarnedBy(result: RoundResult): Boolean =
        result.setup.kind == GameKind.SONGO &&
            result.setup.mode == PlayMode.SOLO &&
            result.setup.difficulty == Difficulty.HARD &&
            result.outcome == Outcome.WIN
}

class PerfectQuizRule(private val perfectScore: Int) : BadgeRule {
    override val badge = Badge.CULTURE_SCHOLAR

    override fun isEarnedBy(result: RoundResult): Boolean =
        result.setup.kind == GameKind.QUIZ && result.score >= perfectScore
}
