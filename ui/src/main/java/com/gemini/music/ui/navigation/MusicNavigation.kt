package com.gemini.music.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.gemini.music.ui.albumdetail.AlbumDetailScreen
import com.gemini.music.ui.home.HomeScreen
import com.gemini.music.ui.nowplaying.NowPlayingScreen
import com.gemini.music.ui.search.SearchScreen
import com.gemini.music.ui.settings.SettingsScreen
import com.gemini.music.ui.albums.AlbumsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object NowPlaying : Screen("now_playing")
    data object Settings : Screen("settings")
    data object Search : Screen("search")
    data object AlbumDetail : Screen("album_detail/{albumId}") {
        const val albumIdArg = "albumId"
        fun createRoute(albumId: Long) = "album_detail/$albumId"
    }
    data object PlaylistList : Screen("playlists")
    data object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        const val playlistIdArg = "playlistId"
        fun createRoute(playlistId: Long) = "playlist_detail/$playlistId"
    }
    data object Albums : Screen("albums")
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
                },
                onPlaylistClick = {
                    navController.navigate(Screen.PlaylistList.route)
                },
                onAlbumsClick = {
                    navController.navigate(Screen.Albums.route)
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
        composable(Screen.Albums.route) {
            AlbumsScreen(
                onBackClick = { navController.popBackStack() },
                onAlbumClick = { albumId ->
                     navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                }
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
        composable(Screen.PlaylistList.route) {
            com.gemini.music.ui.playlist.list.PlaylistListScreen(
                onBackClick = { navController.popBackStack() },
                onPlaylistClick = { id: Long ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument(Screen.PlaylistDetail.playlistIdArg) { type = NavType.LongType })
        ) {
            com.gemini.music.ui.playlist.detail.PlaylistDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
