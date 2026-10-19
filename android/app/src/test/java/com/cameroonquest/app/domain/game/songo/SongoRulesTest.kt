package com.cameroonquest.app.domain.game.songo

import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.PlayMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Ports of tests/game-engine.test.cjs so the Android rules stay identical to the web app. */
class SongoRulesTest {
    @Test
    fun startsWithFivePitsEachAndRejectsOpposingPits() {
        val start = SongoRules.initial()
        assertEquals(50, start.totalSeeds)
        assertEquals(listOf(0, 1, 2, 3, 4), SongoRules.legalMoves(start))
        assertThrows(IllegalArgumentException::class.java) { SongoRules.play(start, 5) }
        assertThrows(IllegalArgumentException::class.java) { SongoRules.play(start, -1) }
        val next = SongoRules.play(start, 0)
        assertEquals(1, next.turn)
        assertEquals(0, next.board[0])
        assertEquals(5, start.board[0])
        assertEquals(50, next.totalSeeds)
    }

    @Test
    fun capturesTwosAndThreesButNeverAWholeRow() {
        val position = SongoRules.initial().copy(board = listOf(0, 0, 0, 0, 2, 1, 2, 5, 0, 0), captured = listOf(20, 20))
        val next = SongoRules.play(position, 4)
        assertEquals(5, next.capturedLast)
        assertEquals(listOf(25, 20), next.captured)
        assertEquals(50, next.totalSeeds)

        val guarded = SongoRules.initial().copy(board = listOf(5, 0, 0, 0, 1, 1, 0, 0, 0, 0), captured = listOf(20, 23))
        val guardedNext = SongoRules.play(guarded, 4)
        assertEquals(0, guardedNext.capturedLast)
        assertEquals(2, guardedNext.board[5])
        assertEquals(50, guardedNext.totalSeeds)
    }

    @Test
    fun requiresFeedingAndFinishesWhenNoFeedIsPossible() {
        val position = SongoRules.initial().copy(board = listOf(1, 0, 0, 0, 2, 0, 0, 0, 0, 0), captured = listOf(23, 24))
        assertEquals(listOf(4), SongoRules.legalMoves(position))
        assertThrows(IllegalArgumentException::class.java) { SongoRules.play(position, 0) }

        val end = SongoRules.initial().copy(board = listOf(0, 0, 0, 0, 1, 1, 0, 0, 0, 0), captured = listOf(24, 24))
        val finished = SongoRules.play(end, 4)
        assertTrue(finished.isOver)
        assertEquals(1, finished.winner)
        assertEquals(50, finished.totalSeeds)
        assertThrows(IllegalArgumentException::class.java) { SongoRules.play(finished, 0) }
    }

    @Test
    fun skipsTheSourcePitOnALap() {
        val position = SongoRules.initial().copy(board = listOf(12, 4, 4, 4, 4, 4, 4, 4, 5, 5))
        val next = SongoRules.play(position, 0)
        assertEquals(0, next.board[0])
        assertEquals(50, next.totalSeeds)
    }

    @Test
    fun everyStrategyPlaysLegalMovesUntilTheEnd() {
        val random = Random(237)
        for (difficulty in Difficulty.entries) {
            val strategy = SongoStrategy.forDifficulty(difficulty, random)
            var position = SongoRules.initial()
            while (!position.isOver) {
                val move = strategy.chooseMove(position)
                assertTrue(move in SongoRules.legalMoves(position))
                position = SongoRules.play(position, move)
                assertEquals(50, position.totalSeeds)
            }
            assertTrue(position.moves <= 200)
            assertTrue(position.board.all { it == 0 })
        }
    }

    @Test
    fun settlesRepeatedPositionsAndTheMoveLimit() {
        val start = SongoRules.initial()
        val expected = SongoRules.play(start, 1)
        val repeated = start.copy(history = listOf(expected.history.last(), expected.history.last()))
        assertEquals(SongoEnding.REPETITION, SongoRules.play(repeated, 1).ending)
        assertEquals(SongoEnding.MOVE_LIMIT, SongoRules.play(SongoRules.initial().copy(moves = 199), 1).ending)
    }

    @Test
    fun soloGameWaitsForTheComputerAndScoresCapturedSeeds() {
        val game = SongoGame(GameSetup(GameKind.SONGO, Difficulty.EASY, PlayMode.SOLO), RandomMoveStrategy(Random(3)))
        val events = mutableListOf<GameEvent>()
        game.addListener { events += it }
        game.play(0)
        assertTrue(game.isComputerTurn)
        assertTrue(game.playablePits.isEmpty())
        assertThrows(IllegalStateException::class.java) { game.play(1) }
        game.playComputer(game.computerMove())
        assertFalse(game.isComputerTurn)
        assertEquals(game.position.captured[0] * SongoGame.POINTS_PER_SEED, game.score())
        assertTrue(events.none { it == GameEvent.RoundOver })
    }

    @Test
    fun aComputerIsOnlyAllowedInSoloRounds() {
        assertThrows(IllegalArgumentException::class.java) {
            SongoGame(GameSetup(GameKind.SONGO, Difficulty.EASY, PlayMode.LOCAL), RandomMoveStrategy(Random(1)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            SongoGame(GameSetup(GameKind.SONGO, Difficulty.EASY, PlayMode.SOLO), null)
        }
    }
}
