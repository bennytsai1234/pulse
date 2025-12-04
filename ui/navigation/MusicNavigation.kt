package com.sigma.music.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.sigma.music.ui.albumdetail.AlbumDetailScreen
import com.sigma.music.ui.home.HomeScreen
import com.sigma.music.ui.nowplaying.NowPlayingScreen
import com.sigma.music.ui.search.SearchScreen
import com.sigma.music.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object NowPlaying : Screen("now_playing")
    data object Settings : Screen("settings")
    data object Search : Screen("search")
    data object AlbumDetail : Screen("album_detail/{albumId}") {
        const val albumIdArg = "albumId"
        fun createRoute(albumId: Long) = "album_detail/$albumId"
    }
}

@Composable
fun MusicNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSongClick = { _ -> 
                    // Just play, don't navigate
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                }
            )
        }
        // NowPlayingScreen is handled by MainScreen overlay, no longer a navigation destination
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(navArgument(Screen.AlbumDetail.albumIdArg) { type = NavType.LongType })
        ) {
            AlbumDetailScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { _ -> 
                    // Just play, don't navigate
                }
            )
        }
    }
}
