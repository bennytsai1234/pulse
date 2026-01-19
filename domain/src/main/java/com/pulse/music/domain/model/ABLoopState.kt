package com.pulse.music.domain.model

/**
 * Represents the state of A-B Loop playback.
 *
 * @property enabled Whether A-B loop is currently active
 * @property aPoint The start point timestamp (in ms)
 * @property bPoint The end point timestamp (in ms)
 */
data class ABLoopState(
    val enabled: Boolean = false,
    val aPoint: Long = C.TIME_UNSET,
    val bPoint: Long = C.TIME_UNSET
) {
    object C {
        const val TIME_UNSET = Long.MIN_VALUE + 1
    }
}
