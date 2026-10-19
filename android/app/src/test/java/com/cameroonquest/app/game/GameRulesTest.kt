package com.cameroonquest.app.game

import com.cameroonquest.app.data.Difficulty
import com.cameroonquest.app.data.PlayMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot
import kotlin.random.Random

/** Ports of tests/game-engine.test.cjs so the Android rules stay identical to the web app. */
class GameRulesTest {
    private class FixedRandom(private val value: Double) : Random() {
        override fun nextBits(bitCount: Int): Int = 0
        override fun nextDouble(): Double = value
    }

    private fun total(state: SongoState) = state.board.sum() + state.captured.sum()

    @Test
    fun songoStartsWithFivePitsEachAndRejectsOpposingPits() {
        val state = Songo.create()
        assertEquals(50, total(state))
        assertEquals(listOf(0, 1, 2, 3, 4), Songo.legalMoves(state))
        assertThrows(IllegalArgumentException::class.java) { Songo.move(state, 5) }
        assertThrows(IllegalArgumentException::class.java) { Songo.move(state, -1) }
        val next = Songo.move(state, 0)
        assertEquals(1, next.turn)
        assertEquals(0, next.board[0])
        assertEquals(5, state.board[0])
        assertEquals(50, total(next))
    }

    @Test
    fun songoCapturesTwosAndThreesButNeverAWholeRow() {
        val state = Songo.create().copy(board = listOf(0, 0, 0, 0, 2, 1, 2, 5, 0, 0), captured = listOf(20, 20))
        val next = Songo.move(state, 4)
        assertEquals(5, next.capturedLast)
        assertEquals(listOf(25, 20), next.captured)
        assertEquals(50, total(next))

        val guarded = Songo.create().copy(board = listOf(5, 0, 0, 0, 1, 1, 0, 0, 0, 0), captured = listOf(20, 23))
        val guardedNext = Songo.move(guarded, 4)
        assertEquals(0, guardedNext.capturedLast)
        assertEquals(2, guardedNext.board[5])
        assertEquals(50, total(guardedNext))
    }

    @Test
    fun songoRequiresFeedingAndFinishesWhenNoFeedIsPossible() {
        val state = Songo.create().copy(board = listOf(1, 0, 0, 0, 2, 0, 0, 0, 0, 0), captured = listOf(23, 24))
        assertEquals(listOf(4), Songo.legalMoves(state))
        assertThrows(IllegalArgumentException::class.java) { Songo.move(state, 0) }

        val end = Songo.create().copy(board = listOf(0, 0, 0, 0, 1, 1, 0, 0, 0, 0), captured = listOf(24, 24))
        val finished = Songo.move(end, 4)
        assertTrue(finished.over)
        assertEquals(1, finished.winner)
        assertEquals(50, total(finished))
        assertThrows(IllegalArgumentException::class.java) { Songo.move(finished, 0) }
    }

    @Test
    fun songoSkipsTheSourcePitOnALap() {
        val state = Songo.create().copy(board = listOf(12, 4, 4, 4, 4, 4, 4, 4, 5, 5))
        val next = Songo.move(state, 0)
        assertEquals(0, next.board[0])
        assertEquals(50, total(next))
    }

    @Test
    fun songoComputerGamesEndConserveSeedsAndPlayLegalMoves() {
        val random = Random(237)
        for (difficulty in Difficulty.entries) {
            var state = Songo.create()
            while (!state.over) {
                val move = Songo.chooseMove(state, difficulty, random)
                assertTrue(move in Songo.legalMoves(state))
                state = Songo.move(state, move)
                assertEquals(50, total(state))
            }
            assertTrue(state.moves <= 200)
            assertTrue(state.board.all { it == 0 })
        }
    }

    @Test
    fun songoSettlesRepeatedPositionsAndTheMoveLimit() {
        val state = Songo.create()
        val expected = Songo.move(state, 1)
        val repeated = state.copy(history = listOf(expected.history.last(), expected.history.last()))
        assertTrue(Songo.move(repeated, 1).over)
        assertTrue(Songo.move(Songo.create().copy(moves = 199), 1).over)
    }

