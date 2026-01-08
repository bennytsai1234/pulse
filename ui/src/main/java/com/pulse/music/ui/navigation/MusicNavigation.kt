package com.pulse.music.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.pulse.music.ui.albumdetail.AlbumDetailScreen
import com.pulse.music.ui.home.HomeScreenRedesigned
import com.pulse.music.ui.nowplaying.NowPlayingScreen
import com.pulse.music.ui.search.SearchScreen
import com.pulse.music.ui.settings.SettingsScreen
import com.pulse.music.ui.albums.AlbumsScreen
import com.pulse.music.ui.stats.StatsScreen
import com.pulse.music.ui.settings.PlaybackSettingsScreen
import com.pulse.music.ui.folder.FolderBrowserScreen
import com.pulse.music.ui.discover.DiscoverScreen
import com.pulse.music.ui.lyrics.LyricsEditorScreen
import com.pulse.music.ui.driving.DrivingModeScreen
import com.pulse.music.ui.settings.crossfade.CrossfadeSettingsScreen

/**
 * Safe popBackStack that checks if we can actually navigate back
 * Prevents crashes and black screens from rapid double-tapping back
 */
fun NavHostController.safePopBackStack(): Boolean {
    return if (this.currentBackStackEntry != null &&
               this.previousBackStackEntry != null) {
        this.popBackStack()
    } else {
        false
    }
}

// Fast navigation animation specs with linear easing for snappy feel
private const val NAV_ANIMATION_DURATION = 150

private val enterTransition = androidx.compose.animation.EnterTransition.None

private val exitTransition = androidx.compose.animation.ExitTransition.None

private val popEnterTransition = androidx.compose.animation.EnterTransition.None

private val popExitTransition = androidx.compose.animation.ExitTransition.None

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object NowPlaying : Screen("now_playing")
    data object Settings : Screen("settings")
    data object Search : Screen("search")
    data object AlbumDetail : Screen("album_detail/{albumId}") {
        const val albumIdArg = "albumId"
        fun createRoute(albumId: Long) = "album_detail/$albumId"
    }
    data object Queue : Screen("queue")
    data object Favorites : Screen("favorites")

    data object PlaylistList : Screen("playlists")
    data object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        const val playlistIdArg = "playlistId"
        fun createRoute(playlistId: Long) = "playlist_detail/$playlistId"
    }
    data object Albums : Screen("albums")
    data object Equalizer : Screen("equalizer/{audioSessionId}") {
        const val audioSessionIdArg = "audioSessionId"
        fun createRoute(audioSessionId: Int) = "equalizer/$audioSessionId"
    }
    data object ArtistDetail : Screen("artist_detail/{artistName}") {
        const val artistNameArg = "artistName"
        fun createRoute(artistName: String) = "artist_detail/${java.net.URLEncoder.encode(artistName, "UTF-8")}"
    }
    data object TagEditor : Screen("tag_editor/{songId}") {
        const val songIdArg = "songId"
        fun createRoute(songId: Long) = "tag_editor/$songId"
    }
    data object Stats : Screen("stats")
    data object PlaybackSettings : Screen("playback_settings")
    data object Folders : Screen("folders")
    data object Discover : Screen("discover")
    data object LyricsEditor : Screen("lyrics_editor/{songId}") {
        const val songIdArg = "songId"
        fun createRoute(songId: Long) = "lyrics_editor/$songId"
    }
    data object DrivingMode : Screen("driving_mode")
    data object CrossfadeSettings : Screen("crossfade_settings")
}

/**
 * Shared Element Key 生成器
 */
object SharedElementKeys {
    fun albumCover(albumId: Long) = "album_cover_$albumId"
    fun albumTitle(albumId: Long) = "album_title_$albumId"
    fun songCover(songId: Long) = "song_cover_$songId"
}

/**
 * CompositionLocal for SharedTransitionScope
 * 讓子組件可以訪問 SharedTransitionScope 來使用 sharedElement modifier
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * CompositionLocal for AnimatedContentScope (from NavHost)
 */
