package com.heaton.funnyvote.data.remote

import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import java.util.UUID

object MockVoteDataSource {
    fun getInitialSeedData(): List<VoteWithDetails> {
        val now = System.currentTimeMillis()
        return listOf(
            VoteWithDetails(
                vote = VoteEntity(
                    voteCode = "vote_001",
                    title = "2026 年 Android 開發最推薦的架構是什麼？",
                    authorName = "Android架構師",
                    category = "hot",
                    minOption = 1,
                    maxOption = 1,
                    isNeedPassword = false,
                    isFavorite = true,
                    totalVotedCount = 128,
                    createdAt = now - 1000 * 60 * 30
                ),
                options = listOf(
                    OptionEntity(voteCode = "vote_001", optionCode = "opt_101", title = "MVI + Jetpack Compose", count = 89),
                    OptionEntity(voteCode = "vote_001", optionCode = "opt_102", title = "傳統 MVVM + ViewBinding", count = 27),
                    OptionEntity(voteCode = "vote_001", optionCode = "opt_103", title = "MVP (Model-View-Presenter)", count = 8),
                    OptionEntity(voteCode = "vote_001", optionCode = "opt_104", title = "Flutter / KMP 跨平台", count = 4)
                )
            ),
            VoteWithDetails(
                vote = VoteEntity(
                    voteCode = "vote_002",
                    title = "中午團隊聚餐想吃哪種類型？(複選最多 2 項)",
                    authorName = "總務小幫手",
                    category = "hot",
                    minOption = 1,
                    maxOption = 2,
                    isNeedPassword = false,
                    totalVotedCount = 45,
                    createdAt = now - 1000 * 60 * 120
                ),
                options = listOf(
                    OptionEntity(voteCode = "vote_002", optionCode = "opt_201", title = "日式拉麵", count = 22),
                    OptionEntity(voteCode = "vote_002", optionCode = "opt_202", title = "美式漢堡薯條", count = 18),
                    OptionEntity(voteCode = "vote_002", optionCode = "opt_203", title = "韓式炸雞配年糕", count = 30),
                    OptionEntity(voteCode = "vote_002", optionCode = "opt_204", title = "健康低卡溫沙拉", count = 5)
                )
            ),
            VoteWithDetails(
                vote = VoteEntity(
                    voteCode = "vote_003",
                    title = "週末程式黑客松最佳主題投票 (加密投票)",
                    authorName = "DevCommunity",
                    category = "new",
                    minOption = 1,
                    maxOption = 1,
                    isNeedPassword = true,
                    password = "123",
                    totalVotedCount = 16,
                    createdAt = now - 1000 * 60 * 10
                ),
                options = listOf(
                    OptionEntity(voteCode = "vote_003", optionCode = "opt_301", title = "AI Agentic Coding 應用開發", count = 10),
                    OptionEntity(voteCode = "vote_003", optionCode = "opt_302", title = "Web3 與去中心化身分驗證", count = 4),
                    OptionEntity(voteCode = "vote_003", optionCode = "opt_303", title = "邊緣運算 (Edge AI) 智慧相機", count = 2)
                )
            ),
            VoteWithDetails(
                vote = VoteEntity(
                    voteCode = "vote_004",
                    title = "你平常最常使用的 Kotlin 異步機制？",
                    authorName = "KotlinFan",
                    category = "new",
                    minOption = 1,
                    maxOption = 1,
                    isNeedPassword = false,
                    isFavorite = true,
                    totalVotedCount = 67,
                    createdAt = now - 1000 * 60 * 300
                ),
                options = listOf(
                    OptionEntity(voteCode = "vote_004", optionCode = "opt_401", title = "StateFlow / SharedFlow", count = 48),
                    OptionEntity(voteCode = "vote_004", optionCode = "opt_402", title = "Channel", count = 12),
                    OptionEntity(voteCode = "vote_004", optionCode = "opt_403", title = "RxJava 2 / 3", count = 5),
                    OptionEntity(voteCode = "vote_004", optionCode = "opt_404", title = "傳統 Callback 介面", count = 2)
                )
            )
        )
    }

    fun createVote(
        title: String,
        options: List<String>,
        isPrivate: Boolean,
        password: String?,
        isMultiChoice: Boolean,
        author: String = "目前使用者"
    ): VoteWithDetails {
        val code = "vote_${UUID.randomUUID().toString().take(8)}"
        val vote = VoteEntity(
            voteCode = code,
            title = title,
            authorName = author,
            category = "new",
            minOption = 1,
            maxOption = if (isMultiChoice) options.size.coerceAtLeast(2) else 1,
            isNeedPassword = isPrivate,
            password = password,
            totalVotedCount = 0,
            createdAt = System.currentTimeMillis()
        )
        val optionEntities = options.mapIndexed { index, optTitle ->
            OptionEntity(
                voteCode = code,
                optionCode = "opt_${code}_$index",
                title = optTitle,
                count = 0
            )
        }
        return VoteWithDetails(vote = vote, options = optionEntities)
    }
}
