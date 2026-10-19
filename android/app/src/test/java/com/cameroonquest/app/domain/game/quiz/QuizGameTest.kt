package com.cameroonquest.app.domain.game.quiz

import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.LocalizedText
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizGameTest {
    private fun text(value: String) = LocalizedText(value, "$value (fr)")

    private fun question(index: Int) = QuizQuestion(
        id = "q$index",
        characterId = null,
        question = text("Question $index"),
        options = List(4) { text("Option $it of $index") },
        correct = index % 4,
        fact = text("Fact $index"),
    )

    private fun quiz(count: Int = QuizGame.QUESTIONS_PER_ROUND) =
        QuizGame(GameSetup(GameKind.QUIZ), List(count) { question(it) }, Random(5))

    private fun QuizGame.answerAll(rightAnswers: Int) {
        repeat(rounds.size) { index ->
            answer(if (index < rightAnswers) current.correct else (current.correct + 1) % current.options.size)
            next()
        }
    }

    @Test
    fun shufflingKeepsTheRightAnswer() {
        quiz().rounds.forEach { round ->
            assertEquals(round.question.options[round.question.correct], round.options[round.correct])
            assertEquals(round.question.options.toSet(), round.options.toSet())
        }
    }

    @Test
    fun sixRightAnswersOutOfTenWin() {
        val winner = quiz()
        winner.answerAll(rightAnswers = 6)
        assertTrue(winner.isOver)
        assertEquals(Outcome.WIN, winner.result().outcome)
        assertEquals(300, winner.score())

        val loser = quiz()
        loser.answerAll(rightAnswers = 5)
        assertEquals(Outcome.LOSS, loser.result().outcome)
        assertEquals(250, loser.score())
    }

    @Test
    fun eachQuestionIsAnsweredOnceAndInOrder() {
        val game = quiz(count = 2)
        assertThrows(IllegalStateException::class.java) { game.next() }
        game.answer(game.current.correct)
        assertThrows(IllegalStateException::class.java) { game.answer(0) }
        game.next()
        assertEquals(1, game.index)
        assertThrows(IllegalArgumentException::class.java) { game.answer(4) }
    }

    @Test
    fun publishesEveryAnswerAndTheEndOfTheRound() {
        val game = quiz(count = 2)
        val events = mutableListOf<GameEvent>()
        game.addListener { events += it }
        game.answerAll(rightAnswers = 1)
        assertEquals(
            listOf(GameEvent.AnswerChecked(true), GameEvent.AnswerChecked(false), GameEvent.RoundOver),
            events,
        )
    }
}
