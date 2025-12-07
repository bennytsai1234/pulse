package com.gemini.music.domain.model

enum class RepeatMode(val value: Int) {
    OFF(0),
    ONE(1),
    ALL(2);

    companion object {
        fun fromInt(value: Int): RepeatMode {
            return when (value) {
                1 -> ONE
                2 -> ALL
                else -> OFF
            }
        }
    }
}
