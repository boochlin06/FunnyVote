package com.heaton.funnyvote.utils

import org.junit.Assert.*
import org.junit.Test

class UtilTest {

    @Test
    fun testGetDate() {
        val timestamp = 1609459200000L // 2021-01-01 00:00:00 UTC
        val formatted = Util.getDate(timestamp, "yyyy")
        assertTrue(formatted.contains("2021") || formatted.contains("2020"))
    }

    @Test
    fun testBundleKey() {
        assertEquals("VOTE_ID", Util.BUNDLE_KEY_VOTE_CODE)
    }
}
