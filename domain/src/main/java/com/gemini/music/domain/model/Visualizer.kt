package com.gemini.music.domain.model

/**
 * 視覺化類型
 */
enum class VisualizerType {
    BARS,           // 頻譜條
    WAVE,           // 波形
    CIRCLE,         // 圓形頻譜
    PARTICLE,       // 粒子效果
    LINE            // 線條波動
}

/**
 * 視覺化設定
 */
data class VisualizerSettings(
    val type: VisualizerType = VisualizerType.BARS,
    val enabled: Boolean = false,
    val sensitivity: Float = 1.0f,  // 0.5 - 2.0
    val colorMode: VisualizerColorMode = VisualizerColorMode.GRADIENT,
    val smoothing: Float = 0.8f     // 0.0 - 1.0
)

/**
 * 視覺化顏色模式
 */
enum class VisualizerColorMode {
    SOLID,          // 單一顏色
    GRADIENT,       // 漸變
    DYNAMIC,        // 跟隨專輯封面
    RAINBOW         // 彩虹
}

/**
 * 視覺化數據
 */
data class VisualizerData(
    val fftData: FloatArray = FloatArray(256),      // FFT 數據
    val waveformData: FloatArray = FloatArray(256), // 波形數據
    val peakFrequency: Float = 0f,
    val averageIntensity: Float = 0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VisualizerData
        return fftData.contentEquals(other.fftData) &&
               waveformData.contentEquals(other.waveformData)
    }

    override fun hashCode(): Int {
        var result = fftData.contentHashCode()
        result = 31 * result + waveformData.contentHashCode()
        return result
    }
}
