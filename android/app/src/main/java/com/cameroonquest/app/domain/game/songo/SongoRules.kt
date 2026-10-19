package com.cameroonquest.app.domain.game.songo

enum class SongoEnding { MAJORITY, NO_MOVES, REPETITION, MOVE_LIMIT }

/** Immutable snapshot of the board. Player 1 owns pits 0-4, player 2 owns pits 5-9; seeds travel 0 -> 9 -> 0. */
data class SongoPosition(
    val board: List<Int>,
    val captured: List<Int>,
    val turn: Int,
    val moves: Int,
    val history: List<String>,
    val lastPit: Int?,
    val capturedLast: Int,
    val ending: SongoEnding?,
) {
    val isOver: Boolean get() = ending != null

    /** Index of the winner, or null for a draw; only meaningful once [isOver]. */
    val winner: Int?
        get() = when {
            captured[0] == captured[1] -> null
            captured[0] > captured[1] -> 0
            else -> 1
        }

    val totalSeeds: Int get() = board.sum() + captured.sum()

    internal val key: String get() = "$turn:${board.joinToString(",")}:${captured.joinToString(",")}"

    fun seedsOn(side: Int): Int = board.subList(side * PITS, (side + 1) * PITS).sum()

    companion object {
        const val PITS = 5
        const val SEEDS = 50

        fun ownerOf(pit: Int): Int = pit / PITS
    }
}

/** Singleton: the stateless rules of five-pit arcade Songo, identical to game-engine.js. */
object SongoRules {
    private const val MAX_MOVES = 200

    fun initial(): SongoPosition {
        val board = List(SongoPosition.PITS * 2) { SongoPosition.SEEDS / (SongoPosition.PITS * 2) }
        val start = SongoPosition(board, listOf(0, 0), 0, 0, emptyList(), null, 0, null)
        return start.copy(history = listOf(start.key))
    }

    fun legalMoves(position: SongoPosition): List<Int> {
        if (position.isOver) return emptyList()
        val opponent = 1 - position.turn
        val opponentEmpty = position.seedsOn(opponent) == 0
        val pits = position.turn * SongoPosition.PITS until (position.turn + 1) * SongoPosition.PITS
        return pits.filter { pit ->
            position.board[pit] > 0 && !(opponentEmpty && sideTotal(sow(position.board, pit).first, opponent) == 0)
        }
    }

    fun play(position: SongoPosition, pit: Int): SongoPosition {
        require(pit in legalMoves(position)) { "Choose a non-empty pit on your side; feed an empty opposing side if you can." }
        val (board, last) = sow(position.board, pit)
        val opponent = 1 - position.turn
        val capturedPits = mutableListOf<Int>()
        var cursor = last
        while (cursor >= 0 && SongoPosition.ownerOf(cursor) == opponent && board[cursor] in 2..3) {
            capturedPits += cursor
            cursor--
        }
        val captured = position.captured.toMutableList()
        val haul = capturedPits.sumOf { board[it] }
        var capturedLast = 0
        // A capture may not empty the opponent's entire row.
        if (haul > 0 && haul < sideTotal(board, opponent)) {
            capturedPits.forEach { board[it] = 0 }
            captured[position.turn] += haul
            capturedLast = haul
        }
        val moved = position.copy(
            board = board.toList(),
            captured = captured.toList(),
            turn = opponent,
            moves = position.moves + 1,
            lastPit = pit,
            capturedLast = capturedLast,
        )
        val next = moved.copy(history = position.history + moved.key)
        return when {
            next.captured.any { it > SongoPosition.SEEDS / 2 } -> finish(next, SongoEnding.MAJORITY)
            legalMoves(next).isEmpty() -> finish(next, SongoEnding.NO_MOVES)
            next.history.count { it == next.key } >= 3 -> finish(next, SongoEnding.REPETITION)
            next.moves >= MAX_MOVES -> finish(next, SongoEnding.MOVE_LIMIT)
            else -> next
        }
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

    private fun sideTotal(board: IntArray, side: Int): Int =
        (side * SongoPosition.PITS until (side + 1) * SongoPosition.PITS).sumOf { board[it] }

    private fun finish(position: SongoPosition, ending: SongoEnding): SongoPosition {
        val captured = listOf(position.captured[0] + position.seedsOn(0), position.captured[1] + position.seedsOn(1))
        return position.copy(board = List(position.board.size) { 0 }, captured = captured, ending = ending)
    }
}
