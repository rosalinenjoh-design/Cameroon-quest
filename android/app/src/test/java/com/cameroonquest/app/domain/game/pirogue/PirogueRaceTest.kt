package com.cameroonquest.app.domain.game.pirogue

import com.cameroonquest.app.FixedRandom
import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.PlayMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PirogueRaceTest {
    private fun setup(mode: PlayMode, difficulty: Difficulty = Difficulty.EASY) = GameSetup(GameKind.PIROGUE, difficulty, mode)

    @Test
    fun usesBothLocalInputsIndependently() {
        val race = PirogueRace.create(setup(PlayMode.LOCAL), Random(237))
        race.steer(0, -1.0)
        race.steer(1, 1.0)
        race.advance(0.5)
        assertTrue(race.racers[0].x < Racer.START_X)
        assertTrue(race.racers[1].x > Racer.START_X)
        assertEquals(race.racers[0].distance, race.racers[1].distance, 0.0)
    }

    @Test
    fun anEmptyRiverIsADrawAndTheRaceFreezesAfterwards() {
        val race = PirogueRace(setup(PlayMode.LOCAL), emptyList(), HumanPaddler)
        while (!race.isOver) race.advance(0.1)
        assertNull(race.winner)
        assertEquals(Outcome.DRAW, race.result().outcome)
        val distance = race.racers[0].distance
        race.steer(0, 1.0)
        race.advance(1.0)
        assertEquals(distance, race.racers[0].distance, 0.0)
    }

    @Test
    fun doesNotDependOnFrameRate() {
        val fast = PirogueRace.create(setup(PlayMode.LOCAL, Difficulty.MEDIUM), Random(9))
        val slow = PirogueRace.create(setup(PlayMode.LOCAL, Difficulty.MEDIUM), Random(9))
        repeat(300) { fast.advance(1.0 / 60) }
        repeat(150) { slow.advance(1.0 / 30) }
        assertEquals(fast.racers[0].distance, slow.racers[0].distance, 1e-7)
        assertEquals(fast.racers[0].hits, slow.racers[0].hits)
    }

    @Test
    fun theComputerCanMissLogsSoACleanRunWins() {
        val race = PirogueRace.create(setup(PlayMode.SOLO), FixedRandom(0.3))
        val hits = mutableListOf<Int>()
        race.addListener { event -> if (event is GameEvent.LogHit) hits += event.racer }
        race.steer(0, 1.0)
        race.advance(0.5)
        race.steer(0, 0.0)
        while (!race.isOver) race.advance(0.1)
        assertEquals(0, race.winner)
        assertEquals(0, race.racers[0].hits)
        assertTrue(race.racers[1].hits > 0)
        assertEquals(race.racers[1].hits, hits.count { it == 1 })
        assertEquals(Outcome.WIN, race.result().outcome)
    }
}
