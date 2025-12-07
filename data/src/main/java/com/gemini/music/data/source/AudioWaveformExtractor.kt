package com.gemini.music.data.source

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

class AudioWaveformExtractor @Inject constructor(
    private val context: Context
) {

    suspend fun extractWaveform(uri: Uri, samplesCount: Int = 100): List<Int> = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext List(samplesCount) { 0 }
        }

        var audioTrackIndex = -1
        var mimeType = ""
        var durationUs = 0L

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) {
                audioTrackIndex = i
                mimeType = mime
                durationUs = format.getLong(MediaFormat.KEY_DURATION)
                break
            }
        }

        if (audioTrackIndex == -1) {
            extractor.release()
            return@withContext List(samplesCount) { 0 }
        }

        extractor.selectTrack(audioTrackIndex)

        val codec = try {
            MediaCodec.createDecoderByType(mimeType)
        } catch (e: Exception) {
            e.printStackTrace()
            extractor.release()
            return@withContext List(samplesCount) { 0 }
        }

        codec.configure(extractor.getTrackFormat(audioTrackIndex), null, null, 0)
        codec.start()

        val rawAmplitudes = ArrayList<Int>()
        val bufferInfo = MediaCodec.BufferInfo()
        var isEOS = false
        
        // Safety break
        val startTime = System.currentTimeMillis() 

        try {
            while (!isEOS) { // && System.currentTimeMillis() - startTime < 10000 // Timeout 10s
                // Input
                val inputBufferIndex = codec.dequeueInputBuffer(1000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                    if (inputBuffer != null) {
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                // Output
                var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 1000)
                while (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                    if (outputBuffer != null) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        
                        // Process PCM data
                        // Assuming 16-bit PCM
                        val shortBuffer = outputBuffer.asShortBuffer()
                        var sum = 0L
                        var count = 0
                        
                        // Calculate RMS for this chunk
                        while (shortBuffer.hasRemaining()) {
                            val sample = shortBuffer.get()
                            sum += sample * sample
                            count++
                            // Skip some samples for speed?
                            // if (shortBuffer.position() % 10 != 0) continue 
                        }
                        
                        if (count > 0) {
                            val amplitude = sqrt((sum / count).toDouble()).toInt()
                            rawAmplitudes.add(amplitude)
                        }
                    }
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 1000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                codec.stop()
                codec.release()
                extractor.release()
            } catch (e: Exception) {
               // Ignore cleanup errors
            }
        }

        // Downsample
        if (rawAmplitudes.isEmpty()) return@withContext List(samplesCount) { 0 }
        
        val result =  if (rawAmplitudes.size < samplesCount) {
             // Pad with simple interpolation or just fill
             val ratio = samplesCount.toFloat() / rawAmplitudes.size
             List(samplesCount) { i -> rawAmplitudes.getOrElse((i / ratio).toInt()) { 0 } }
        } else {
             // Downsample
             val chunkSize = rawAmplitudes.size / samplesCount
             val chunks = rawAmplitudes.chunked(chunkSize)
             chunks.map { chunk -> 
                 chunk.average().toInt()
             }.take(samplesCount)
        }
        
        // Normalize 0..100 (optional but helpful for UI)
        val max = result.maxOrNull() ?: 1
        return@withContext result.map { (it.toFloat() / max * 100).toInt() }
    }
}
