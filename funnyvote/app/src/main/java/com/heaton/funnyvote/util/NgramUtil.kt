package com.heaton.funnyvote.util

object NgramUtil {
    /**
     * 將輸入字串切詞為 Bi-gram 陣列，供 Firestore whereArrayContains 模糊比對查詢。
     * 例如：「午餐吃什麼」-> ["午餐", "餐吃", "吃什", "什麼"]
     */
    fun generateBiGrams(text: String): List<String> {
        val clean = text.trim()
        if (clean.length < 2) return if (clean.isEmpty()) emptyList() else listOf(clean)
        val ngrams = mutableSetOf<String>()
        for (i in 0 until clean.length - 1) {
            ngrams.add(clean.substring(i, i + 2).lowercase())
        }
        return ngrams.toList()
    }
}
