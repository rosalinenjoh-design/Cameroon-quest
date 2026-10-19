package com.cameroonquest.app

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cameroonquest.app.data.Characters
import com.cameroonquest.app.data.Difficulty
import com.cameroonquest.app.data.GameKind
import com.cameroonquest.app.data.Outcome
import com.cameroonquest.app.data.PlayMode
import com.cameroonquest.app.data.PlayerStore
import com.cameroonquest.app.data.RoundReward
import com.cameroonquest.app.ui.components.LocalHapticsEnabled
import com.cameroonquest.app.ui.screens.CharacterDetailScreen
import com.cameroonquest.app.ui.screens.CharacterSelectScreen
import com.cameroonquest.app.ui.screens.DochiScreen
import com.cameroonquest.app.ui.screens.GameSelectScreen
import com.cameroonquest.app.ui.screens.HelpScreen
import com.cameroonquest.app.ui.screens.HomeScreen
import com.cameroonquest.app.ui.screens.LeaderboardScreen
import com.cameroonquest.app.ui.screens.PirogueScreen
import com.cameroonquest.app.ui.screens.ProfileScreen
import com.cameroonquest.app.ui.screens.ProgressScreen
import com.cameroonquest.app.ui.screens.QuizScreen
import com.cameroonquest.app.ui.screens.SettingsScreen
import com.cameroonquest.app.ui.screens.SongoScreen
import com.cameroonquest.app.ui.screens.WelcomeScreen

private object Routes {
    const val WELCOME = "welcome"
    const val CHARACTERS = "characters"
    const val CHARACTER = "character/{id}"
    const val HOME = "home"
    const val GAMES = "games"
    const val SONGO = "songo/{level}/{mode}"
    const val PIROGUE = "pirogue/{level}/{mode}"
    const val DOCHI = "dochi/{level}"
    const val QUIZ = "quiz"
    const val PROFILE = "profile"
    const val LEADERBOARD = "leaderboard"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val HELP = "help"
}

private val levelArgument = navArgument("level") { type = NavType.StringType }
private val modeArgument = navArgument("mode") { type = NavType.StringType }

private fun NavBackStackEntry.difficulty(): Difficulty =
    Difficulty.entries.firstOrNull { it.name == arguments?.getString("level") } ?: Difficulty.EASY

private fun NavBackStackEntry.mode(): PlayMode =
    PlayMode.entries.firstOrNull { it.name == arguments?.getString("mode") } ?: PlayMode.SOLO

@Composable
fun QuestApp(store: PlayerStore) {
    val profile by store.profile.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val start = remember { if (store.profile.value.isReady) Routes.HOME else Routes.WELCOME }

    fun go(route: String) = nav.navigate(route) { launchSingleTop = true }
    fun back() {
        nav.popBackStack()
    }
    fun startOver(route: String) = nav.navigate(route) { popUpTo(nav.graph.id) { inclusive = true } }
    fun backToGames() {
        if (!nav.popBackStack(Routes.GAMES, inclusive = false)) go(Routes.GAMES)
    }
    fun recorder(game: GameKind, mode: PlayMode, difficulty: Difficulty?): (Int, Outcome) -> RoundReward =
        { score, outcome -> store.recordRound(game, score, outcome, mode, difficulty) }

    CompositionLocalProvider(LocalHapticsEnabled provides profile.haptics) {
        NavHost(
            navController = nav,
            startDestination = start,
            enterTransition = { fadeIn(tween(260)) },
            exitTransition = { fadeOut(tween(200)) },
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(onStart = { go(Routes.CHARACTERS) })
            }
            composable(Routes.CHARACTERS) {
                CharacterSelectScreen(onBack = { back() }, onPick = { go("character/${it.id}") })
            }
            composable(Routes.CHARACTER, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val character = Characters.orDefault(entry.arguments?.getString("id"))
                CharacterDetailScreen(
                    character = character,
                    currentName = profile.name,
                    onBack = { back() },
                    onSelect = { name ->
                        store.setName(name)
                        store.setCharacter(character.id)
                        startOver(Routes.HOME)
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    profile = profile,
                    onPlay = { go(Routes.GAMES) },
                    onProgress = { go(Routes.PROGRESS) },
                    onLeaderboard = { go(Routes.LEADERBOARD) },
                    onProfile = { go(Routes.PROFILE) },
                    onSettings = { go(Routes.SETTINGS) },
                    onHelp = { go(Routes.HELP) },
                )
            }
            composable(Routes.GAMES) {
                GameSelectScreen(
                    onBack = { back() },
                    onStart = { game, difficulty, mode ->
                        go(
                            when (game) {
                                GameKind.SONGO -> "songo/${difficulty.name}/${mode.name}"
                                GameKind.PIROGUE -> "pirogue/${difficulty.name}/${mode.name}"
                                GameKind.DOCHI -> "dochi/${difficulty.name}"
                                GameKind.QUIZ -> Routes.QUIZ
                            },
                        )
                    },
                )
            }
            composable(Routes.SONGO, arguments = listOf(levelArgument, modeArgument)) { entry ->
                val difficulty = entry.difficulty()
                val mode = entry.mode()
                SongoScreen(profile, difficulty, mode, recorder(GameKind.SONGO, mode, difficulty), onExit = { backToGames() })
            }
            composable(Routes.PIROGUE, arguments = listOf(levelArgument, modeArgument)) { entry ->
                val difficulty = entry.difficulty()
                val mode = entry.mode()
                PirogueScreen(profile, difficulty, mode, recorder(GameKind.PIROGUE, mode, difficulty), onExit = { backToGames() })
            }
            composable(Routes.DOCHI, arguments = listOf(levelArgument)) { entry ->
                val difficulty = entry.difficulty()
                DochiScreen(profile, difficulty, recorder(GameKind.DOCHI, PlayMode.SOLO, difficulty), onExit = { backToGames() })
            }
            composable(Routes.QUIZ) {
                QuizScreen(profile, recorder(GameKind.QUIZ, PlayMode.SOLO, null), onExit = { backToGames() })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    profile = profile,
                    onBack = { back() },
                    onChangeCharacter = { go(Routes.CHARACTERS) },
                    onProgress = { go(Routes.PROGRESS) },
                    onLeaderboard = { go(Routes.LEADERBOARD) },
                    onSettings = { go(Routes.SETTINGS) },
                    onHome = { if (!nav.popBackStack(Routes.HOME, inclusive = false)) startOver(Routes.HOME) },
                    onGames = { go(Routes.GAMES) },
                )
            }
            composable(Routes.LEADERBOARD) {
                LeaderboardScreen(profile, onBack = { back() })
            }
            composable(Routes.PROGRESS) {
                ProgressScreen(profile, onBack = { back() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    profile = profile,
                    onBack = { back() },
                    onRename = { store.setName(it) },
                    onChangeCharacter = { go(Routes.CHARACTERS) },
                    onHaptics = { store.setHaptics(it) },
                    onHelp = { go(Routes.HELP) },
                    onReset = {
                        store.reset()
                        startOver(Routes.WELCOME)
                    },
                )
            }
            composable(Routes.HELP) {
                HelpScreen(onBack = { back() })
            }
        }
    }
}
