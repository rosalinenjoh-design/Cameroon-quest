package com.cameroonquest.app.domain.game.songo

import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.game.GameSession
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.PlayMode

/** One Songo round between player 1 and either the [computer] or a second player on the same phone. */
class SongoGame(setup: GameSetup, private val computer: SongoStrategy?) : GameSession(setup) {
    init {
        require(setup.kind == GameKind.SONGO) { "SongoGame only plays Songo." }
        require((computer != null) == (setup.mode == PlayMode.SOLO)) { "Solo rounds need a computer strategy." }
    }

    var position: SongoPosition = SongoRules.initial()
        private set

    val isComputerTurn: Boolean
        get() = computer != null && !position.isOver && position.turn == COMPUTER

    val playablePits: Set<Int>
        get() = if (isComputerTurn) emptySet() else SongoRules.legalMoves(position).toSet()

    override val isOver: Boolean get() = position.isOver

    fun play(pit: Int) {
        check(!isComputerTurn) { "Wait for the computer to play." }
        sow(pit)
    }

    /** Asks the strategy for a move. It only reads the position, so it may run off the main thread. */
    fun computerMove(): Int = checkNotNull(computer) { "This is a two-player round." }.chooseMove(position)

    fun playComputer(pit: Int) {
        check(isComputerTurn) { "It is not the computer's turn." }
        sow(pit)
    }

    override fun score(): Int = position.captured[0] * POINTS_PER_SEED

    override fun outcome(): Outcome = when (position.winner) {
        0 -> Outcome.WIN
        null -> Outcome.DRAW
        else -> Outcome.LOSS
    }

    private fun sow(pit: Int) {
        val mover = position.turn
        position = SongoRules.play(position, pit)
        if (position.capturedLast > 0) publish(GameEvent.SeedsCaptured(mover, position.capturedLast))
        if (position.isOver) publish(GameEvent.RoundOver)
    }

    companion object {
        const val POINTS_PER_SEED = 20
        private const val COMPUTER = 1
    }
}
