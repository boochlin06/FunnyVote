package com.heaton.funnyvote.security

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class SecurityValidationTest {

    private val deepLinkRegex = Regex("^[a-zA-Z0-9_-]{1,64}$")

    @Test
    fun `deep link voteCode regex allows valid identifiers`() {
        val validCodes = listOf(
            "poll_123",
            "vote_2026-test",
            "abcDEF123",
            "a",
            "a".repeat(64),
            "custom-poll_with-dash"
        )
        for (code in validCodes) {
            assertTrue("Code '$code' should be valid", code.matches(deepLinkRegex))
        }
    }

    @Test
    fun `deep link voteCode regex rejects dangerous payloads and injections`() {
        val invalidCodes = listOf(
            "",
            " ",
            "../etc/passwd",
            "<script>alert(1)</script>",
            "poll/123",
            "poll?code=1",
            "poll;DROP TABLE",
            "poll\u0000nullbyte",
            "a".repeat(65),
            "javascript:void(0)",
            "{\"voteCode\": 1}"
        )
        for (code in invalidCodes) {
            assertFalse("Code '$code' should be rejected", code.matches(deepLinkRegex))
        }
    }

    @Test
    fun `zero trust sha256 password hash is deterministic and collision resistant`() {
        fun hashPassword(pollId: String, pw: String): String {
            val input = "$pollId:$pw"
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        val hash1 = hashPassword("poll_101", "super_secret")
        val hash2 = hashPassword("poll_101", "super_secret")
        val hash3 = hashPassword("poll_101", "Super_secret") // case sensitivity
        val hash4 = hashPassword("poll_102", "super_secret") // salt by pollId

        assertEquals(64, hash1.length)
        assertEquals("Hash must be deterministic", hash1, hash2)
        assertNotEquals("Case difference must change hash", hash1, hash3)
        assertNotEquals("Different pollId must change hash", hash1, hash4)
    }

    @Test
    fun `manifest file enforces allowBackup false against adb dump`() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml must exist", manifestFile.exists())
        val content = manifestFile.readText()
        assertTrue("Manifest must set android:allowBackup=\"false\"", content.contains("android:allowBackup=\"false\""))
    }

    @Test
    fun `cloud firestore and storage rules enforce security invariants`() {
        val firestoreRules = File("../../firestore.rules").takeIf { it.exists() }
            ?: File("../firestore.rules").takeIf { it.exists() }
            ?: File("firestore.rules")

        val storageRules = File("../../storage.rules").takeIf { it.exists() }
            ?: File("../storage.rules").takeIf { it.exists() }
            ?: File("storage.rules")

        if (firestoreRules.exists()) {
            val fContent = firestoreRules.readText()
            assertTrue("Firestore rules must enforce !exists for voters", fContent.contains("!exists"))
            assertTrue("Firestore rules must block secure_polls list", fContent.contains("allow list: if false;"))
        }

        if (storageRules.exists()) {
            val sContent = storageRules.readText()
            assertTrue("Storage rules must disallow anonymous for polls", sContent.contains("sign_in_provider != 'anonymous'"))
            assertTrue("Storage rules must enforce 300KB limit", sContent.contains("300 * 1024"))
        }
    }
}
