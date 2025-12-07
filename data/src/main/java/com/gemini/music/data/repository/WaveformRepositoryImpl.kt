package com.gemini.music.data.repository

import android.content.Context
import com.gemini.music.domain.repository.WaveformRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import linc.com.amplituda.Amplituda
import linc.com.amplituda.AmplitudaResult
import linc.com.amplituda.Cache
import linc.com.amplituda.callback.AmplitudaErrorListener
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WaveformRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WaveformRepository {

    // DISABLED: Amplituda native library causes SIGSEGV crash on x86_64 emulators
    // private val amplituda = Amplituda(context)

    override suspend fun extractWaveform(filePath: String): List<Int> = withContext(Dispatchers.IO) {
        // Temporarily return empty list to prevent native crash
        // The UI will fall back to animated random waveform display
        // TODO: Re-enable when running on real ARM devices or find alternative library
        return@withContext emptyList()
        
        /* Original implementation - disabled due to native crash
        val file = File(filePath)
        if (!file.exists()) return@withContext emptyList()

        return@withContext suspendCoroutine { continuation ->
            amplituda.processAudio(filePath)
                .get({ result ->
                    val amplitudes = result.amplitudesAsList()
                    continuation.resume(amplitudes)
                }, { error ->
                    error.printStackTrace()
                    continuation.resume(emptyList())
                })
        }
        */
    }
}
