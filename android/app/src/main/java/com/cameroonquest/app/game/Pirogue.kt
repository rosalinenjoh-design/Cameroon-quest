package com.cameroonquest.app.game

import com.cameroonquest.app.data.Difficulty
import com.cameroonquest.app.data.PlayMode
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.random.Random

internal const val PHYSICS_STEP = 1.0 / 120

/** Splits a frame into fixed physics steps so results do not depend on the screen refresh rate. */
internal inline fun fixedSteps(seconds: Double, update: (Double) -> Boolean) {
    require(seconds.isFinite() && seconds in 0.0..1.0) { "A simulation step must be between zero and one second." }
    var remaining = seconds
    while (remaining > 1e-9) {
        val step = min(PHYSICS_STEP, remaining)
        if (!update(step)) break
        remaining -= step
    }
}

data class RaceConfig(
    val length: Double,
    val speed: Double,
    val spacing: Double,
    val aiLookahead: Double,
    val aiMissChance: Double,
)

class Obstacle(val distance: Double, val x: Double, val aiDistracted: Boolean) {
    val checked = BooleanArray(2)
}

class Racer {
    var x = 0.5
    var distance = 0.0
    var hits = 0
    var slowFor = 0.0
    var finishAt: Double? = null
}

class RaceState(val mode: PlayMode, val config: RaceConfig, val obstacles: List<Obstacle>) {
    val players = listOf(Racer(), Racer())
    var time = 0.0
    var over = false
    var winner: Int? = null
}

/** Pirogue Regatta from game-engine.js: equal-speed canoes, matched courses, one slowdown per log. */
object Pirogue {
    const val COLLISION_HALF_WIDTH = 0.2
    private const val SLOW_SECONDS = 1.35

    fun config(difficulty: Difficulty) = when (difficulty) {
        Difficulty.EASY -> RaceConfig(1800.0, 82.0, 230.0, 90.0, 0.35)
        Difficulty.MEDIUM -> RaceConfig(2400.0, 98.0, 210.0, 140.0, 0.18)
        Difficulty.HARD -> RaceConfig(3000.0, 112.0, 180.0, 200.0, 0.08)
    }

    fun create(difficulty: Difficulty, mode: PlayMode, random: Random = Random.Default): RaceState {
        val config = config(difficulty)
        val obstacles = mutableListOf<Obstacle>()
        var distance = 300.0
        while (distance < config.length - 100) {
            obstacles += Obstacle(distance, 0.2 + random.nextDouble() * 0.6, random.nextDouble() < config.aiMissChance)
            distance += config.spacing
        }
        return RaceState(mode, config, obstacles)
    }

    fun step(state: RaceState, playerOne: Double, playerTwo: Double, seconds: Double) {
        if (state.over) return
        require(playerOne.isFinite() && playerTwo.isFinite()) { "Both racers need a numeric steering input." }
        fixedSteps(seconds) { dt ->
            val controls = doubleArrayOf(
                playerOne.coerceIn(-1.0, 1.0),
                if (state.mode == PlayMode.SOLO) aiInput(state) else playerTwo.coerceIn(-1.0, 1.0),
            )
            state.players.forEachIndexed { index, racer ->
                val previous = racer.distance
                racer.x = (racer.x + controls[index] * dt * 0.85).coerceIn(0.1, 0.9)
                val speed = state.config.speed * if (racer.slowFor > 0) 0.35 else 1.0
                racer.slowFor = max(0.0, racer.slowFor - dt)
                racer.distance += speed * dt
                for (obstacle in state.obstacles) {
                    if (!obstacle.checked[index] && racer.distance >= obstacle.distance) {
                        obstacle.checked[index] = true
                        if (abs(racer.x - obstacle.x) < COLLISION_HALF_WIDTH) {
                            racer.hits++
                            racer.slowFor = SLOW_SECONDS
                        }
                    }
                }
                if (racer.distance >= state.config.length && racer.finishAt == null) {
                    racer.finishAt = state.time + (state.config.length - previous) / speed
                    racer.distance = state.config.length
                }
            }
            state.time += dt
            val first = state.players[0].finishAt
            val second = state.players[1].finishAt
            if (first != null || second != null) {
                state.over = true
                state.winner = when {
                    first != null && second != null -> if (abs(first - second) < 1e-7) null else if (first < second) 0 else 1
                    first != null -> 0
                    else -> 1
                }
                return@fixedSteps false
            }
            true
        }
    }

    fun score(state: RaceState, player: Int): Int =
        (state.players[player].distance / state.config.length * 1000).toInt()

    private fun aiInput(state: RaceState): Double {
        val racer = state.players[1]
        val next = state.obstacles.firstOrNull {
            it.distance > racer.distance && it.distance - racer.distance < state.config.aiLookahead
        }
        if (next?.aiDistracted == true) return 0.0
        val target = when {
            next == null -> 0.5
            next.x < 0.5 -> 0.85
            else -> 0.15
        }
        return if (abs(target - racer.x) < 0.025) 0.0 else sign(target - racer.x)
    }
}
