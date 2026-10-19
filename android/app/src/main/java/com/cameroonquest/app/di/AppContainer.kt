package com.cameroonquest.app.di

import android.content.Context
import com.cameroonquest.app.data.AppCompatLanguageManager
import com.cameroonquest.app.data.ChampionLeaderboard
import com.cameroonquest.app.data.content.LocalCharacterCatalog
import com.cameroonquest.app.data.content.LocalQuestionBank
import com.cameroonquest.app.data.repository.PreferencesPlayerRepository
import com.cameroonquest.app.data.repository.PreferencesSettingsRepository
import com.cameroonquest.app.data.storage.SharedPreferencesStore
import com.cameroonquest.app.domain.game.GameFactory
import com.cameroonquest.app.domain.game.dochi.DochiGame
import com.cameroonquest.app.domain.game.pirogue.PirogueRace
import com.cameroonquest.app.domain.game.quiz.QuizGame
import com.cameroonquest.app.domain.game.songo.SongoGame
import com.cameroonquest.app.domain.game.songo.SongoStrategy
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.domain.progress.AnswerRewardPolicy
import com.cameroonquest.app.domain.progress.FirstVictoryRule
import com.cameroonquest.app.domain.progress.GameRewardPolicy
import com.cameroonquest.app.domain.progress.LevelPolicy
import com.cameroonquest.app.domain.progress.LinearLevelPolicy
import com.cameroonquest.app.domain.progress.OutcomeRewardPolicy
import com.cameroonquest.app.domain.progress.PerfectQuizRule
import com.cameroonquest.app.domain.progress.ProgressTracker
import com.cameroonquest.app.domain.progress.SongoMasterRule
import com.cameroonquest.app.domain.repository.CharacterCatalog
import com.cameroonquest.app.domain.repository.LanguageManager
import com.cameroonquest.app.domain.repository.Leaderboard
import com.cameroonquest.app.domain.repository.PlayerRepository
import com.cameroonquest.app.domain.repository.QuestionBank
import com.cameroonquest.app.domain.repository.SettingsRepository
import kotlin.random.Random

/** Dependency injection: everything the screens need, typed by interface. */
interface AppContainer {
    val players: PlayerRepository
    val settings: SettingsRepository
    val languages: LanguageManager
    val characters: CharacterCatalog
    val questions: QuestionBank
    val leaderboard: Leaderboard
    val levels: LevelPolicy
    val progress: ProgressTracker
    val songoGames: GameFactory<SongoGame>
    val pirogueRaces: GameFactory<PirogueRace>
    val dochiGames: GameFactory<DochiGame>
    val quizGames: GameFactory<QuizGame>
}

/** Composition root: the only place that chooses which implementation backs each interface. */
class DefaultAppContainer(context: Context) : AppContainer {
    private val random: Random = Random.Default

    override val players: PlayerRepository = PreferencesPlayerRepository(
        SharedPreferencesStore(context.getSharedPreferences(PROGRESS_FILE, Context.MODE_PRIVATE)),
    )
    override val settings: SettingsRepository = PreferencesSettingsRepository(
        SharedPreferencesStore(context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)),
    )
    override val languages: LanguageManager = AppCompatLanguageManager()
    override val characters: CharacterCatalog = LocalCharacterCatalog
    override val questions: QuestionBank = LocalQuestionBank
    override val leaderboard: Leaderboard = ChampionLeaderboard(characters)
    override val levels: LevelPolicy = LinearLevelPolicy(XP_PER_LEVEL)

    override val progress: ProgressTracker = ProgressTracker(
        players = players,
        rewards = GameRewardPolicy(
            fallback = OutcomeRewardPolicy(),
            byGame = mapOf(GameKind.QUIZ to AnswerRewardPolicy(QuizGame.POINTS_PER_ANSWER)),
        ),
        levels = levels,
        badgeRules = listOf(
            FirstVictoryRule,
            SongoMasterRule,
            PerfectQuizRule(QuizGame.QUESTIONS_PER_ROUND * QuizGame.POINTS_PER_ANSWER),
        ),
    )

    override val songoGames: GameFactory<SongoGame> = GameFactory { setup ->
        val computer = if (setup.mode == PlayMode.SOLO) SongoStrategy.forDifficulty(setup.difficulty, random) else null
        SongoGame(setup, computer)
    }
    override val pirogueRaces: GameFactory<PirogueRace> = GameFactory { setup -> PirogueRace.create(setup, random) }
    override val dochiGames: GameFactory<DochiGame> = GameFactory { setup -> DochiGame(setup, random) }
    override val quizGames: GameFactory<QuizGame> = GameFactory { setup ->
        QuizGame(setup, questions.drawRound(QuizGame.QUESTIONS_PER_ROUND, random), random)
    }

    companion object {
        const val XP_PER_LEVEL = 250
        private const val PROGRESS_FILE = "cameroon_quest"
        private const val SETTINGS_FILE = "cameroon_quest_settings"
    }
}
