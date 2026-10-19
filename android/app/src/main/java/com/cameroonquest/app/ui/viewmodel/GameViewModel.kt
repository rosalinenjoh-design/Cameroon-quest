package com.cameroonquest.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.game.GameEventListener
import com.cameroonquest.app.domain.game.GameFactory
import com.cameroonquest.app.domain.game.GameSession
import com.cameroonquest.app.domain.game.RealTimeGame
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.RoundResult
import com.cameroonquest.app.domain.model.RoundReward
import com.cameroonquest.app.domain.progress.ProgressTracker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** State pattern: each phase of a round decides which transitions it allows. */
sealed class RoundPhase {
    open val acceptsMoves: Boolean get() = false

    open fun start(): RoundPhase = this

    open fun pause(): RoundPhase = this

    open fun resume(): RoundPhase = this

    data object Ready : RoundPhase() {
        override fun start(): RoundPhase = Playing
    }

    data object Playing : RoundPhase() {
        override val acceptsMoves: Boolean get() = true

        override fun pause(): RoundPhase = Paused
    }

    data object Paused : RoundPhase() {
        override fun resume(): RoundPhase = Playing
    }

    data class Finished(val result: RoundResult, val reward: RoundReward) : RoundPhase()
}

/**
 * Template method: the round lifecycle every game shares (start, pause, restart, saving the result once).
 * Subclasses only add their moves and say how to publish their state.
 */
abstract class GameViewModel<G : GameSession>(
    private val factory: GameFactory<G>,
    val setup: GameSetup,
    private val progress: ProgressTracker,
    private val needsStart: Boolean,
) : ViewModel(), GameEventListener {
    protected var game: G = factory.create(setup)
        private set

    private val _phase = MutableStateFlow(firstPhase())
    val phase: StateFlow<RoundPhase> = _phase.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = EVENT_BUFFER)

    /** Observer: screens collect these to vibrate on captures, hits and answers. */
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    init {
        game.addListener(this)
    }

    fun start() = _phase.update { it.start() }

    open fun pause() = _phase.update { it.pause() }

    fun resume() = _phase.update { it.resume() }

    fun restart() {
        game.removeListener(this)
        game = factory.create(setup).also { it.addListener(this) }
        _phase.value = firstPhase()
        onRestarted()
        publishState()
    }

    /** Call after every move: publishes the new state and saves the result once when the round ends. */
    protected fun refresh() {
        publishState()
        val round = game
        if (round.isOver && _phase.value !is RoundPhase.Finished) {
            val result = round.result()
            _phase.value = RoundPhase.Finished(result, progress.record(result))
        }
    }

    protected abstract fun publishState()

    protected open fun onRestarted() = Unit

    override fun onGameEvent(event: GameEvent) {
        _events.tryEmit(event)
    }

    override fun onCleared() {
        game.removeListener(this)
    }

    private fun firstPhase(): RoundPhase = if (needsStart) RoundPhase.Ready else RoundPhase.Playing

    private companion object {
        const val EVENT_BUFFER = 16
    }
}

/** Template method for real-time games: the screen sends display frames, subclasses only map their controls. */
abstract class RealTimeGameViewModel<G : RealTimeGame>(
    factory: GameFactory<G>,
    setup: GameSetup,
    progress: ProgressTracker,
) : GameViewModel<G>(factory, setup, progress, needsStart = true) {

    /** Changes every frame; reading it inside a draw block redraws the canvas without recomposing the screen. */
    var frame by mutableLongStateOf(0L)
        private set

    val session: G get() = game

    fun onFrame(seconds: Double) {
        if (!phase.value.acceptsMoves) return
        applyControls()
        game.advance(seconds.coerceIn(0.0, MAX_FRAME_SECONDS))
        frame++
        if (game.isOver) refresh()
    }

    override fun pause() {
        super.pause()
        releaseControls()
    }

    override fun onRestarted() {
        releaseControls()
        frame = 0L
    }

    override fun publishState() = Unit

    protected abstract fun applyControls()

    protected abstract fun releaseControls()

    private companion object {
        const val MAX_FRAME_SECONDS = 0.05
    }
}