val LocalAnimatedContentScope = compositionLocalOf<AnimatedContentScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MusicNavigation(navController: NavHostController) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = { enterTransition },
                exitTransition = { exitTransition },
                popEnterTransition = { popEnterTransition },
                popExitTransition = { popExitTransition }
            ) {
                composable(Screen.Home.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        HomeScreenRedesigned(
                            onSongClick = { _ ->
                                // Just play, don't navigate
                            },
                            onSettingsClick = {
                                navController.navigate(Screen.Settings.route)
                            },
                            onSearchClick = {
                                navController.navigate(Screen.Search.route)
                            },
                            onPlaylistClick = {
                                navController.navigate(Screen.PlaylistList.route)
                            },
                            onAlbumsClick = {
                                navController.navigate(Screen.Albums.route)
                            },
                            onFavoritesClick = {
                                navController.navigate(Screen.Favorites.route)
                            },
                            onDiscoverClick = {
                                navController.navigate(Screen.Discover.route)
                            },
                            onStatsClick = {
                                navController.navigate(Screen.Stats.route)
                            },
                            onFoldersClick = {
                                navController.navigate(Screen.Folders.route)
                            },
                            onDrivingModeClick = {
                                navController.navigate(Screen.DrivingMode.route)
                            },
                            onPlaybackSettingsClick = {
                                navController.navigate(Screen.PlaybackSettings.route)
                            }
                        )
                    }
                }
                composable(Screen.Settings.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        SettingsScreen(
                            onBackClick = { navController.safePopBackStack() },
                            onInternalEqualizerClick = { sessionId ->
                                navController.navigate(Screen.Equalizer.createRoute(sessionId))
                            }
                        )
                    }
                }
                composable(Screen.Search.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        SearchScreen(
                            onBackClick = { navController.safePopBackStack() },
                            onAlbumClick = { albumId ->
                                navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                            },
                            onArtistClick = { artistName ->
                                navController.navigate(Screen.ArtistDetail.createRoute(artistName))
                            }
                        )
                    }
                }
                composable(Screen.Queue.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        com.pulse.music.ui.queue.QueueScreen(
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }
                composable(Screen.Favorites.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        com.pulse.music.ui.favorites.FavoritesScreen(
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }
                composable(Screen.Albums.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        AlbumsScreen(
                            onBackClick = { navController.safePopBackStack() },
                            onAlbumClick = { albumId ->
                                navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                            }
                        )
                    }
                }
                composable(
                    route = Screen.AlbumDetail.route,
                    arguments = listOf(navArgument(Screen.AlbumDetail.albumIdArg) { type = NavType.LongType })
                ) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        AlbumDetailScreen(
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }
                composable(Screen.PlaylistList.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        com.pulse.music.ui.playlist.list.PlaylistListScreen(
                            onBackClick = { navController.safePopBackStack() },
                            onPlaylistClick = { id: Long ->
                                navController.navigate(Screen.PlaylistDetail.createRoute(id))
                            }
                        )
                    }
                }
                composable(
                    route = Screen.PlaylistDetail.route,
                    arguments = listOf(navArgument(Screen.PlaylistDetail.playlistIdArg) { type = NavType.LongType })
                ) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        com.pulse.music.ui.playlist.detail.PlaylistDetailScreen(
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }
                composable(
                    route = Screen.Equalizer.route,
                    arguments = listOf(navArgument(Screen.Equalizer.audioSessionIdArg) { type = NavType.IntType })
                ) { backStackEntry ->
                    val audioSessionId = backStackEntry.arguments?.getInt(Screen.Equalizer.audioSessionIdArg) ?: 0
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        com.pulse.music.ui.equalizer.EqualizerScreen(
                            audioSessionId = audioSessionId,
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }
                composable(
                    route = Screen.ArtistDetail.route,
                    arguments = listOf(navArgument(Screen.ArtistDetail.artistNameArg) { type = NavType.StringType })
                ) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        com.pulse.music.ui.artist.ArtistDetailScreen(
                            onBackClick = { navController.safePopBackStack() },
                            onAlbumClick = { albumId ->
                                navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                            }
                        )
                    }
                }
                composable(
                    route = Screen.TagEditor.route,
                    arguments = listOf(navArgument(Screen.TagEditor.songIdArg) { type = NavType.LongType })
                ) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        com.pulse.music.ui.tageditor.TagEditorScreen(
                            onNavigateBack = { navController.safePopBackStack() }
                        )
                    }
                }

                // Stats Screen
                composable(route = Screen.Stats.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        StatsScreen(
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }

                // Playback Settings Screen
                composable(route = Screen.PlaybackSettings.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        PlaybackSettingsScreen(
                            onBackClick = { navController.safePopBackStack() },
                            onCrossfadeClick = { navController.navigate(Screen.CrossfadeSettings.route) }
                        )
                    }
                }

                // Crossfade Settings Screen
                composable(route = Screen.CrossfadeSettings.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        CrossfadeSettingsScreen(
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }

                // Folder Browser Screen
                composable(route = Screen.Folders.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        FolderBrowserScreen(
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }

                // Discover Screen (Recommendations)
                composable(route = Screen.Discover.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        DiscoverScreen(
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }

                // Lyrics Editor Screen
                composable(
                    route = Screen.LyricsEditor.route,
                    arguments = listOf(navArgument(Screen.LyricsEditor.songIdArg) { type = NavType.LongType })
                ) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        LyricsEditorScreen(
                            onBackClick = { navController.safePopBackStack() }
                        )
                    }
                }



                // Driving Mode Screen
                composable(route = Screen.DrivingMode.route) {
                    CompositionLocalProvider(LocalAnimatedContentScope provides this) {
                        DrivingModeScreen(
                            onNavigateBack = { navController.safePopBackStack() }
                        )
                    }
                }
            }
        }
    }
}





