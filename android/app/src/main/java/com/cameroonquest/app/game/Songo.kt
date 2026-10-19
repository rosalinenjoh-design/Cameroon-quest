package com.cameroonquest.app.game

import com.cameroonquest.app.data.Difficulty
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class SongoState(
    val board: List<Int>,
    val captured: List<Int>,
    val turn: Int,
    val moves: Int,
    val over: Boolean,
    /** Index of the winning player, or null for a draw (only meaningful when [over]). */
    val winner: Int?,
    val history: List<String>,
    val lastPit: Int?,
    val capturedLast: Int,
    val reason: String,
)

/**
 * Five-pit arcade Songo, identical to game-engine.js: player 0 owns pits 0-4, player 1 owns pits 5-9,
 * and seeds travel 0 -> 9 -> 0.
 */
object Songo {
    const val PITS = 5
    const val SEEDS = 50
    private const val MAX_MOVES = 200

    fun create(): SongoState {
        val board = List(PITS * 2) { SEEDS / (PITS * 2) }
        val captured = listOf(0, 0)
        return SongoState(board, captured, 0, 0, false, null, listOf(key(0, board, captured)), null, 0, "")
    }

    fun owner(pit: Int): Int = pit / PITS

    fun legalMoves(state: SongoState): List<Int> {
        if (state.over) return emptyList()
        val opponent = 1 - state.turn
        val opponentEmpty = sideTotal(state.board, opponent) == 0
        return (state.turn * PITS until (state.turn + 1) * PITS).filter { pit ->
            state.board[pit] > 0 && !(opponentEmpty && sideTotal(sow(state.board, pit).first.asList(), opponent) == 0)
        }
    }

    fun move(state: SongoState, pit: Int): SongoState {
        require(pit in legalMoves(state)) { "Choose a non-empty pit on your side; feed an empty opposing side if you can." }
        val (board, last) = sow(state.board, pit)
        val opponent = 1 - state.turn
        val capturedPits = mutableListOf<Int>()
        var cursor = last
        while (cursor >= 0 && owner(cursor) == opponent && board[cursor] in 2..3) {
            capturedPits += cursor
            cursor--
        }
        val captured = state.captured.toMutableList()
        val haul = capturedPits.sumOf { board[it] }
        var capturedLast = 0
        // A capture may not empty the opponent's entire row.
        if (haul > 0 && haul < sideTotal(board.asList(), opponent)) {
            capturedPits.forEach { board[it] = 0 }
            captured[state.turn] += haul
            capturedLast = haul
        }
        val nextBoard = board.toList()
        val nextKey = key(opponent, nextBoard, captured)
        val next = state.copy(
            board = nextBoard, captured = captured.toList(), turn = opponent, moves = state.moves + 1,
            history = state.history + nextKey, lastPit = pit, capturedLast = capturedLast,
        )
        return when {
            next.captured.any { it > SEEDS / 2 } -> finish(next, "A player captured most of the seeds.")
            legalMoves(next).isEmpty() -> finish(next, "No legal move remains. Each side collects its remaining seeds.")
            next.history.count { it == nextKey } >= 3 -> finish(next, "The position repeated three times. Remaining seeds are shared by side.")
            next.moves >= MAX_MOVES -> finish(next, "The 200-turn limit was reached. Remaining seeds are shared by side.")
            else -> next
        }
    }

    fun chooseMove(state: SongoState, difficulty: Difficulty, random: Random = Random.Default): Int {
        val moves = legalMoves(state)
        check(moves.isNotEmpty()) { "No legal Songo move is available." }
        if (difficulty == Difficulty.EASY) return moves[random.nextInt(moves.size)]
        val player = state.turn
        val depth = if (difficulty == Difficulty.HARD) 4 else 2

        fun evaluate(position: SongoState, remaining: Int, alphaStart: Double, betaStart: Double): Double {
            val difference = (position.captured[player] - position.captured[1 - player]).toDouble()
            if (position.over) {
                return when (position.winner) {
                    null -> 0.0
                    player -> 10_000 + difference
                    else -> -10_000 + difference
                }
            }
            if (remaining == 0) {
                return difference * 12 + sideTotal(position.board, player) - sideTotal(position.board, 1 - player)
            }
            val maximizing = position.turn == player
            var alpha = alphaStart
            var beta = betaStart
            var best = if (maximizing) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
            for (candidate in legalMoves(position)) {
                val score = evaluate(move(position, candidate), remaining - 1, alpha, beta)
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

        var bestMove = moves.first()
        var bestScore = Double.NEGATIVE_INFINITY
        for (candidate in moves) {
            val score = evaluate(move(state, candidate), depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
            if (score > bestScore) {
                bestScore = score
                bestMove = candidate
            }
        }
        return bestMove
    }

    private fun sow(board: List<Int>, pit: Int): Pair<IntArray, Int> {
        val next = board.toIntArray()
        var seeds = next[pit]
        var last = pit
        next[pit] = 0
        while (seeds > 0) {
            last = (last + 1) % next.size
            if (last != pit) {
                next[last]++
                seeds--
            }
        }
        return next to last
    }

    private fun sideTotal(board: List<Int>, player: Int): Int = board.subList(player * PITS, (player + 1) * PITS).sum()

    private fun key(turn: Int, board: List<Int>, captured: List<Int>) =
        "$turn:${board.joinToString(",")}:${captured.joinToString(",")}"

    private fun finish(state: SongoState, reason: String): SongoState {
        val captured = listOf(state.captured[0] + sideTotal(state.board, 0), state.captured[1] + sideTotal(state.board, 1))
        val winner = if (captured[0] == captured[1]) null else if (captured[0] > captured[1]) 0 else 1
        return state.copy(board = List(PITS * 2) { 0 }, captured = captured, over = true, winner = winner, reason = reason)
    }
}
