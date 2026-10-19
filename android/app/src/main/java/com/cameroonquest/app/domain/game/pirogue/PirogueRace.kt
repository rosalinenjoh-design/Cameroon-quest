package com.cameroonquest.app.domain.game.pirogue

import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.game.RealTimeGame
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.PlayMode
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.random.Random

data class RaceConfig(
    val length: Double,
    val speed: Double,
    val spacing: Double,
    val lookahead: Double,
    val missChance: Double,
) {
    companion object {
        fun of(difficulty: Difficulty): RaceConfig = when (difficulty) {
            Difficulty.EASY -> RaceConfig(1800.0, 82.0, 230.0, 90.0, 0.35)
            Difficulty.MEDIUM -> RaceConfig(2400.0, 98.0, 210.0, 140.0, 0.18)
            Difficulty.HARD -> RaceConfig(3000.0, 112.0, 180.0, 200.0, 0.08)
        }
    }
}

/** A floating log. Each canoe is checked against it once, so one log slows a canoe only one time. */
class Obstacle(val distance: Double, val x: Double, val distractsComputer: Boolean) {
    private val passed = BooleanArray(2)

    internal fun isPassedBy(racer: Int): Boolean = passed[racer]

    internal fun markPassedBy(racer: Int) {
        passed[racer] = true
    }
}

/** One canoe: it moves itself and keeps track of its own collisions. */
class Racer internal constructor(private val index: Int) {
    var x = START_X
        private set
    var distance = 0.0
        private set
    var hits = 0
        private set
    var slowedFor = 0.0
        private set
    var finishedAt: Double? = null
        private set

    val isSlowed: Boolean get() = slowedFor > 0

    /** Moves the canoe for one physics step and returns true if it hit a log. */
    internal fun paddle(steering: Double, dt: Double, now: Double, config: RaceConfig, course: List<Obstacle>): Boolean {
        val previous = distance
        x = (x + steering.coerceIn(-1.0, 1.0) * dt * STEER_SPEED).coerceIn(MIN_X, MAX_X)
        val speed = config.speed * if (slowedFor > 0) SLOW_FACTOR else 1.0
        slowedFor = max(0.0, slowedFor - dt)
        distance += speed * dt
        var hit = false
        for (obstacle in course) {
            if (!obstacle.isPassedBy(index) && distance >= obstacle.distance) {
                obstacle.markPassedBy(index)
                if (abs(x - obstacle.x) < COLLISION_HALF_WIDTH) {
                    hits++
                    slowedFor = SLOW_SECONDS
                    hit = true
                }
            }
        }
        if (distance >= config.length && finishedAt == null) {
            finishedAt = now + (config.length - previous) / speed
            distance = config.length
        }
        return hit
    }

    companion object {
        const val COLLISION_HALF_WIDTH = 0.2
        const val START_X = 0.5
        private const val MIN_X = 0.1
        private const val MAX_X = 0.9
        private const val STEER_SPEED = 0.85
        private const val SLOW_FACTOR = 0.35
        private const val SLOW_SECONDS = 1.35
    }
}

/** Strategy: how canoe 2 is steered, by a second person on the same phone or by the computer. */
fun interface Paddler {
    fun steering(race: PirogueRace, humanInput: Double): Double
}

object HumanPaddler : Paddler {
    override fun steering(race: PirogueRace, humanInput: Double): Double = humanInput
}

/** Steers away from the next log, but sometimes gets distracted so a clean human run can win. */
object ComputerPaddler : Paddler {
    override fun steering(race: PirogueRace, humanInput: Double): Double {
        val racer = race.racers[1]
        val next = race.course.firstOrNull {
            it.distance > racer.distance && it.distance - racer.distance < race.config.lookahead
        }
        if (next?.distractsComputer == true) return 0.0
        val target = when {
            next == null -> 0.5
            next.x < 0.5 -> 0.85
            else -> 0.15
        }
        return if (abs(target - racer.x) < 0.025) 0.0 else sign(target - racer.x)
    }
}

/** Pirogue regatta: both canoes paddle at the same speed on the same course; the first across the line wins. */
class PirogueRace(setup: GameSetup, val course: List<Obstacle>, private val opponent: Paddler) : RealTimeGame(setup) {
    init {
        require(setup.kind == GameKind.PIROGUE) { "PirogueRace only plays Pirogue Racing." }
    }

    val config: RaceConfig = RaceConfig.of(setup.difficulty)
    val racers: List<Racer> = listOf(Racer(0), Racer(1))
    private val steeringInput = DoubleArray(2)
    private var finished = false

    var time = 0.0
        private set
    var winner: Int? = null
        private set

    override val isOver: Boolean get() = finished

    fun steer(racer: Int, direction: Double) {
        require(racer in 0..1 && direction.isFinite()) { "Steer canoe 0 or 1 with a number." }
        steeringInput[racer] = direction.coerceIn(-1.0, 1.0)
    }

    fun scoreOf(racer: Int): Int = (racers[racer].distance / config.length * MAX_SCORE).toInt()

    override fun update(dt: Double) {
        val controls = doubleArrayOf(steeringInput[0], opponent.steering(this, steeringInput[1]))
        racers.forEachIndexed { index, racer ->
            if (racer.paddle(controls[index], dt, time, config, course)) publish(GameEvent.LogHit(index))
        }
        time += dt
        val first = racers[0].finishedAt
        val second = racers[1].finishedAt
        if (first != null || second != null) {
            finished = true
            winner = when {
                first != null && second != null -> if (abs(first - second) < 1e-7) null else if (first < second) 0 else 1
                first != null -> 0
                else -> 1
            }
            publish(GameEvent.RoundOver)
        }
    }

    override fun score(): Int = scoreOf(0)

    override fun outcome(): Outcome = when (winner) {
        0 -> Outcome.WIN
        null -> Outcome.DRAW
        else -> Outcome.LOSS
    }

    companion object {
        private const val MAX_SCORE = 1000
        private const val FIRST_LOG = 300.0

        /** Factory method: a race on a random course, steered by the computer in solo mode. */
        fun create(setup: GameSetup, random: Random): PirogueRace {
            val config = RaceConfig.of(setup.difficulty)
            val course = mutableListOf<Obstacle>()
            var distance = FIRST_LOG
            while (distance < config.length - 100) {
                course += Obstacle(distance, 0.2 + random.nextDouble() * 0.6, random.nextDouble() < config.missChance)
                distance += config.spacing
            }
            return PirogueRace(setup, course, if (setup.mode == PlayMode.SOLO) ComputerPaddler else HumanPaddler)
        }
    }
}
