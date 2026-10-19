package com.cameroonquest.app.domain.game

import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.RoundResult
import kotlin.math.min

sealed interface GameEvent {
    data class SeedsCaptured(val player: Int, val seeds: Int) : GameEvent

    data class LogHit(val racer: Int) : GameEvent

    data object PlayerHit : GameEvent

    data class AnswerChecked(val correct: Boolean) : GameEvent

    data object RoundOver : GameEvent
}

/** Observer: reacts to game moments (vibration, sound...) without the game knowing who listens. */
fun interface GameEventListener {
    fun onGameEvent(event: GameEvent)
}

/** Base class of every round. Subclasses supply [score] and [outcome]; [result] works the same for all games. */
abstract class GameSession(val setup: GameSetup) {
    private val listeners = mutableListOf<GameEventListener>()

    abstract val isOver: Boolean

    /** Points earned by player 1 so far. */
    abstract fun score(): Int

    /** Outcome for player 1, valid once [isOver]. */
    protected abstract fun outcome(): Outcome

    fun result(): RoundResult {
        check(isOver) { "The round is not finished yet." }
        return RoundResult(setup, score(), outcome())
    }

    fun addListener(listener: GameEventListener) {
        listeners += listener
    }

    fun removeListener(listener: GameEventListener) {
        listeners -= listener
    }

    protected fun publish(event: GameEvent) {
        listeners.toList().forEach { it.onGameEvent(event) }
    }
}

/** Factory method: builds a fresh round of one game for a setup. */
fun interface GameFactory<out G : GameSession> {
    fun create(setup: GameSetup): G
}

/**
 * Template method: [advance] cuts any display frame into fixed physics steps and calls [update] for each,
 * so a game plays the same on a 60 Hz or a 120 Hz screen.
 */
abstract class RealTimeGame(setup: GameSetup) : GameSession(setup) {
    fun advance(seconds: Double) {
        require(seconds.isFinite() && seconds in 0.0..1.0) { "A frame must last between 0 and 1 second." }
        var remaining = seconds
        while (remaining > 1e-9 && !isOver) {
            val step = min(STEP_SECONDS, remaining)
            update(step)
            remaining -= step
        }
    }

    protected abstract fun update(dt: Double)

    companion object {
        const val STEP_SECONDS = 1.0 / 120
    }
}
