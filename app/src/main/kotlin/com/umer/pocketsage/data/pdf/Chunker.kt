package com.umer.pocketsage.data.pdf

class Chunker {
    fun chunk(text: String, size: Int = 800, overlap: Int = 120): List<String> {
        if (text.isBlank()) return emptyList()
        val step = size - overlap
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            var end = minOf(start + size, text.length)
            if (end < text.length) {
                // Scan back within the overlap window to avoid cutting mid-word
                val scanFrom = maxOf(end - overlap, start + 1)
                val ws = (end downTo scanFrom).firstOrNull { text[it - 1].isWhitespace() }
                if (ws != null) end = ws
            }
            val chunk = text.substring(start, end).trim()
            if (chunk.isNotEmpty()) chunks.add(chunk)
            start += step
        }
        return chunks
    }
}