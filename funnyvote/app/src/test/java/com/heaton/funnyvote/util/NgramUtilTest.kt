package com.heaton.funnyvote.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NgramUtilTest {
    @Test
    fun testGenerateBiGrams() {
        val text = "午餐吃什麼"
        val ngrams = NgramUtil.generateBiGrams(text)
        assertEquals(4, ngrams.size)
        assertTrue(ngrams.contains("午餐"))
        assertTrue(ngrams.contains("餐吃"))
        assertTrue(ngrams.contains("吃什"))
        assertTrue(ngrams.contains("什麼"))
    }

    @Test
    fun testShortText() {
        val single = "好"
        assertEquals(listOf("好"), NgramUtil.generateBiGrams(single))

        val empty = ""
        assertTrue(NgramUtil.generateBiGrams(empty).isEmpty())
    }
}
