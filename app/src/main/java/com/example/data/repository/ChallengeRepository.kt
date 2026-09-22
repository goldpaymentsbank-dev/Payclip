package com.example.data.repository

import com.example.data.model.ChallengeCampaign
import com.example.data.model.ChallengeParticipant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChallengeRepository {

    private val _campaigns = MutableStateFlow(
        listOf(
            ChallengeCampaign(
                id = "camp_01",
                brandName = "Doritos México",
                hashtag = "#RetoDoritosMex",
                title = "El nacho con más queso y creatividad",
                description = "Graba un video corto mostrando tu nacho más épico. Los 10 videos con mayor interacción ganan premios en efectivo transferidos por SPEI al instante.",
                totalPrizePoolMxn = 50000.0,
                distributedPrizeMxn = 18500.0,
                participantsCount = 1430,
                topPrizeMxn = 10000.0,
                daysRemaining = 4,
                bannerTheme = "Doritos",
                participants = listOf(
                    ChallengeParticipant(1, "Sofia Dance", "@sofiadance", "1.4M vistas", 14200, 10000.0),
                    ChallengeParticipant(2, "Carlos Cooks", "@carloscooks", "980K vistas", 11300, 5000.0),
                    ChallengeParticipant(3, "GamerBoy99", "@gamerboy99", "740K vistas", 8900, 3000.0),
                    ChallengeParticipant(4, "Pao Trends", "@paotrends", "520K vistas", 6700, 2000.0)
                )
            ),
            ChallengeCampaign(
                id = "camp_02",
                brandName = "Nu México",
                hashtag = "#NuAhorroInteligente",
                title = "Tu mejor hack financiero en 30 segundos",
                description = "Comparte un tip o hábito de ahorro que cambió tus finanzas. Nu premia la educación financiera con transferencias SPEI directas a tu cuenta.",
                totalPrizePoolMxn = 75000.0,
                distributedPrizeMxn = 32000.0,
                participantsCount = 2190,
                topPrizeMxn = 15000.0,
                daysRemaining = 8,
                bannerTheme = "Nu",
                participants = listOf(
                    ChallengeParticipant(1, "Fintech Queen", "@fintechqueen", "2.1M vistas", 24500, 15000.0),
                    ChallengeParticipant(2, "Rodrigo Tips", "@rodrigotips", "1.2M vistas", 16800, 8000.0),
                    ChallengeParticipant(3, "Dani Ahorra", "@daniahorra", "850K vistas", 12100, 5000.0)
                )
            ),
            ChallengeCampaign(
                id = "camp_03",
                brandName = "Nike México",
                hashtag = "#CorreTuCiudad",
                title = "Demuestra tu ruta urbana más extrema",
                description = "Sube tu reel corriendo por los lugares más icónicos de tu ciudad. Recompensas semanales dispersadas vía SPEI cada domingo.",
                totalPrizePoolMxn = 100000.0,
                distributedPrizeMxn = 45000.0,
                participantsCount = 3850,
                topPrizeMxn = 20000.0,
                daysRemaining = 12,
                bannerTheme = "Nike",
                participants = listOf(
                    ChallengeParticipant(1, "Marathoner Max", "@marathonermax", "3.4M vistas", 35000, 20000.0),
                    ChallengeParticipant(2, "Clara Runner", "@clararunner", "1.8M vistas", 19200, 10000.0)
                )
            )
        )
    )

    val campaigns: StateFlow<List<ChallengeCampaign>> = _campaigns.asStateFlow()

    fun joinCampaign(campaignId: String) {
        val current = _campaigns.value.toMutableList()
        val index = current.indexOfFirst { it.id == campaignId }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(
                isJoined = true,
                participantsCount = item.participantsCount + 1
            )
            _campaigns.value = current
        }
    }
}
