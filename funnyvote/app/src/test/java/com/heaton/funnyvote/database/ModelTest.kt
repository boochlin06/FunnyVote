package com.heaton.funnyvote.database

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ModelTest {

    @Test
    fun testVoteDataProperties() {
        val vote = VoteData()
        vote.id = 1L
        vote.voteCode = "VC_123"
        vote.title = "Favorite Programming Language"
        vote.authorCode = "AUTH_001"
        vote.authorName = "Alice"
        vote.authorIcon = "https://example.com/icon.png"
        vote.setAuthorCodeType("GOOGLE")
        vote.startTime = 1000L
        vote.endTime = 2000L
        vote.minOption = 1
        vote.maxOption = 3
        vote.isUserCanAddOption = true
        vote.isNeedPassword = false
        vote.security = VoteData.SECURITY_PUBLIC
        vote.pollCount = 50
        vote.isFavorite = true
        vote.isPolled = true
        vote.displayOrder = 5
        vote.category = "Tech"
        vote.voteImage = "https://example.com/vote.jpg"
        vote.localImage = 123
        vote.option1Title = "Opt1"
        vote.option1Code = "C1"
        vote.option1Count = 10
        vote.option1Polled = true
        vote.option2Title = "Opt2"
        vote.option2Code = "C2"
        vote.option2Count = 20
        vote.option2Polled = false
        vote.optionTopTitle = "OptTop"
        vote.optionTopCode = "CT"
        vote.optionTopCount = 30
        vote.optionTopPolled = true
        vote.isCanPreviewResult = true
        vote.pollType = "NORMAL"

        assertEquals(1L.toLong(), vote.id)
        assertEquals("VC_123", vote.voteCode)
        assertEquals("Favorite Programming Language", vote.title)
        assertEquals("AUTH_001", vote.authorCode)
        assertEquals("Alice", vote.authorName)
        assertEquals("https://example.com/icon.png", vote.authorIcon)
        assertEquals("GOOGLE", vote.authorCodeType)
        assertEquals(1000L, vote.startTime)
        assertEquals(2000L, vote.endTime)
        assertTrue(vote.isMultiChoice)
        assertEquals(1, vote.minOption)
        assertEquals(3, vote.maxOption)
        assertTrue(vote.isUserCanAddOption)
        assertFalse(vote.isNeedPassword)
        assertEquals(VoteData.SECURITY_PUBLIC, vote.security)
        assertEquals(50, vote.pollCount)
        assertTrue(vote.isFavorite)
        assertTrue(vote.isPolled)
        assertEquals(Integer.valueOf(5), vote.displayOrder)
        assertEquals("Tech", vote.category)
        assertEquals("https://example.com/vote.jpg", vote.voteImage)
        assertEquals(123, vote.localImage)
        assertEquals("Opt1", vote.option1Title)
        assertEquals("C1", vote.option1Code)
        assertEquals(10, vote.option1Count)
        assertTrue(vote.option1Polled)
        assertEquals("Opt2", vote.option2Title)
        assertEquals("C2", vote.option2Code)
        assertEquals(20, vote.option2Count)
        assertFalse(vote.option2Polled)
        assertEquals("OptTop", vote.optionTopTitle)
        assertEquals("CT", vote.optionTopCode)
        assertEquals(30, vote.optionTopCount)
        assertTrue(vote.optionTopPolled)
        assertTrue(vote.isCanPreviewResult)
        assertEquals("NORMAL", vote.pollType)

        val file = File("test.jpg")
        vote.imageFile = file
        assertEquals(file, vote.imageFile)

        val o1 = Option()
        val o2 = Option()
        vote.firstOption = o1
        vote.secondOption = o2
        vote.topOption = o1
        vote.userOption = o2
        assertEquals(o1, vote.firstOption)
        assertEquals(o2, vote.secondOption)
        assertEquals(o1, vote.topOption)
        assertEquals(o2, vote.userOption)
    }

    @Test
    fun testOptionProperties() {
        val option = Option(10L, "VC_123", "Kotlin", 42, "OPT_01", true)
        assertEquals(10L.toLong(), option.id)
        assertEquals("OPT_01", option.code)
        assertEquals("Kotlin", option.title)
        assertEquals("VC_123", option.voteCode)
        assertEquals(Integer.valueOf(42), option.count)
        assertTrue(option.isUserChoiced)

        option.id = 20L
        option.code = "OPT_02"
        option.title = "Java"
        option.voteCode = "VC_456"
        option.count = 15
        option.isUserChoiced = false

        assertEquals(20L.toLong(), option.id)
        assertEquals("OPT_02", option.code)
        assertEquals("Java", option.title)
        assertEquals("VC_456", option.voteCode)
        assertEquals(Integer.valueOf(15), option.count)
        assertFalse(option.isUserChoiced)
        option.dumpDetail()
    }

    @Test
    fun testUserProperties() {
        val user = User()
        user.id = 99L
        user.userID = "U_001"
        user.userName = "Bob"
        user.userCode = "CODE_ABC"
        user.type = User.TYPE_GOOGLE
        user.userIcon = "https://example.com/bob.png"
        user.email = "bob@example.com"
        user.gender = "male"
        user.minAge = 20
        user.maxAge = 30
        user.personalTokenType = "guest"

        assertEquals(99L.toLong(), user.id)
        assertEquals("U_001", user.userID)
        assertEquals("Bob", user.userName)
        assertEquals("CODE_ABC", user.userCode)
        assertEquals(User.TYPE_GOOGLE, user.type)
        assertEquals("https://example.com/bob.png", user.userIcon)
        assertEquals("bob@example.com", user.email)
        assertEquals("male", user.gender)
        assertEquals(20, user.minAge)
        assertEquals(30, user.maxAge)
        assertEquals("guest", user.personalTokenType)
        assertEquals("Google", User.getUserTypeString(User.TYPE_GOOGLE))
        assertEquals("FaceBook", User.getUserTypeString(User.TYPE_FACEBOOK))
        assertEquals("Twitter", User.getUserTypeString(User.TYPE_TWITTER))
        assertEquals("Guest", User.getUserTypeString(User.TYPE_GUEST))
    }

    @Test
    fun testPromotionProperties() {
        val promo = Promotion(5L, "https://example.com/promo.png", "https://funny-vote.com", "Promo Title")
        assertEquals(5L.toLong(), promo.id)
        assertEquals("Promo Title", promo.title)
        assertEquals("https://example.com/promo.png", promo.imageURL)
        assertEquals("https://funny-vote.com", promo.actionURL)

        promo.id = 10L
        promo.title = "New Promo"
        promo.imageURL = "https://example.com/new.png"
        promo.actionURL = "https://example.com"
        assertEquals(10L.toLong(), promo.id)
        assertEquals("New Promo", promo.title)
        assertEquals("https://example.com/new.png", promo.imageURL)
        assertEquals("https://example.com", promo.actionURL)
    }
}
