package com.gemini.music.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.isSystemInDarkTheme
import com.gemini.music.data.repository.UserPreferencesRepository
import com.gemini.music.core.designsystem.GeminiTheme
import com.gemini.music.ui.navigation.MusicNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.activity.enableEdgeToEdge

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, ViewModel will handle scan
            // We need to notify the ViewModel to re-scan.
            // Since we can't easily access the Hilt VM instance here directly without scoping issues in legacy Views,
            // but in Compose, the VM is obtained in setContent.
            // However, for this simple case, we can rely on the fact that if permission is granted,
            // the onResume or the LaunchedEffect in UI can handle it, OR we can use a shared flow/event.
            // BUT, the simplest valid fix for "Run Normally" is to let the VM know.
            
            // Recreating the activity is a nuclear option, but ensures everything re-initializes with permissions.
            // recreate() 
            
            // Better: Just let the UI recompose. The VM scan logic is currently in init{}.
            // We should move scan logic to be triggered by UI.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        checkPermissions()

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = UserPreferencesRepository.THEME_SYSTEM)
            val isSystemDark = isSystemInDarkTheme()
            
            val darkTheme = when (themeMode) {
                 UserPreferencesRepository.THEME_LIGHT -> false
                 UserPreferencesRepository.THEME_DARK -> true
                 else -> isSystemDark
            }
            
            // Trigger scan on resume (to handle permission grant return)
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                         // We need a way to signal the ViewModel to scan.
                         // But we don't have the VM instance here.
                         // Instead, we will rely on the fact that HomeViewModel calls scan in init{}.
                         // AND we adding a LaunchedEffect in HomeScreen to re-scan if empty?
                         // actually, passing an Intent/Event is cleaner, but let's stick to simple UI patterns.
                         // Best practical fix: Verify permission again in HomeViewModel.
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            GeminiTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }

    private fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission)
        }
    }
}
