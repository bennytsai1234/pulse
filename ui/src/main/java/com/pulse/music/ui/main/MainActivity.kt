package com.pulse.music.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.pulse.music.domain.repository.UserPreferencesRepository
import com.pulse.music.core.designsystem.PulseTheme
import com.pulse.music.ui.navigation.MusicNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

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

            PulseTheme(darkTheme = darkTheme, dynamicColor = useDynamicColor) {
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


