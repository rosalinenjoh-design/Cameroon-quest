package com.cameroonquest.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ProgressionTest {
    @Test
    fun levelsChangeEvery250Xp() {
        assertEquals(LevelInfo(1, 0, 250), levelOf(0))
        assertEquals(LevelInfo(1, 249, 250), levelOf(249))
        assertEquals(LevelInfo(2, 0, 500), levelOf(250))
        assertEquals(LevelInfo(2, 249, 500), levelOf(499))
        assertEquals(LevelInfo(3, 0, 750), levelOf(500))
    }

    @Test
    fun rewardsMatchTheWebApp() {
        assertEquals(50, rewardXp(GameKind.SONGO, 600, Outcome.WIN))
        assertEquals(25, rewardXp(GameKind.PIROGUE, 1000, Outcome.DRAW))
        assertEquals(10, rewardXp(GameKind.DOCHI, 120, Outcome.LOSS))
        assertEquals(30, rewardXp(GameKind.QUIZ, 150, Outcome.LOSS))
    }

    @Test
    fun championBoardSharesTiedRanksAndListsThePlayerOnce() {
        val board = championBoard("Alex", "njoh", 7450)
        assertEquals(10, board.size)
        assertFalse(board.any { !it.isPlayer && it.character.id == "njoh" })
        val me = board.single { it.isPlayer }
        val wouri = board.single { it.character.id == "wouri" }
        assertEquals(2, me.rank)
        assertEquals(wouri.rank, me.rank)
        assertTrue(board.indexOf(me) > board.indexOf(wouri))
        assertEquals(1, board.first().rank)
    }

    @Test
    fun quizRoundsUseDistinctQuestionsAndKeepTheRightAnswer() {
        val round = Questions.pickRound(random = Random(7))
        assertEquals(Questions.PER_ROUND, round.size)
        assertEquals(round.size, round.map { it.source.question }.toSet().size)
        round.forEach { assertEquals(it.source.options[it.source.correct], it.options[it.correct]) }
    }

    @Test
    fun everyQuestionIsWellFormed() {
        Questions.bank.forEach { question ->
            assertEquals(question.question, 4, question.options.toSet().size)
            assertTrue(question.question, question.correct in question.options.indices)
            assertTrue(question.question, question.characterId == null || Characters.byId(question.characterId) != null)
        }
    }
}
