package com.example.data.model

data class ChallengeParticipant(
    val rank: Int,
    val creatorName: String,
    val handle: String,
    val viewsCount: String,
    val votesCount: Int,
    val estimatedPayoutMxn: Double
)

data class ChallengeCampaign(
    val id: String,
    val brandName: String,
    val hashtag: String,
    val title: String,
    val description: String,
    val totalPrizePoolMxn: Double,
    val distributedPrizeMxn: Double,
    val participantsCount: Int,
    val topPrizeMxn: Double,
    val daysRemaining: Int,
    val bannerTheme: String,
    val participants: List<ChallengeParticipant> = emptyList(),
    val isJoined: Boolean = false
)
