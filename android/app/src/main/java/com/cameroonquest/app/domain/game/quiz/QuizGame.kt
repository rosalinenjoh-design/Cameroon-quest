package com.cameroonquest.app.domain.game.quiz

import com.cameroonquest.app.domain.game.GameEvent
import com.cameroonquest.app.domain.game.GameSession
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.LocalizedText
import com.cameroonquest.app.domain.model.Outcome
import com.cameroonquest.app.domain.model.QuizQuestion
import kotlin.random.Random

/** One question of a round, with its answers shuffled for this round only. */
class QuizRound(val question: QuizQuestion, random: Random) {
    private val order: List<Int> = question.options.indices.shuffled(random)

    val options: List<LocalizedText> = order.map { question.options[it] }
    val correct: Int = order.indexOf(question.correct)
}

class QuizGame(setup: GameSetup, questions: List<QuizQuestion>, random: Random) : GameSession(setup) {
    init {
        require(setup.kind == GameKind.QUIZ) { "QuizGame only plays the quiz." }
        require(questions.isNotEmpty()) { "A quiz needs at least one question." }
    }

    val rounds: List<QuizRound> = questions.map { QuizRound(it, random) }
    private var finished = false

    var index = 0
        private set
    var selected: Int? = null
        private set
    var correctAnswers = 0
        private set

    val current: QuizRound get() = rounds[index]
    val isLastQuestion: Boolean get() = index == rounds.lastIndex

    /** At least 60% of the answers must be right to win (6 out of 10). */
    val winThreshold: Int get() = (rounds.size * 3 + 4) / 5

    override val isOver: Boolean get() = finished

    fun answer(option: Int) {
        check(!finished && selected == null) { "This question is already answered." }
        require(option in current.options.indices) { "Unknown answer." }
        selected = option
        val right = option == current.correct
        if (right) correctAnswers++
        publish(GameEvent.AnswerChecked(right))
    }

    fun next() {
        check(selected != null) { "Answer the question first." }
        if (isLastQuestion) {
            finished = true
            publish(GameEvent.RoundOver)
        } else {
            index++
            selected = null
        }
    }

    override fun score(): Int = correctAnswers * POINTS_PER_ANSWER

    override fun outcome(): Outcome = if (correctAnswers >= winThreshold) Outcome.WIN else Outcome.LOSS

    companion object {
        const val QUESTIONS_PER_ROUND = 10
        const val POINTS_PER_ANSWER = 50
    }
}
