package com.example.data.repository

import com.example.data.model.LiveGift
import com.example.data.model.TriviaQuestion
import com.example.data.model.VideoComment
import com.example.data.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VideoRepository {

    val availableGifts = listOf(
        LiveGift("gift_rose", "Rosa SPEI", "🌹", 5.0, "Envía apoyo instantáneo al creador", 0xFFFF2A6D),
        LiveGift("gift_taco", "Taco Legendario", "🌮", 15.0, "¡Sabor mexicano directo a su CLABE!", 0xFFFFB300),
        LiveGift("gift_crown", "Corona Oro", "👑", 50.0, "Destaca como super seguidor VIP", 0xFFFFD700),
        LiveGift("gift_rocket", "Cohete Banxico", "🚀", 100.0, "Despega el stream y monetiza al segundo", 0xFF00E676),
        LiveGift("gift_diamond", "Diamante STP", "💎", 250.0, "La mayor recompensa para el creador", 0xFF05D9E8)
    )

    private val _videos = MutableStateFlow(
        listOf(
            VideoItem(
                id = "vid_01",
                title = "¿Cómo retirar tus recompensas por SPEI en 3 segundos?",
                description = "Tutorial oficial: Mira videos, acumula monedas y transfiérelos a cualquier banco de México sin comisiones. #WatchToEarn #SPEI #FintechMex",
                creatorName = "Sofía Finanzas",
                creatorHandle = "@sofiafintech",
                soundTitle = "Sonido Original - Fintech Beats 2026",
                likesCount = 24890,
                commentsCount = 1420,
                sharesCount = 5310,
                watchRewardPerSecond = 0.30,
                maxRewardPerView = 4.50,
                videoTheme = "fintech",
                sponsorBrand = "STP Banxico",
                trivia = TriviaQuestion(
                    id = "triv_01",
                    sponsorName = "STP Dispersión",
                    question = "¿Cuántos dígitos componen una CLABE interbancaria en México?",
                    options = listOf("16 dígitos", "18 dígitos", "20 dígitos"),
                    correctIndex = 1,
                    rewardMxn = 25.0,
                    explanation = "¡Exacto! La CLABE interbancaria en México tiene 18 dígitos: 3 de banco, 3 de plaza, 11 de cuenta y 1 dígito verificador."
                ),
                comments = listOf(
                    VideoComment("c1", "Mariana Rios", "¡Literal me llegó en 2 segundos a mi Mercado Pago! 🚀", "hace 5m", 320),
                    VideoComment("c2", "Alex CDMX", "¿Es en serio que paga directo? Ahorita retiro", "hace 12m", 154),
                    VideoComment("c3", "Rodrigo Finanzas", "Excelente iniciativa con CLABE directa", "hace 35m", 89)
                )
            ),
            VideoItem(
                id = "vid_02",
                title = "#RetoDoritosMex ¡El nacho con más queso del mundo!",
                description = "Desafío oficial de @DoritosMex. Sube tu video con el hashtag #RetoDoritosMex y gana hasta $10,000 MXN directo a tu cuenta.",
                creatorName = "Doritos México",
                creatorHandle = "@doritosmex",
                soundTitle = "Crunchy Drop - Doritos Sound",
                likesCount = 89450,
                commentsCount = 4210,
                sharesCount = 12900,
                watchRewardPerSecond = 0.25,
                maxRewardPerView = 3.75,
                videoTheme = "gaming",
                sponsorBrand = "Doritos México",
                challengeHashtag = "#RetoDoritosMex",
                trivia = TriviaQuestion(
                    id = "triv_02",
                    sponsorName = "Doritos México",
                    question = "¿Cuál es el premio mayor del #RetoDoritosMex?",
                    options = listOf("$2,000 MXN", "$5,000 MXN", "$10,000 MXN"),
                    correctIndex = 2,
                    rewardMxn = 35.0,
                    explanation = "¡Ganaste $35 MXN! Doritos reparte $50,000 MXN en total con un premio mayor de $10,000 MXN."
                ),
                comments = listOf(
                    VideoComment("c4", "GamerBoy99", "¡Ya subí mi video! Voten por favor 🙏", "hace 2m", 45),
                    VideoComment("c5", "Pao Trends", "Me encanta este reto, el queso se ve irreal", "hace 18m", 62)
                )
            ),
            VideoItem(
                id = "vid_03",
                title = "Setup Cyberpunk 2077 y pantalla OLED vertical 🎮🚀",
                description = "Configuré una estación con refrigeración líquida y luces sincronizadas a los bajos del audio. ¿Qué calificación le das?",
                creatorName = "Pedro Gamer Pro",
                creatorHandle = "@pedrotech",
                soundTitle = "Cyber City - Neon Synthwave",
                likesCount = 61200,
                commentsCount = 2100,
                sharesCount = 6400,
                watchRewardPerSecond = 0.25,
                maxRewardPerView = 3.75,
                videoTheme = "tech",
                sponsorBrand = "HyperGamer Gear",
                trivia = TriviaQuestion(
                    id = "triv_03",
                    sponsorName = "HyperGamer Gear",
                    question = "¿A qué tasa de refresco corre la pantalla de este setup?",
                    options = listOf("90Hz", "144Hz", "240Hz"),
                    correctIndex = 2,
                    rewardMxn = 20.0,
                    explanation = "¡Correcto! Corre a 240Hz para una fluidez insuperable en streaming y juegos."
                ),
                comments = listOf(
                    VideoComment("c8", "Santi Pro", "Increíble iluminación, esos cables neon están locos", "hace 30m", 78)
                )
            ),
            VideoItem(
                id = "vid_04",
                title = "Monetizando con IA: Gemini 2.5 Flash en acción 🧠✨",
                description = "Probamos la moderación automática y recomendación personalizada de videos en tiempo real con Google AI Studio. #GeminiAI",
                creatorName = "Carlos Tech Review",
                creatorHandle = "@carlostech",
                soundTitle = "Neural Beats - Future Audio",
                likesCount = 37800,
                commentsCount = 1200,
                sharesCount = 2900,
                watchRewardPerSecond = 0.20,
                maxRewardPerView = 3.00,
                videoTheme = "fintech",
                comments = listOf(
                    VideoComment("c9", "Mau Live", "La velocidad de respuesta de Gemini 2.5 Flash es sorprendente", "hace 15m", 38)
                )
            ),
            VideoItem(
                id = "vid_05",
                title = "Los mejores tacos de birria con queso en Guadalajara 🌮🔥",
                description = "Si vienes a Jalisco TIENES que probar estos tacos con consomé caliente. ¡Doble tortilla y salsa que pica rico! #FoodieMex #Tacos",
                creatorName = "Taco Hunter GDL",
                creatorHandle = "@tacohunter_gdl",
                soundTitle = "Mariachi Moderno - Jalisco Vibes",
                likesCount = 45120,
                commentsCount = 890,
                sharesCount = 3100,
                watchRewardPerSecond = 0.20,
                maxRewardPerView = 3.00,
                videoTheme = "lifestyle",
                comments = listOf(
                    VideoComment("c6", "Chef Fer", "¡Una joya de puesto! Recomiendo el agua de horchata", "hace 1h", 112)
                )
            )
        )
    )

    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    fun toggleLike(videoId: String) {
        val current = _videos.value.toMutableList()
        val index = current.indexOfFirst { it.id == videoId }
        if (index != -1) {
            val item = current[index]
            val newLiked = !item.isLiked
            val newCount = if (newLiked) item.likesCount + 1 else item.likesCount - 1
            current[index] = item.copy(isLiked = newLiked, likesCount = newCount)
            _videos.value = current
        }
    }

    fun markTriviaAnswered(videoId: String) {
        val current = _videos.value.toMutableList()
        val index = current.indexOfFirst { it.id == videoId }
        if (index != -1) {
            current[index] = current[index].copy(hasAnsweredTrivia = true)
            _videos.value = current
        }
    }

    fun addComment(videoId: String, commentText: String) {
        val current = _videos.value.toMutableList()
        val index = current.indexOfFirst { it.id == videoId }
        if (index != -1) {
            val item = current[index]
            val newComment = VideoComment(
                id = "c_${System.currentTimeMillis()}",
                authorName = "Alex_ReelsMX",
                text = commentText,
                timeAgo = "ahora"
            )
            val newComments = listOf(newComment) + item.comments
            current[index] = item.copy(comments = newComments, commentsCount = item.commentsCount + 1)
            _videos.value = current
        }
    }

    fun reorderVideos(orderedIds: List<String>) {
        val current = _videos.value
        val map = current.associateBy { it.id }
        val reordered = mutableListOf<VideoItem>()
        for (id in orderedIds) {
            map[id]?.let { reordered.add(it) }
        }
        for (item in current) {
            if (!reordered.any { it.id == item.id }) {
                reordered.add(item)
            }
        }
        _videos.value = reordered
    }

    fun addNewVideo(item: VideoItem) {
        _videos.value = listOf(item) + _videos.value
    }
}
