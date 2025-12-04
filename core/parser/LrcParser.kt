package com.sigma.music.core.parser

import com.sigma.music.domain.model.LyricLine

object LrcParser {
    // \[(\d{2}):(\d{2})\.(\d{2,3})]
    private val TIME_REGEX = """\[(\d{2}):(\d{2})\.(\d{2,3})]""".toRegex()

    fun parse(lrcContent: String): List<LyricLine> {
        val lines = lrcContent.lines()
        val lyrics = mutableListOf<LyricLine>()

        for (line in lines) {
            if (line.isBlank()) continue

            // 匹配時間標籤
            val matches = TIME_REGEX.findAll(line)
            if (matches.none()) continue
            
            // 提取歌詞文字 (移除所有時間標籤)
            val text = line.replace(TIME_REGEX, "").trim()
            
            // 同一行可能有多個時間標籤 (例如重複的歌詞)
            for (match in matches) {
                val minutes = match.groupValues[1].toLong()
                val seconds = match.groupValues[2].toLong()
                val millisStr = match.groupValues[3]
                // 處理 2位或3位毫秒
                val millis = if (millisStr.length == 2) {
                    millisStr.toLong() * 10
                } else {
                    millisStr.toLong()
                }

                val startTime = (minutes * 60 + seconds) * 1000 + millis
                lyrics.add(LyricLine(startTime, text))
            }
        }

        return lyrics.sortedBy { it.startTime }
    }
}
