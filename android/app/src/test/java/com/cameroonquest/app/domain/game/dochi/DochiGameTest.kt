package com.cameroonquest.app.domain.game.dochi

import com.cameroonquest.app.FixedRandom
import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.PlayMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot
import kotlin.random.Random

class DochiGameTest {
    private fun game(difficulty: Difficulty = Difficulty.EASY, random: Random = Random(1)) =
        DochiGame(GameSetup(GameKind.DOCHI, difficulty, PlayMode.SOLO), random)

    @Test
    fun normalizesDiagonalsAndKeepsThePlayerInside() {
        val straight = game()
        val diagonal = game()
        straight.move(1.0, 0.0)
        diagonal.move(1.0, 1.0)
        straight.advance(0.2)
        diagonal.advance(0.2)
        assertEquals(straight.x - 320, hypot(diagonal.x - 320, diagonal.y - 230), 1e-7)

        straight.nextThrowAt = Double.POSITIVE_INFINITY
        straight.move(1.0, -1.0)
        repeat(10) { straight.advance(1.0) }
        assertEquals(DochiGame.MAX_X, straight.x, 0.0)
        assertEquals(DochiGame.MIN_Y, straight.y, 0.0)
    }

    @Test
    fun hitsAStandingPlayer() {
        val round = game(random = FixedRandom(0.5))
        val events = mutableListOf<GameEvent>()
        round.addListener { events += it }
        while (!round.isOver) round.advance(0.1)
        assertFalse(round.won)
        assertTrue(round.time < 5)
        assertEquals(listOf(GameEvent.PlayerHit, GameEvent.RoundOver), events)
        assertEquals(Outcome.LOSS, round.result().outcome)
    }

    @Test
    fun survivingGivesTheExactTargetScore() {
        val round = game(Difficulty.HARD)
        round.nextThrowAt = Double.POSITIVE_INFINITY
        while (!round.isOver) round.advance(0.1)
        assertTrue(round.won)
        assertEquals(45.0, round.time, 0.0)
        assertEquals(450, round.score())
    }

    @Test
    fun catchesFastBallsBetweenSteps() {
        val round = game()
        round.throwBall(280.0, 230.0, 10_000.0, 0.0)
        round.advance(1.0 / 60)
        assertTrue(round.isOver)
        assertFalse(round.won)
    }

    @Test
    fun rejectsTwoPlayerRoundsAndInvalidInput() {
        assertThrows(IllegalArgumentException::class.java) {
            DochiGame(GameSetup(GameKind.DOCHI, Difficulty.EASY, PlayMode.LOCAL), Random(1))
        }
        assertThrows(IllegalArgumentException::class.java) { game().move(Double.NaN, 0.0) }
    }
}
