package com.cameroonquest.app.domain.game.dochi

import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.game.RealTimeGame
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.PlayMode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class DochiConfig(val duration: Double, val ballSpeed: Double, val interval: Double) {
    companion object {
        fun of(difficulty: Difficulty): DochiConfig = when (difficulty) {
            Difficulty.EASY -> DochiConfig(30.0, 170.0, 1.05)
            Difficulty.MEDIUM -> DochiConfig(40.0, 210.0, 0.8)
            Difficulty.HARD -> DochiConfig(45.0, 250.0, 0.62)
        }
    }
}

class Ball internal constructor(x: Double, y: Double, private val vx: Double, private val vy: Double) {
    var x = x
        private set
    var y = y
        private set

    internal val isOffCourt: Boolean
        get() = x < -MARGIN || x > DochiGame.WIDTH + MARGIN || y < -MARGIN || y > DochiGame.HEIGHT + MARGIN

    internal fun move(dt: Double) {
        x += vx * dt
        y += vy * dt
    }

    private companion object {
        const val MARGIN = 20.0
    }
}

/** Dochi dodgeball on a 640 x 460 court: survive the throws from both sides until the timer ends. */
class DochiGame(setup: GameSetup, private val random: Random) : RealTimeGame(setup) {
    init {
        require(setup.kind == GameKind.DOCHI && setup.mode == PlayMode.SOLO) { "Dochi is a solo game." }
    }

    val config: DochiConfig = DochiConfig.of(setup.difficulty)
    private val flying = mutableListOf<Ball>()
    private var inputX = 0.0
    private var inputY = 0.0
    private var finished = false

    var time = 0.0
        private set
    var x = START_X
        private set
    var y = START_Y
        private set
    var points = 0
        private set
    var won = false
        private set
    var nextSide = 0
        private set
    internal var nextThrowAt = FIRST_THROW

    val balls: List<Ball> get() = flying
    override val isOver: Boolean get() = finished

    fun move(dx: Double, dy: Double) {
        require(dx.isFinite() && dy.isFinite()) { "Dochi requires numeric movement." }
        inputX = dx.coerceIn(-1.0, 1.0)
        inputY = dy.coerceIn(-1.0, 1.0)
    }

    override fun update(dt: Double) {
        val step = min(dt, max(0.0, config.duration - time))
        val oldX = x
        val oldY = y
        val magnitude = max(1.0, hypot(inputX, inputY))
        x = (x + inputX / magnitude * PLAYER_SPEED * step).coerceIn(MIN_X, MAX_X)
        y = (y + inputY / magnitude * PLAYER_SPEED * step).coerceIn(MIN_Y, MAX_Y)
        time += step
        points = ((time + 1e-8) * 10).toInt()
        if (time >= nextThrowAt) {
            val fromX = if (nextSide == 0) LEFT_THROWER_X else RIGHT_THROWER_X
            val angle = atan2(y - THROWER_Y + (random.nextDouble() - 0.5) * 40, x - fromX)
            throwBall(fromX, THROWER_Y, cos(angle) * config.ballSpeed, sin(angle) * config.ballSpeed)
            nextSide = 1 - nextSide
            nextThrowAt += config.interval
        }
        for (ball in flying) {
            val ballOldX = ball.x
            val ballOldY = ball.y
            ball.move(step)
            if (sweptDistance(ballOldX - oldX, ballOldY - oldY, ball.x - x, ball.y - y) <= PLAYER_RADIUS + BALL_RADIUS) {
                finished = true
                won = false
                publish(GameEvent.PlayerHit)
                publish(GameEvent.RoundOver)
                return
            }
        }
        flying.removeAll { it.isOffCourt }
        if (time >= config.duration - 1e-8) {
            time = config.duration
            points = (config.duration * 10).toInt()
            finished = true
            won = true
            publish(GameEvent.RoundOver)
        }
    }

    internal fun throwBall(fromX: Double, fromY: Double, vx: Double, vy: Double) {
        flying += Ball(fromX, fromY, vx, vy)
    }

    override fun score(): Int = points

    override fun outcome(): Outcome = if (won) Outcome.WIN else Outcome.LOSS

    /** Closest distance to the player along the ball's path between two steps, so fast balls cannot skip through. */
    private fun sweptDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val dx = x2 - x1
        val dy = y2 - y1
        val lengthSquared = dx * dx + dy * dy
        val t = if (lengthSquared == 0.0) 0.0 else (-(x1 * dx + y1 * dy) / lengthSquared).coerceIn(0.0, 1.0)
        return hypot(x1 + t * dx, y1 + t * dy)
    }

    companion object {
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
        private const val START_X = 320.0
        private const val START_Y = 230.0
        private const val PLAYER_SPEED = 230.0
        private const val FIRST_THROW = 1.2
    }
}
