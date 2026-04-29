package com.umer.pocketsage.data.embedding

class BertTokenizer(vocab: List<String>) {

    private val tokenToId: Map<String, Int> = buildMap {
        vocab.forEachIndexed { i, token -> put(token, i) }
    }

    fun encode(text: String, maxLen: Int = 128): Triple<IntArray, IntArray, IntArray> {
        val tokens = tokenize(text)
        val truncated = if (tokens.size > maxLen - 2) tokens.subList(0, maxLen - 2) else tokens

        val ids = buildList {
            add(CLS_ID)
            addAll(truncated.map { tokenToId[it] ?: UNK_ID })
            add(SEP_ID)
        }

        val inputIds    = IntArray(maxLen) { if (it < ids.size) ids[it] else PAD_ID }
        val attnMask    = IntArray(maxLen) { if (it < ids.size) 1 else 0 }
        val tokenTypeIds = IntArray(maxLen) { 0 }

        return Triple(inputIds, attnMask, tokenTypeIds)
    }

    private fun tokenize(text: String): List<String> =
        splitOnWhitespaceAndPunct(text.lowercase().trim()).flatMap { wordPiece(it) }

    private fun splitOnWhitespaceAndPunct(text: String): List<String> {
        val result = mutableListOf<String>()
        val cur = StringBuilder()
        for (ch in text) {
            when {
                ch.isWhitespace() -> {
                    if (cur.isNotEmpty()) { result += cur.toString(); cur.clear() }
                }
                isPunct(ch) -> {
                    if (cur.isNotEmpty()) { result += cur.toString(); cur.clear() }
                    result += ch.toString()
                }
                else -> cur.append(ch)
            }
        }
        if (cur.isNotEmpty()) result += cur.toString()
        return result
    }

    // Standard BERT WordPiece: if no prefix can be matched at any position, whole word → [UNK].
    private fun wordPiece(word: String): List<String> {
        if (word.isEmpty()) return emptyList()
        val subtokens = mutableListOf<String>()
        var start = 0
        while (start < word.length) {
            var end = word.length
            var match = ""
            while (start < end) {
                val sub = if (start == 0) word.substring(0, end) else "##${word.substring(start, end)}"
                if (sub in tokenToId) { match = sub; break }
                end--
            }
            if (match.isEmpty()) return listOf(UNK_TOKEN)
            subtokens += match
            start = end
        }
        return subtokens
    }

    // ASCII punctuation ranges (33–47, 58–64, 91–96, 123–126).
    private fun isPunct(ch: Char): Boolean {
        val cp = ch.code
        return (cp in 33..47) || (cp in 58..64) || (cp in 91..96) || (cp in 123..126)
    }

    companion object {
        const val PAD_ID = 0
        const val UNK_ID = 100
        const val CLS_ID = 101
        const val SEP_ID = 102
        private const val UNK_TOKEN = "[UNK]"
    }
}