    @Test
    fun pirogueUsesBothLocalInputsIndependently() {
        val state = Pirogue.create(Difficulty.EASY, PlayMode.LOCAL, Random(237))
        Pirogue.step(state, -1.0, 1.0, 0.5)
        assertTrue(state.players[0].x < 0.5)
        assertTrue(state.players[1].x > 0.5)
        assertEquals(state.players[0].distance, state.players[1].distance, 0.0)
    }

    @Test
    fun pirogueIsADrawOnAnEmptyRiverAndFreezesAfterwards() {
        val state = RaceState(PlayMode.LOCAL, Pirogue.config(Difficulty.EASY), emptyList())
        while (!state.over) Pirogue.step(state, 0.0, 0.0, 0.1)
        assertNull(state.winner)
        val distance = state.players[0].distance
        Pirogue.step(state, 1.0, -1.0, 1.0)
        assertEquals(distance, state.players[0].distance, 0.0)
    }

    @Test
    fun pirogueDoesNotDependOnFrameRate() {
        val fast = Pirogue.create(Difficulty.MEDIUM, PlayMode.LOCAL, Random(9))
        val slow = Pirogue.create(Difficulty.MEDIUM, PlayMode.LOCAL, Random(9))
        repeat(300) { Pirogue.step(fast, 0.0, 0.0, 1.0 / 60) }
        repeat(150) { Pirogue.step(slow, 0.0, 0.0, 1.0 / 30) }
        assertEquals(fast.players[0].distance, slow.players[0].distance, 1e-7)
        assertEquals(fast.players[0].hits, slow.players[0].hits)
    }

    @Test
    fun soloPirogueComputerCanMissLogsSoACleanRunWins() {
        val state = Pirogue.create(Difficulty.EASY, PlayMode.SOLO, FixedRandom(0.3))
        Pirogue.step(state, 1.0, 0.0, 0.5)
        while (!state.over) Pirogue.step(state, 0.0, 0.0, 0.1)
        assertEquals(0, state.winner)
        assertEquals(0, state.players[0].hits)
        assertTrue(state.players[1].hits > 0)
    }

    @Test
    fun dochiNormalizesDiagonalsAndKeepsThePlayerInside() {
        val straight = Dochi.create(Difficulty.EASY)
        val diagonal = Dochi.create(Difficulty.EASY)
        Dochi.step(straight, 1.0, 0.0, 0.2)
        Dochi.step(diagonal, 1.0, 1.0, 0.2)
        assertEquals(straight.x - 320, hypot(diagonal.x - 320, diagonal.y - 230), 1e-7)
        repeat(10) {
            straight.nextThrow = Double.POSITIVE_INFINITY
            Dochi.step(straight, 1.0, -1.0, 1.0)
        }
        assertEquals(Dochi.MAX_X, straight.x, 0.0)
        assertEquals(Dochi.MIN_Y, straight.y, 0.0)
    }

    @Test
    fun dochiHitsAStandingPlayer() {
        val state = Dochi.create(Difficulty.EASY)
        while (!state.over) Dochi.step(state, 0.0, 0.0, 0.1, FixedRandom(0.5))
        assertFalse(state.won)
        assertTrue(state.time < 5)
    }

    @Test
    fun dochiSurvivalGivesTheExactTargetScore() {
        val state = Dochi.create(Difficulty.HARD)
        state.nextThrow = Double.POSITIVE_INFINITY
        while (!state.over) Dochi.step(state, 0.0, 0.0, 0.1)
        assertTrue(state.won)
        assertEquals(45.0, state.time, 0.0)
        assertEquals(450, state.score)
    }

    @Test
    fun dochiCatchesFastBallsBetweenFrames() {
        val state = Dochi.create(Difficulty.EASY)
        state.balls += Ball(280.0, 230.0, 10_000.0, 0.0)
        Dochi.step(state, 0.0, 0.0, 1.0 / 60)
        assertTrue(state.over)
        assertFalse(state.won)
    }
}
