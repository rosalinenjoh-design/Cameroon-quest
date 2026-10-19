package com.cameroonquest.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cameroonquest.app.di.AppContainer
import com.cameroonquest.app.domain.model.Difficulty
import com.cameroonquest.app.domain.model.GameKind
import com.cameroonquest.app.domain.model.GameSetup
import com.cameroonquest.app.domain.model.Language
import com.cameroonquest.app.domain.model.PlayMode
import com.cameroonquest.app.ui.components.LocalHapticsEnabled
import com.cameroonquest.app.ui.screens.games.DochiScreen
import com.cameroonquest.app.ui.screens.games.GameSelectScreen
import com.cameroonquest.app.ui.screens.games.PirogueScreen
import com.cameroonquest.app.ui.screens.games.QuizScreen
import com.cameroonquest.app.ui.screens.games.SongoScreen
import com.cameroonquest.app.ui.screens.home.HomeScreen
import com.cameroonquest.app.ui.screens.onboarding.CharacterDetailScreen
import com.cameroonquest.app.ui.screens.onboarding.CharacterSelectScreen
import com.cameroonquest.app.ui.screens.onboarding.WelcomeScreen
import com.cameroonquest.app.ui.screens.player.HelpScreen
import com.cameroonquest.app.ui.screens.player.LeaderboardScreen
import com.cameroonquest.app.ui.screens.player.ProfileScreen
import com.cameroonquest.app.ui.screens.player.ProgressScreen
import com.cameroonquest.app.ui.screens.player.SettingsScreen
import com.cameroonquest.app.ui.style.GameStyles
import com.cameroonquest.app.ui.style.LocalLanguage
import com.cameroonquest.app.ui.viewmodel.DochiViewModel
import com.cameroonquest.app.ui.viewmodel.LeaderboardViewModel
import com.cameroonquest.app.ui.viewmodel.PirogueViewModel
import com.cameroonquest.app.ui.viewmodel.PlayerViewModel
import com.cameroonquest.app.ui.viewmodel.QuizViewModel
import com.cameroonquest.app.ui.viewmodel.SongoViewModel

/** Every screen of the app and how to build its route. */
sealed class Destination(val route: String) {
    data object Welcome : Destination("welcome")
    data object Characters : Destination("characters")
    data object CharacterDetail : Destination("character/{$ARG_ID}") {
        fun of(id: String) = "character/$id"
    }
    data object Home : Destination("home")
    data object Games : Destination("games")
    data object Play : Destination("play/{$ARG_KIND}/{$ARG_LEVEL}/{$ARG_MODE}") {
        fun of(setup: GameSetup) = "play/${setup.kind.name}/${setup.difficulty.name}/${setup.mode.name}"
    }
    data object Profile : Destination("profile")
    data object Leaderboard : Destination("leaderboard")
    data object Progress : Destination("progress")
    data object Settings : Destination("settings")
    data object Help : Destination("help")

    companion object {
        const val ARG_ID = "id"
        const val ARG_KIND = "kind"
        const val ARG_LEVEL = "level"
        const val ARG_MODE = "mode"
    }
}

