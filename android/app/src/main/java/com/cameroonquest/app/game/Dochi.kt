package com.cameroonquest.app.game

import com.cameroonquest.app.data.Difficulty
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class DochiConfig(val duration: Double, val ballSpeed: Double, val interval: Double)

class Ball(var x: Double, var y: Double, val vx: Double, val vy: Double)

class DochiState(val config: DochiConfig) {
    var time = 0.0
    var nextThrow = 1.2
    var nextSide = 0
    var x = 320.0
    var y = 230.0
    val balls = mutableListOf<Ball>()
    var score = 0
    var over = false
    var won = false
}

/** Dochi dodgeball from game-engine.js on a 640 x 460 court with throwers on both sides. */
object Dochi {
    const val WIDTH = 640.0
    const val HEIGHT = 460.0
    const val PLAYER_RADIUS = 13.0
    const val BALL_RADIUS = 8.0
    const val LEFT_THROWER_X = 42.0
    const val RIGHT_THROWER_X = 598.0
    const val THROWER_Y = 230.0
    const val MIN_X = 83.0
    const val MAX_X = 557.0
    const val MIN_Y = 53.0
    const val MAX_Y = 407.0
    private const val PLAYER_SPEED = 230.0

    fun config(difficulty: Difficulty) = when (difficulty) {
        Difficulty.EASY -> DochiConfig(30.0, 170.0, 1.05)
        Difficulty.MEDIUM -> DochiConfig(40.0, 210.0, 0.8)
        Difficulty.HARD -> DochiConfig(45.0, 250.0, 0.62)
    }

    fun create(difficulty: Difficulty) = DochiState(config(difficulty))

    fun step(state: DochiState, inputX: Double, inputY: Double, seconds: Double, random: Random = Random.Default) {
        if (state.over) return
        require(inputX.isFinite() && inputY.isFinite()) { "Dochi requires numeric movement." }
        fixedSteps(seconds) { step ->
            val dt = min(step, max(0.0, state.config.duration - state.time))
            val oldX = state.x
            val oldY = state.y
            val dx = inputX.coerceIn(-1.0, 1.0)
            val dy = inputY.coerceIn(-1.0, 1.0)
            val magnitude = max(1.0, hypot(dx, dy))
            state.x = (state.x + dx / magnitude * PLAYER_SPEED * dt).coerceIn(MIN_X, MAX_X)
            state.y = (state.y + dy / magnitude * PLAYER_SPEED * dt).coerceIn(MIN_Y, MAX_Y)
            state.time += dt
            state.score = ((state.time + 1e-8) * 10).toInt()
            if (state.time >= state.nextThrow) {
                val fromX = if (state.nextSide == 0) LEFT_THROWER_X else RIGHT_THROWER_X
                val angle = atan2(state.y - THROWER_Y + (random.nextDouble() - 0.5) * 40, state.x - fromX)
                state.balls += Ball(fromX, THROWER_Y, cos(angle) * state.config.ballSpeed, sin(angle) * state.config.ballSpeed)
                state.nextSide = 1 - state.nextSide
                state.nextThrow += state.config.interval
            }
            for (ball in state.balls) {
                val ballOldX = ball.x
                val ballOldY = ball.y
                ball.x += ball.vx * dt
                ball.y += ball.vy * dt
                val gap = segmentDistance(ballOldX - oldX, ballOldY - oldY, ball.x - state.x, ball.y - state.y)
                if (gap <= PLAYER_RADIUS + BALL_RADIUS) {
                    state.over = true
                    state.won = false
                    return@fixedSteps false
                }
            }
            state.balls.removeAll { it.x < -20 || it.x > WIDTH + 20 || it.y < -20 || it.y > HEIGHT + 20 }
            if (state.time >= state.config.duration - 1e-8) {
                state.time = state.config.duration
                state.score = (state.config.duration * 10).toInt()
                state.over = true
                state.won = true
                return@fixedSteps false
            }
            true
        }
    }

    /** Closest distance to the origin along the segment between two relative positions (swept collision). */
    private fun segmentDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val dx = x2 - x1
        val dy = y2 - y1
        val lengthSquared = dx * dx + dy * dy
        val t = if (lengthSquared == 0.0) 0.0 else (-(x1 * dx + y1 * dy) / lengthSquared).coerceIn(0.0, 1.0)
        return hypot(x1 + t * dx, y1 + t * dy)
    }
}
