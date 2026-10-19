package com.cameroonquest.app.domain.game.songo

import com.cameroonquest.app.domain.model.Difficulty
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/** Strategy: how the computer chooses a Songo move. */
interface SongoStrategy {
    fun chooseMove(position: SongoPosition): Int

    companion object {
        /** Factory: the strategy that matches a difficulty level. */
        fun forDifficulty(difficulty: Difficulty, random: Random = Random.Default): SongoStrategy = when (difficulty) {
            Difficulty.EASY -> RandomMoveStrategy(random)
            Difficulty.MEDIUM -> MinimaxStrategy(depth = 2)
            Difficulty.HARD -> MinimaxStrategy(depth = 4)
        }
    }
}

class RandomMoveStrategy(private val random: Random) : SongoStrategy {
    override fun chooseMove(position: SongoPosition): Int {
        val moves = SongoRules.legalMoves(position)
        check(moves.isNotEmpty()) { "No legal Songo move is available." }
        return moves[random.nextInt(moves.size)]
    }
}

/** Looks [depth] moves ahead with alpha-beta pruning. */
class MinimaxStrategy(private val depth: Int) : SongoStrategy {
    init {
        require(depth >= 1) { "The computer must look at least one move ahead." }
    }

    override fun chooseMove(position: SongoPosition): Int {
        val moves = SongoRules.legalMoves(position)
        check(moves.isNotEmpty()) { "No legal Songo move is available." }
        val player = position.turn
        var bestMove = moves.first()
        var bestScore = Double.NEGATIVE_INFINITY
        for (move in moves) {
            val score = evaluate(SongoRules.play(position, move), player, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun evaluate(position: SongoPosition, player: Int, remaining: Int, alphaStart: Double, betaStart: Double): Double {
        val difference = (position.captured[player] - position.captured[1 - player]).toDouble()
        if (position.isOver) {
            return when (position.winner) {
                null -> 0.0
                player -> WIN_VALUE + difference
                else -> -WIN_VALUE + difference
            }
        }
        if (remaining == 0) return difference * 12 + position.seedsOn(player) - position.seedsOn(1 - player)
        val maximizing = position.turn == player
        var alpha = alphaStart
        var beta = betaStart
        var best = if (maximizing) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        for (move in SongoRules.legalMoves(position)) {
            val score = evaluate(SongoRules.play(position, move), player, remaining - 1, alpha, beta)
            if (maximizing) {
                best = max(best, score)
                alpha = max(alpha, best)
            } else {
                best = min(best, score)
                beta = min(beta, best)
            }
            if (alpha >= beta) break
        }
        return best
    }

    private companion object {
        const val WIN_VALUE = 10_000.0
    }
}