@Composable
fun QuestApp(container: AppContainer) {
    val player = viewModel<PlayerViewModel>(factory = PlayerViewModel.factory(container))
    val state by player.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val start = remember { if (state.profile.hasIdentity) Destination.Home.route else Destination.Welcome.route }
    val language = Language.fromTag(LocalConfiguration.current.locales[0].language)

    CompositionLocalProvider(LocalHapticsEnabled provides state.haptics, LocalLanguage provides language) {
        NavHost(
            navController = nav,
            startDestination = start,
            enterTransition = { fadeIn(tween(260)) },
            exitTransition = { fadeOut(tween(200)) },
        ) {
            composable(Destination.Welcome.route) {
                WelcomeScreen(onStart = { nav.go(Destination.Characters.route) })
            }
            composable(Destination.Characters.route) {
                CharacterSelectScreen(
                    characters = player.characters,
                    onBack = { nav.popBackStack() },
                    onPick = { nav.go(Destination.CharacterDetail.of(it.id)) },
                )
            }
            composable(
                Destination.CharacterDetail.route,
                arguments = listOf(navArgument(Destination.ARG_ID) { type = NavType.StringType }),
            ) { entry ->
                val character = player.character(entry.arguments?.getString(Destination.ARG_ID))
                CharacterDetailScreen(
                    character = character,
                    currentName = state.profile.name,
                    onBack = { nav.popBackStack() },
                    onSelect = { name ->
                        player.chooseCharacter(character.id, name)
                        nav.startOver(Destination.Home.route)
                    },
                )
            }
            composable(Destination.Home.route) {
                HomeScreen(
                    state = state,
                    onPlay = { nav.go(Destination.Games.route) },
                    onProgress = { nav.go(Destination.Progress.route) },
                    onLeaderboard = { nav.go(Destination.Leaderboard.route) },
                    onProfile = { nav.go(Destination.Profile.route) },
                    onSettings = { nav.go(Destination.Settings.route) },
                    onHelp = { nav.go(Destination.Help.route) },
                )
            }
            composable(Destination.Games.route) {
                GameSelectScreen(
                    onBack = { nav.popBackStack() },
                    onStart = { setup -> nav.go(Destination.Play.of(setup)) },
                )
            }
            composable(
                Destination.Play.route,
                arguments = listOf(Destination.ARG_KIND, Destination.ARG_LEVEL, Destination.ARG_MODE).map {
                    navArgument(it) { type = NavType.StringType }
                },
            ) { entry ->
                val setup = entry.gameSetup()
                val onExit = { nav.backToGames() }
                when (setup.kind) {
                    GameKind.SONGO -> SongoScreen(
                        viewModel = viewModel<SongoViewModel>(factory = SongoViewModel.factory(container, setup)),
                        player = state,
                        rival = player.rivalFor(GameStyles.of(setup.kind).rivalId),
                        onExit = onExit,
                    )
                    GameKind.PIROGUE -> PirogueScreen(
                        viewModel = viewModel<PirogueViewModel>(factory = PirogueViewModel.factory(container, setup)),
                        player = state,
                        rival = player.rivalFor(GameStyles.of(setup.kind).rivalId),
                        onExit = onExit,
                    )
                    GameKind.DOCHI -> DochiScreen(
                        viewModel = viewModel<DochiViewModel>(factory = DochiViewModel.factory(container, setup)),
                        player = state,
                        throwers = player.characters.filter { it.id != state.character.id }.take(2),
                        onExit = onExit,
                    )
                    GameKind.QUIZ -> QuizScreen(
                        viewModel = viewModel<QuizViewModel>(factory = QuizViewModel.factory(container, setup)),
                        catalog = container.characters,
                        onExit = onExit,
                    )
                }
            }
            composable(Destination.Profile.route) {
                ProfileScreen(
                    state = state,
                    onBack = { nav.popBackStack() },
                    onChangeCharacter = { nav.go(Destination.Characters.route) },
                    onProgress = { nav.go(Destination.Progress.route) },
                    onLeaderboard = { nav.go(Destination.Leaderboard.route) },
                    onSettings = { nav.go(Destination.Settings.route) },
                    onHome = { if (!nav.popBackStack(Destination.Home.route, inclusive = false)) nav.startOver(Destination.Home.route) },
                    onGames = { nav.go(Destination.Games.route) },
                )
            }
            composable(Destination.Leaderboard.route) {
                LeaderboardScreen(
                    viewModel = viewModel<LeaderboardViewModel>(factory = LeaderboardViewModel.factory(container)),
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Destination.Progress.route) {
                ProgressScreen(state = state, onBack = { nav.popBackStack() })
            }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    state = state,
                    language = language,
                    onBack = { nav.popBackStack() },
                    onRename = { player.rename(it) },
                    onChangeCharacter = { nav.go(Destination.Characters.route) },
                    onHaptics = { player.setHaptics(it) },
                    onLanguage = { player.setLanguage(it) },
                    onHelp = { nav.go(Destination.Help.route) },
                    onReset = {
                        player.resetProgress()
                        nav.startOver(Destination.Welcome.route)
                    },
                )
            }
            composable(Destination.Help.route) {
                HelpScreen(xpPerLevel = state.level.required, onBack = { nav.popBackStack() })
            }
        }
    }
}

/** Reads the game setup from the route, falling back to safe values if the route was tampered with. */
private fun NavBackStackEntry.gameSetup(): GameSetup {
    val args = arguments
    val kind = GameKind.entries.firstOrNull { it.name == args?.getString(Destination.ARG_KIND) } ?: GameKind.QUIZ
    val difficulty = Difficulty.entries.firstOrNull { it.name == args?.getString(Destination.ARG_LEVEL) } ?: Difficulty.EASY
    val mode = PlayMode.entries.firstOrNull { it.name == args?.getString(Destination.ARG_MODE) }
        ?.takeIf { kind.allowsTwoPlayers } ?: PlayMode.SOLO
    return GameSetup(kind, difficulty, mode)
}

private fun NavController.go(route: String) = navigate(route) { launchSingleTop = true }

private fun NavController.startOver(route: String) = navigate(route) { popUpTo(graph.id) { inclusive = true } }

private fun NavController.backToGames() {
    if (!popBackStack(Destination.Games.route, inclusive = false)) go(Destination.Games.route)
}
