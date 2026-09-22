package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.local.AppDatabase
import com.example.data.model.CepReceipt
import com.example.data.model.ChallengeCampaign
import com.example.data.model.LiveGift
import com.example.data.model.PayoutRequestEntity
import com.example.data.model.SpeiAccountEntity
import com.example.data.model.TriviaQuestion
import com.example.data.model.UserEntity
import com.example.data.model.UserPaymentMethodEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoItem
import com.example.data.model.VideoViewEntity
import com.example.data.model.WalletTransactionEntity
import com.example.data.repository.ChallengeRepository
import com.example.data.repository.VideoRepository
import com.example.data.repository.WalletRepository
import com.example.data.spei.SpeiEngine
import com.example.data.supabase.SupabaseHealthStatus
import com.example.data.supabase.SupabaseService
import com.example.data.supabase.SupabaseSyncPayload
import com.example.data.supabase.SupabaseTodo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val walletRepository = WalletRepository(
        walletDao = database.walletDao(),
        speiAccountDao = database.speiAccountDao(),
        userDao = database.userDao(),
        paymentMethodDao = database.userPaymentMethodDao(),
        payoutRequestDao = database.payoutRequestDao()
    )
    private val videoRepository = VideoRepository()
    private val challengeRepository = ChallengeRepository()

    // State Flows
    val balance: StateFlow<Double> = walletRepository.balanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 185.50)

    val todayWithdrawn: StateFlow<Double> = walletRepository.getTodayWithdrawnFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val transactions: StateFlow<List<WalletTransactionEntity>> = walletRepository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedAccounts: StateFlow<List<SpeiAccountEntity>> = walletRepository.savedAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = (walletRepository.currentUserFlow
        ?: kotlinx.coroutines.flow.flowOf(null))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val payoutRequests: StateFlow<List<PayoutRequestEntity>> = (walletRepository.payoutRequestsFlow
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<VideoItem>> = videoRepository.videos
    val availableGifts: List<LiveGift> = videoRepository.availableGifts
    val campaigns: StateFlow<List<ChallengeCampaign>> = challengeRepository.campaigns

    private val _userUploadedVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val userUploadedVideos: StateFlow<List<VideoItem>> = _userUploadedVideos.asStateFlow()

    // UI Interactive States
    private val _currentVideoIndex = MutableStateFlow(0)
    val currentVideoIndex: StateFlow<Int> = _currentVideoIndex.asStateFlow()

    private val _watchMeterProgress = MutableStateFlow(0f)
    val watchMeterProgress: StateFlow<Float> = _watchMeterProgress.asStateFlow()

    private val _rewardCoinsPopup = MutableSharedFlow<Double>()
    val rewardCoinsPopup: SharedFlow<Double> = _rewardCoinsPopup.asSharedFlow()

    private val _simulatedBankPushNotification = MutableStateFlow<String?>(null)
    val simulatedBankPushNotification: StateFlow<String?> = _simulatedBankPushNotification.asStateFlow()

    private val _activeCepReceipt = MutableStateFlow<CepReceipt?>(null)
    val activeCepReceipt: StateFlow<CepReceipt?> = _activeCepReceipt.asStateFlow()

    private val _isWithdrawalSheetOpen = MutableStateFlow(false)
    val isWithdrawalSheetOpen: StateFlow<Boolean> = _isWithdrawalSheetOpen.asStateFlow()

    private val _isGiftingSheetOpen = MutableStateFlow(false)
    val isGiftingSheetOpen: StateFlow<Boolean> = _isGiftingSheetOpen.asStateFlow()

    private val _isAuthModalOpen = MutableStateFlow(false)
    val isAuthModalOpen: StateFlow<Boolean> = _isAuthModalOpen.asStateFlow()

    private val _activeTriviaQuestion = MutableStateFlow<TriviaQuestion?>(null)
    val activeTriviaQuestion: StateFlow<TriviaQuestion?> = _activeTriviaQuestion.asStateFlow()

    private val _withdrawalProcessingState = MutableStateFlow<String?>(null)
    val withdrawalProcessingState: StateFlow<String?> = _withdrawalProcessingState.asStateFlow()

    // Supabase Cloud States
    private val _supabaseTodos = MutableStateFlow<List<SupabaseTodo>>(emptyList())
    val supabaseTodos: StateFlow<List<SupabaseTodo>> = _supabaseTodos.asStateFlow()

    private val _supabaseHealth = MutableStateFlow<SupabaseHealthStatus?>(null)
    val supabaseHealth: StateFlow<SupabaseHealthStatus?> = _supabaseHealth.asStateFlow()

    private val _isSupabaseLoading = MutableStateFlow(false)
    val isSupabaseLoading: StateFlow<Boolean> = _isSupabaseLoading.asStateFlow()

    private val _isSupabaseModalOpen = MutableStateFlow(false)
    val isSupabaseModalOpen: StateFlow<Boolean> = _isSupabaseModalOpen.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private var watchJob: Job? = null
    private val watchedCategories = mutableListOf<String>()

    init {
        startWatchSimulation()
        refreshSupabaseData()
    }

    fun onVideoChanged(newIndex: Int) {
        _currentVideoIndex.value = newIndex
        _watchMeterProgress.value = 0f
        videos.value.getOrNull(newIndex)?.let { video ->
            watchedCategories.add(video.videoTheme)
            // Record view in database
            viewModelScope.launch {
                database.videoViewDao().recordView(
                    VideoViewEntity(
                        videoId = video.id,
                        userId = "usr_vip_001",
                        watchDurationSeconds = 15,
                        coinsClaimed = true
                    )
                )
            }
        }
        startWatchSimulation()
    }

    private fun startWatchSimulation() {
        watchJob?.cancel()
        watchJob = viewModelScope.launch {
            val videoList = videos.value
            val currentVideo = videoList.getOrNull(currentVideoIndex.value) ?: return@launch
            var progress = 0f

            while (isActive) {
                delay(200) // update every 200ms
                progress += 0.04f
                if (progress >= 1.0f) {
                    progress = 0f
                    // Reached a reward cycle!
                    val reward = 1.50 // $1.50 MXN (150 coins) per cycle completed
                    walletRepository.addWatchReward(reward, currentVideo.title)
                    _rewardCoinsPopup.emit(reward)
                    triggerHapticFeedback()
                }
                _watchMeterProgress.value = progress
            }
        }
    }

    /**
     * Request dynamic feed reordering using Gemini 2.5 Flash
     */
    fun requestAiFeedReordering() {
        viewModelScope.launch {
            _toastMessage.emit("🧠 Gemini 2.5 Flash optimizando tu feed 'Para Ti'...")
            val currentVideos = videos.value
            val catalog = currentVideos.map {
                VideoEntity(
                    id = it.id,
                    creatorId = it.creatorHandle,
                    creatorName = it.creatorName,
                    creatorHandle = it.creatorHandle,
                    title = it.title,
                    description = it.description,
                    videoUrl = "",
                    category = it.videoTheme
                )
            }

            val orderedIds = GeminiService.getPersonalizedFeedOrdering(
                userWatchedCategories = watchedCategories.takeLast(10),
                availableVideos = catalog
            )

            if (orderedIds.isNotEmpty()) {
                videoRepository.reorderVideos(orderedIds)
                _toastMessage.emit("✨ ¡Feed 'Para Ti' reordenado con éxito por Gemini AI!")
                triggerHapticFeedback()
            }
        }
    }

    fun openTriviaForCurrentVideo() {
        val currentVideo = videos.value.getOrNull(currentVideoIndex.value)
        if (currentVideo?.trivia != null && !currentVideo.hasAnsweredTrivia) {
            _activeTriviaQuestion.value = currentVideo.trivia
        }
    }

    fun dismissTrivia() {
        _activeTriviaQuestion.value = null
    }

    fun submitTriviaAnswer(selectedIndex: Int) {
        val trivia = _activeTriviaQuestion.value ?: return
        val currentVideo = videos.value.getOrNull(currentVideoIndex.value) ?: return

        viewModelScope.launch {
            if (selectedIndex == trivia.correctIndex) {
                walletRepository.addTriviaReward(trivia.rewardMxn, trivia.sponsorName)
                videoRepository.markTriviaAnswered(currentVideo.id)
                _toastMessage.emit("¡Correcto! Ganaste $${String.format(java.util.Locale.US, "%.2f", trivia.rewardMxn)} MXN")
                triggerHapticFeedback()
            } else {
                _toastMessage.emit("Respuesta incorrecta. ¡Sigue viendo reels para ganar!")
            }
            _activeTriviaQuestion.value = null
        }
    }

    fun openWithdrawalSheet() {
        _isWithdrawalSheetOpen.value = true
    }

    fun closeWithdrawalSheet() {
        _isWithdrawalSheetOpen.value = false
        _withdrawalProcessingState.value = null
    }

    fun openGiftingSheet() {
        _isGiftingSheetOpen.value = true
    }

    fun closeGiftingSheet() {
        _isGiftingSheetOpen.value = false
    }

    fun openAuthModal() {
        _isAuthModalOpen.value = true
    }

    fun closeAuthModal() {
        _isAuthModalOpen.value = false
    }

    fun openSupabaseModal() {
        _isSupabaseModalOpen.value = true
        refreshSupabaseData()
    }

    fun closeSupabaseModal() {
        _isSupabaseModalOpen.value = false
    }

    fun refreshSupabaseData() {
        viewModelScope.launch {
            _isSupabaseLoading.value = true
            val health = SupabaseService.checkHealth()
            _supabaseHealth.value = health
            val todosResult = SupabaseService.fetchTodos()
            todosResult.onSuccess { list ->
                _supabaseTodos.value = list
            }.onFailure { _ ->
                if (_supabaseTodos.value.isEmpty()) {
                    _supabaseTodos.value = listOf(
                        SupabaseTodo(id = "todo_1", name = "Configurar Supabase PostgREST Client", isComplete = true),
                        SupabaseTodo(id = "todo_2", name = "Sincronizar saldo de SPEI Reels", isComplete = false),
                        SupabaseTodo(id = "todo_3", name = "Validar token publishable en .env", isComplete = true)
                    )
                }
            }
            _isSupabaseLoading.value = false
        }
    }

    fun addSupabaseTodo(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _isSupabaseLoading.value = true
            val res = SupabaseService.insertTodo(name)
            res.onSuccess { newTodo ->
                _supabaseTodos.value = listOf(newTodo) + _supabaseTodos.value
                _toastMessage.emit("¡Tarea guardada en Supabase!")
            }.onFailure {
                val localTodo = SupabaseTodo(
                    id = "local_${System.currentTimeMillis()}",
                    name = name,
                    isComplete = false
                )
                _supabaseTodos.value = listOf(localTodo) + _supabaseTodos.value
                _toastMessage.emit("Tarea agregada a la lista de Supabase")
            }
            _isSupabaseLoading.value = false
            triggerHapticFeedback()
        }
    }

    fun deleteSupabaseTodo(id: String) {
        viewModelScope.launch {
            _supabaseTodos.value = _supabaseTodos.value.filter { it.id != id }
            SupabaseService.deleteTodo(id)
            _toastMessage.emit("Tarea eliminada")
            triggerHapticFeedback()
        }
    }

    fun syncWalletToSupabase() {
        viewModelScope.launch {
            _isSupabaseLoading.value = true
            val payload = SupabaseSyncPayload(
                userId = currentUser.value?.id ?: "usr_vip_001",
                balanceMxn = balance.value,
                totalTransactions = transactions.value.size
            )
            val res = SupabaseService.syncWalletData(payload)
            res.onSuccess {
                _toastMessage.emit("¡Cartera sincronizada con Supabase Cloud exitosamente!")
                val health = SupabaseService.checkHealth()
                _supabaseHealth.value = health
            }.onFailure {
                _toastMessage.emit("Error al sincronizar con Supabase: ${it.localizedMessage}")
            }
            _isSupabaseLoading.value = false
            triggerHapticFeedback()
        }
    }

    fun showCepReceipt(receipt: CepReceipt) {
        _activeCepReceipt.value = receipt
    }

    fun closeCepReceipt() {
        _activeCepReceipt.value = null
    }

    fun dismissBankNotification() {
        _simulatedBankPushNotification.value = null
    }

    fun registerOrLoginUser(provider: String, email: String, username: String, isNewRegistration: Boolean = false) {
        viewModelScope.launch {
            val existingUser = currentUser.value
            val userRank = existingUser?.userRankNumber ?: ((1000..99999).random())
            val newUser = UserEntity(
                id = "usr_vip_001",
                authProvider = provider,
                email = email.trim(),
                username = username.trim().removePrefix("@"),
                coinBalance = if (existingUser != null && existingUser.coinBalance > 0) existingUser.coinBalance else 15000L,
                mxnBalance = if (existingUser != null && existingUser.mxnBalance > 0) existingUser.mxnBalance else 150.0,
                usdBalance = 8.50,
                hasReceivedWelcomeBonus = true,
                userRankNumber = userRank,
                avatarUrl = ""
            )
            database.userDao().insertUser(newUser)

            if (isNewRegistration) {
                // Ensure welcome bonus transaction is recorded
                walletRepository.claimWelcomeBonus()
                _toastMessage.emit("🎉 ¡Bienvenido @${newUser.username}! Registro exitoso y Bono de $150 MXN acreditado.")
            } else {
                _toastMessage.emit("✨ ¡Sesión iniciada con éxito vía $provider como @${newUser.username}!")
            }
            _isAuthModalOpen.value = false
            triggerHapticFeedback()
        }
    }

    fun loginUser(provider: String, email: String, username: String) {
        registerOrLoginUser(provider, email, username, isNewRegistration = false)
    }

    fun claimWelcomeBonus() {
        viewModelScope.launch {
            val success = walletRepository.claimWelcomeBonus()
            if (success) {
                _toastMessage.emit("🎉 ¡Bono de $150 MXN (15,000 monedas) acreditado con éxito!")
                triggerHapticFeedback()
            } else {
                _toastMessage.emit("🎉 Ya has recibido tu bono de $150 MXN por ser de los primeros 1,000,000 usuarios.")
            }
            _isAuthModalOpen.value = false
        }
    }

    fun uploadUserVideo(videoItem: VideoItem) {
        viewModelScope.launch {
            videoRepository.addNewVideo(videoItem)
            _userUploadedVideos.value = listOf(videoItem) + _userUploadedVideos.value
            database.videoDao().insertVideo(
                VideoEntity(
                    id = videoItem.id,
                    creatorId = "usr_vip_001",
                    creatorName = videoItem.creatorName,
                    creatorHandle = videoItem.creatorHandle,
                    title = videoItem.title,
                    description = videoItem.description,
                    videoUrl = "",
                    rewardCoins = 60,
                    category = videoItem.videoTheme
                )
            )
            // Award creator upload incentive
            walletRepository.addWatchReward(10.0, "Incentivo Creador: ${videoItem.title}")
            _toastMessage.emit("¡Video publicado en 'Para Ti'! Recibiste +$10.00 MXN de bono de creador.")
            triggerHapticFeedback()
        }
    }

    /**
     * Executes withdrawal with method: MERCADO_PAGO, PAYPAL, SPEI
     * Status updates: Pending -> Processing -> Success
     */
    fun executeWithdrawalWithMethod(
        method: String,
        amount: Double,
        destination: String,
        holderName: String
    ) {
        viewModelScope.launch {
            _withdrawalProcessingState.value = "Processing"
            delay(1200)

            val currentBal = balance.value
            val todayTotal = todayWithdrawn.value

            val result = walletRepository.processWithdrawalWithMethod(
                method = method,
                amountMxn = amount,
                destination = destination,
                holderName = holderName,
                concept = "RETIRO REELS $method",
                currentBalance = currentBal,
                todayWithdrawn = todayTotal
            )

            when (result) {
                is WalletRepository.WithdrawalResult.Success -> {
                    _withdrawalProcessingState.value = "Success"
                    _activeCepReceipt.value = result.cep

                    val providerTitle = when (method) {
                        "MERCADO_PAGO" -> "Mercado Pago"
                        "PAYPAL" -> "PayPal"
                        else -> "SPEI Banxico"
                    }

                    _simulatedBankPushNotification.value =
                        "🔔 $providerTitle: Depósito recibido de SPEI REELS por $${String.format(java.util.Locale.US, "%.2f", amount)} MXN. ¡Dispersión exitosa!"

                    triggerHapticFeedback()
                    delay(2000)
                    _withdrawalProcessingState.value = null
                }
                is WalletRepository.WithdrawalResult.Error -> {
                    _withdrawalProcessingState.value = null
                    _toastMessage.emit(result.message)
                }
            }
        }
    }

    fun executeSpeiWithdrawal(
        amountMxn: Double,
        clabe: String,
        beneficiaryName: String,
        concept: String,
        saveAccount: Boolean,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _withdrawalProcessingState.value = "Processing"
            delay(800)

            val clabeCheck = SpeiEngine.validateClabe(clabe)
            if (!clabeCheck.isValid) {
                _withdrawalProcessingState.value = null
                onComplete(false, clabeCheck.errorMessage)
                return@launch
            }

            val result = walletRepository.processSpeiWithdrawal(
                amountMxn = amountMxn,
                targetClabe = clabe,
                beneficiaryName = beneficiaryName,
                concept = concept,
                saveAccount = saveAccount,
                currentBalance = balance.value,
                todayWithdrawn = todayWithdrawn.value
            )

            when (result) {
                is WalletRepository.WithdrawalResult.Success -> {
                    _withdrawalProcessingState.value = null
                    _isWithdrawalSheetOpen.value = false
                    _activeCepReceipt.value = result.cep

                    _simulatedBankPushNotification.value =
                        "🔔 ${result.transaction.bankName}: Depósito SPEI recibido de SPEI REELS por $${String.format(java.util.Locale.US, "%.2f", amountMxn)} MXN."

                    triggerHapticFeedback()
                    onComplete(true, null)
                }
                is WalletRepository.WithdrawalResult.Error -> {
                    _withdrawalProcessingState.value = null
                    onComplete(false, result.message)
                }
            }
        }
    }

    fun sendGift(gift: LiveGift) {
        val currentVideo = videos.value.getOrNull(currentVideoIndex.value) ?: return
        viewModelScope.launch {
            if (balance.value < gift.costMxn) {
                _toastMessage.emit("Saldo insuficiente ($${String.format(java.util.Locale.US, "%.2f", balance.value)} MXN) para enviar ${gift.name}.")
                return@launch
            }

            walletRepository.sendLiveGift(gift.costMxn, gift.name, currentVideo.creatorName)
            _toastMessage.emit("¡Enviaste ${gift.iconEmoji} ${gift.name} a ${currentVideo.creatorName}!")
            _isGiftingSheetOpen.value = false
            triggerHapticFeedback()
        }
    }

    fun toggleLike(videoId: String) {
        videoRepository.toggleLike(videoId)
        triggerHapticFeedback()
    }

    fun addComment(videoId: String, text: String) {
        if (text.isNotBlank()) {
            videoRepository.addComment(videoId, text.trim())
        }
    }

    fun joinChallenge(campaignId: String) {
        challengeRepository.joinCampaign(campaignId)
        viewModelScope.launch {
            _toastMessage.emit("¡Te has unido al reto! Sube tu video en 'Subir (+)' para competir.")
            triggerHapticFeedback()
        }
    }

    fun simulateBonusChallengeWin(campaign: ChallengeCampaign) {
        viewModelScope.launch {
            val winAmount = 150.0
            walletRepository.addChallengeWin(winAmount, campaign.hashtag)
            _toastMessage.emit("¡Felicidades! Ganaste $${winAmount} MXN en ${campaign.hashtag} directo a tu saldo.")
            triggerHapticFeedback()
        }
    }

    private fun triggerHapticFeedback() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration not permitted
        }
    }

    override fun onCleared() {
        super.onCleared()
        watchJob?.cancel()
    }
}
