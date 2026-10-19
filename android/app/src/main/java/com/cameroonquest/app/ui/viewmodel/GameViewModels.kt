package com.cameroonquest.app.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cameroonquest.app.di.AppContainer
import com.cameroonquest.app.domain.game.GameFactory
import com.cameroonquest.app.domain.game.dochi.DochiGame
import com.cameroonquest.app.domain.game.pirogue.PirogueRace
import com.cameroonquest.app.domain.game.quiz.QuizGame
import com.cameroonquest.app.domain.game.quiz.QuizRound
import com.cameroonquest.app.domain.game.songo.SongoGame
import com.cameroonquest.app.domain.game.songo.SongoPosition
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.progress.ProgressTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SongoUiState(val position: SongoPosition, val playablePits: Set<Int>, val computerTurn: Boolean)

class SongoViewModel(factory: GameFactory<SongoGame>, setup: GameSetup, progress: ProgressTracker) :
    GameViewModel<SongoGame>(factory, setup, progress, needsStart = false) {

    private val _state = MutableStateFlow(snapshot())
    val state: StateFlow<SongoUiState> = _state.asStateFlow()
    private var computerJob: Job? = null

    fun play(pit: Int) {
        if (!phase.value.acceptsMoves || pit !in game.playablePits) return
        game.play(pit)
        refresh()
    }

    override fun publishState() {
        _state.value = snapshot()
        scheduleComputerTurn()
    }

    override fun onRestarted() {
        computerJob?.cancel()
    }

    /** The computer waits while the round is paused and plays as soon as it resumes. */
    private fun scheduleComputerTurn() {
        computerJob?.cancel()
        val round = game
        if (!round.isComputerTurn) return
        computerJob = viewModelScope.launch {
            while (round === game && round.isComputerTurn) {
                phase.first { it.acceptsMoves }
                delay(COMPUTER_DELAY_MS)
                val move = withContext(Dispatchers.Default) { round.computerMove() }
                if (round === game && phase.value.acceptsMoves) {
                    round.playComputer(move)
                    refresh()
                }
            }
        }
    }

    private fun snapshot() = SongoUiState(game.position, game.playablePits, game.isComputerTurn)

    companion object {
        private const val COMPUTER_DELAY_MS = 800L

        fun factory(container: AppContainer, setup: GameSetup) = viewModelFactory {
            initializer { SongoViewModel(container.songoGames, setup, container.progress) }
        }
    }
}

data class QuizUiState(
    val round: QuizRound,
    val number: Int,
    val total: Int,
    val selected: Int?,
    val correctAnswers: Int,
    val isLast: Boolean,
    val winThreshold: Int,
)

class QuizViewModel(factory: GameFactory<QuizGame>, setup: GameSetup, progress: ProgressTracker) :
    GameViewModel<QuizGame>(factory, setup, progress, needsStart = false) {

    private val _state = MutableStateFlow(snapshot())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    fun answer(option: Int) {
        if (!phase.value.acceptsMoves || game.selected != null) return
        game.answer(option)
        refresh()
    }

    fun next() {
        if (!phase.value.acceptsMoves || game.selected == null) return
        game.next()
        refresh()
    }

    override fun publishState() {
        _state.value = snapshot()
    }

    private fun snapshot() = QuizUiState(
        round = game.current,
        number = game.index + 1,
        total = game.rounds.size,
        selected = game.selected,
        correctAnswers = game.correctAnswers,
        isLast = game.isLastQuestion,
        winThreshold = game.winThreshold,
    )

    companion object {
        fun factory(container: AppContainer, setup: GameSetup) = viewModelFactory {
            initializer { QuizViewModel(container.quizGames, setup, container.progress) }
        }
    }
}

enum class SteerButton(val racer: Int, val direction: Int) {
    PLAYER_ONE_LEFT(0, -1),
    PLAYER_ONE_RIGHT(0, 1),
    PLAYER_TWO_LEFT(1, -1),
    PLAYER_TWO_RIGHT(1, 1),
}

class PirogueViewModel(factory: GameFactory<PirogueRace>, setup: GameSetup, progress: ProgressTracker) :
    RealTimeGameViewModel<PirogueRace>(factory, setup, progress) {

    private val held = BooleanArray(SteerButton.entries.size)

    fun hold(button: SteerButton, pressed: Boolean) {
        held[button.ordinal] = pressed && phase.value.acceptsMoves
    }

    override fun applyControls() {
        for (racer in 0..1) game.steer(racer, directionOf(racer))
    }

    override fun releaseControls() {
        held.fill(false)
    }

    private fun directionOf(racer: Int): Double =
        SteerButton.entries.filter { it.racer == racer && held[it.ordinal] }.sumOf { it.direction }.toDouble()

    companion object {
        fun factory(container: AppContainer, setup: GameSetup) = viewModelFactory {
            initializer { PirogueViewModel(container.pirogueRaces, setup, container.progress) }
        }
    }
}

class DochiViewModel(factory: GameFactory<DochiGame>, setup: GameSetup, progress: ProgressTracker) :
    RealTimeGameViewModel<DochiGame>(factory, setup, progress) {

    private var stickX = 0.0
    private var stickY = 0.0

    fun moveStick(x: Float, y: Float) {
        stickX = x.toDouble()
        stickY = y.toDouble()
    }

    override fun applyControls() {
        game.move(stickX, stickY)
    }

    override fun releaseControls() {
        stickX = 0.0
        stickY = 0.0
    }

    companion object {
        fun factory(container: AppContainer, setup: GameSetup) = viewModelFactory {
            initializer { DochiViewModel(container.dochiGames, setup, container.progress) }
        }
    }
}
