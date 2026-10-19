package com.cameroonquest.app.domain.progress

import com.cameroonquest.app.InMemoryStore
import com.cameroonquest.app.data.repository.PreferencesPlayerRepository
import com.cameroonquest.app.domain.game.quiz.QuizGame
import com.cameroonquest.app.domain.model.Badge
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.LevelInfo
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.domain.model.PlayerProfile
import com.cameroonquest.app.domain.model.RoundResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTrackerTest {
    private val levels = LinearLevelPolicy(250)
    private val rewards = GameRewardPolicy(
        fallback = OutcomeRewardPolicy(),
        byGame = mapOf(GameKind.QUIZ to AnswerRewardPolicy(QuizGame.POINTS_PER_ANSWER)),
    )
    private val store = InMemoryStore()
    private val players = PreferencesPlayerRepository(store)
    private val tracker = ProgressTracker(
        players = players,
        rewards = rewards,
        levels = levels,
        badgeRules = listOf(FirstVictoryRule, SongoMasterRule, PerfectQuizRule(500)),
    )

    private fun result(kind: GameKind, score: Int, outcome: Outcome, difficulty: Difficulty = Difficulty.EASY) =
        RoundResult(GameSetup(kind, difficulty, PlayMode.SOLO), score, outcome)

    @Test
    fun levelsChangeEvery250Xp() {
        assertEquals(LevelInfo(1, 0, 250, 250), levels.levelFor(0))
        assertEquals(LevelInfo(1, 249, 250, 250), levels.levelFor(249))
        assertEquals(LevelInfo(2, 0, 250, 500), levels.levelFor(250))
        assertEquals(LevelInfo(2, 249, 250, 500), levels.levelFor(499))
        assertEquals(LevelInfo(3, 0, 250, 750), levels.levelFor(500))
    }

    @Test
    fun rewardsMatchTheWebApp() {
        assertEquals(50, rewards.xpFor(result(GameKind.SONGO, 600, Outcome.WIN)))
        assertEquals(25, rewards.xpFor(result(GameKind.PIROGUE, 1000, Outcome.DRAW)))
        assertEquals(10, rewards.xpFor(result(GameKind.DOCHI, 120, Outcome.LOSS)))
        assertEquals(30, rewards.xpFor(result(GameKind.QUIZ, 150, Outcome.LOSS)))
    }

    @Test
    fun recordingARoundUpdatesAndSavesTheProfile() {
        players.save(PlayerProfile().renamed("  Alex  ").withCharacter("njoh"))
        val reward = tracker.record(result(GameKind.SONGO, 600, Outcome.WIN))

        assertEquals(50, reward.xp)
        assertTrue(reward.newBest)
        assertNull(reward.newLevel)
        assertEquals(listOf(Badge.FIRST_VICTORY), reward.newBadges)

        val saved = players.profile.value
        assertEquals("Alex", saved.name)
        assertEquals(50, saved.xp)
        assertEquals(600, saved.totalScore)
        assertEquals(1, saved.wins)
        assertEquals(600, saved.bestScore(GameKind.SONGO))
        assertEquals(1, saved.timesPlayed(GameKind.SONGO))
        assertEquals(saved, PreferencesPlayerRepository(store).profile.value)
    }

    @Test
    fun badgesAndLevelsAreAnnouncedOnlyOnce() {
        repeat(4) { tracker.record(result(GameKind.SONGO, 100, Outcome.WIN)) }
        val fifth = tracker.record(result(GameKind.SONGO, 100, Outcome.WIN, Difficulty.HARD))
        assertEquals(2, fifth.newLevel)
        assertEquals(listOf(Badge.SONGO_MASTER), fifth.newBadges)
        assertFalse(fifth.newBest)

        val perfect = tracker.record(result(GameKind.QUIZ, 500, Outcome.WIN))
        assertEquals(listOf(Badge.CULTURE_SCHOLAR), perfect.newBadges)
        assertEquals(100, perfect.xp)
        assertEquals(Badge.entries.toSet(), players.profile.value.badges)
    }

    @Test
    fun clearingForgetsEverything() {
        tracker.record(result(GameKind.DOCHI, 300, Outcome.WIN))
        players.clear()
        assertEquals(PlayerProfile(), players.profile.value)
        assertEquals(PlayerProfile(), PreferencesPlayerRepository(store).profile.value)
    }

    @Test
    fun namesAreTrimmedAndShortened() {
        assertEquals("Alex", PlayerProfile().renamed("  Alex ").name)
        assertEquals(PlayerProfile.MAX_NAME_LENGTH, PlayerProfile().renamed("x".repeat(40)).name.length)
    }
}
