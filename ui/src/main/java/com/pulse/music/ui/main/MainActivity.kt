package com.pulse.music.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.pulse.music.domain.repository.UserPreferencesRepository
import com.pulse.music.core.designsystem.PulseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.pulse.music.ui.theme.DynamicThemeHandler
import com.pulse.music.ui.theme.LocalDynamicTheme
import androidx.compose.runtime.CompositionLocalProvider

import android.view.WindowManager

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    
    @Inject
    lateinit var musicRepository: com.pulse.music.domain.repository.MusicRepository

    // Observable permission state for the UI
    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        _permissionGranted.value = allGranted

        if (allGranted) {
            // Permission granted - trigger rescan via broadcast or event
            sendBroadcast(android.content.Intent("com.pulse.music.action.PERMISSION_GRANTED"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen
        installSplashScreen()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initial permission check
        _permissionGranted.value = hasRequiredPermissions()
        if (!_permissionGranted.value) {
            requestPermissions()
        }

        // Observe Screen Settings
        lifecycleScope.launch {
            userPreferencesRepository.keepScreenOn.collect { keepOn ->
                if (keepOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        lifecycleScope.launch {
            userPreferencesRepository.isRotationLocked.collect { locked ->
                if (locked) {
                    requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED
                } else {
                    requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = UserPreferencesRepository.THEME_SYSTEM)
            val useDynamicColor by userPreferencesRepository.useDynamicColor.collectAsState(initial = false)
            val isSystemDark = isSystemInDarkTheme()
            val hasPermission by permissionGranted.collectAsState()
            
            val darkTheme = when (themeMode) {
                 UserPreferencesRepository.THEME_LIGHT -> false
                 UserPreferencesRepository.THEME_DARK -> true
                 else -> isSystemDark
            }

            // Provide the dynamic theme state
            CompositionLocalProvider(LocalDynamicTheme provides com.pulse.music.ui.theme.DynamicThemeState()) {
                val viewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val musicState: com.pulse.music.domain.model.MusicState by viewModel.musicState.collectAsState(
                    initial = com.pulse.music.domain.model.MusicState()
                )
                val currentSong = musicState.currentSong
                
                DynamicThemeHandler(artworkUri = currentSong?.albumArtUri) { seedColor ->
                    PulseTheme(
                        darkTheme = darkTheme, 
                        dynamicColor = useDynamicColor,
                        seedColor = if (useDynamicColor) seedColor else null 
                    ) {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            if (hasPermission) {
                                MainScreen()
                            } else {
                                PermissionRequiredScreen(
                                    onRequestPermission = { requestPermissions() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if permissions were revoked while app was in background
        val currentlyGranted = hasRequiredPermissions()
        if (_permissionGranted.value && !currentlyGranted) {
            // Permission was revoked!
            _permissionGranted.value = false
        } else if (!_permissionGranted.value && currentlyGranted) {
            // Permission was granted outside the app (Settings)
            _permissionGranted.value = true
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val permissionsToRequest = permissions.filter {
             ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
