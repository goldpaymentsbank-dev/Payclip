package com.example.data.model

data class TriviaQuestion(
    val id: String,
    val sponsorName: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val rewardMxn: Double,
    val explanation: String
)

data class LiveGift(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val costMxn: Double,
    val description: String,
    val animationColorHex: Long
)

data class VideoComment(
    val id: String,
    val authorName: String,
    val text: String,
    val timeAgo: String,
    val likesCount: Int = 0
)

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val creatorName: String,
    val creatorHandle: String,
    val creatorAvatarUrl: String = "",
    val soundTitle: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val watchRewardPerSecond: Double = 0.25, // MXN earned per second watched
    val maxRewardPerView: Double = 3.50,
    val trivia: TriviaQuestion? = null,
    val sponsorBrand: String? = null,
    val challengeHashtag: String? = null,
    val videoTheme: String = "fintech", // "fintech", "gaming", "tech", "lifestyle", "food"
    val comments: List<VideoComment> = emptyList(),
    var isLiked: Boolean = false,
    var hasAnsweredTrivia: Boolean = false
)
