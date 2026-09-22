package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AuthWelcomeBonusModal
import com.example.ui.components.CepDialog
import com.example.ui.components.InteractiveTriviaDialog
import com.example.ui.components.LiveGiftingSheet
import com.example.ui.components.SimulatedPushNotification
import com.example.ui.components.SupabaseCloudModal
import com.example.ui.screens.CarteraPayoutScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UploadStudioScreen
import com.example.ui.screens.VideoFeedScreen
import com.example.ui.theme.FuturisticBlack
import com.example.ui.theme.FuturisticDarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.WarmGold
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current

    // State collections
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val todayWithdrawn by viewModel.todayWithdrawn.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val userVideos by viewModel.userUploadedVideos.collectAsStateWithLifecycle()
    val campaigns by viewModel.campaigns.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val payoutRequests by viewModel.payoutRequests.collectAsStateWithLifecycle()
    val currentVideoIndex by viewModel.currentVideoIndex.collectAsStateWithLifecycle()
    val watchMeterProgress by viewModel.watchMeterProgress.collectAsStateWithLifecycle()

    // Modals and notifications
    val simulatedNotification by viewModel.simulatedBankPushNotification.collectAsStateWithLifecycle()
    val activeCepReceipt by viewModel.activeCepReceipt.collectAsStateWithLifecycle()
    val isGiftingOpen by viewModel.isGiftingSheetOpen.collectAsStateWithLifecycle()
    val isAuthModalOpen by viewModel.isAuthModalOpen.collectAsStateWithLifecycle()
    val activeTrivia by viewModel.activeTriviaQuestion.collectAsStateWithLifecycle()
    val withdrawalState by viewModel.withdrawalProcessingState.collectAsStateWithLifecycle()
    val isSupabaseModalOpen by viewModel.isSupabaseModalOpen.collectAsStateWithLifecycle()
    val supabaseTodos by viewModel.supabaseTodos.collectAsStateWithLifecycle()
    val supabaseHealth by viewModel.supabaseHealth.collectAsStateWithLifecycle()
    val isSupabaseLoading by viewModel.isSupabaseLoading.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) }
    var coinRewardPopup by remember { mutableStateOf<Double?>(null) }

    // Listen to coin rewards
    LaunchedEffect(Unit) {
        viewModel.rewardCoinsPopup.collect { reward ->
            coinRewardPopup = reward
            kotlinx.coroutines.delay(2200)
            coinRewardPopup = null
        }
    }

    // Listen to toasts
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val currentVideo = videos.getOrNull(currentVideoIndex)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FuturisticBlack,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xF00A0A10),
                contentColor = NeonCyan,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0x3300F2FE), Color(0x339D4EDD))),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                // Tab 0: Para Ti
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Para Ti",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Para Ti", fontSize = 10.sp, fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = NeonCyan.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_para_ti")
                )

                // Tab 1: Descubrir
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Descubrir",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Descubrir", fontSize = 10.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = NeonCyan.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_discover")
                )

                // Tab 2: Subir (+) (Glowing Neon Pill)
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(NeonCyan, NeonPurple, NeonPink))
                                )
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (activeTab == 2) Color.Transparent else FuturisticBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Subir Video",
                                    tint = if (activeTab == 2) Color.Black else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    },
                    label = { Text("Subir (+)", fontSize = 10.sp, fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_upload")
                )

                // Tab 3: Cartera
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Cartera",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Cartera", fontSize = 10.sp, fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarmGold,
                        selectedTextColor = WarmGold,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = WarmGold.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_cartera")
                )

                // Tab 4: Perfil / Mis Videos
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Perfil", fontSize = 10.sp, fontWeight = if (activeTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = NeonPurple.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> VideoFeedScreen(
                    videos = videos,
                    currentIndex = currentVideoIndex,
                    watchProgress = watchMeterProgress,
                    balance = balance,
                    coinRewardPopup = coinRewardPopup,
                    onVideoChange = viewModel::onVideoChanged,
                    onLikeToggle = viewModel::toggleLike,
                    onCommentSubmit = viewModel::addComment,
                    onOpenTrivia = viewModel::openTriviaForCurrentVideo,
                    onOpenGifting = viewModel::openGiftingSheet,
                    onOpenWithdrawal = { activeTab = 3 },
                    onRequestAiReorder = viewModel::requestAiFeedReordering
                )
                1 -> DiscoverScreen(
                    campaigns = campaigns,
                    onJoinChallenge = {
                        viewModel.joinChallenge(it)
                        activeTab = 2 // Move to upload
                    },
                    onSimulateWin = viewModel::simulateBonusChallengeWin
                )
                2 -> UploadStudioScreen(
                    onVideoUploaded = {
                        viewModel.uploadUserVideo(it)
                        activeTab = 0 // Go to feed to watch it
                    },
                    userUploadedVideos = userVideos
                )
                3 -> CarteraPayoutScreen(
                    balanceMxn = balance,
                    currentUser = currentUser,
                    transactions = transactions,
                    payoutRequests = payoutRequests,
                    withdrawalState = withdrawalState,
                    onExecuteWithdrawal = { method, amount, dest, name ->
                        viewModel.executeWithdrawalWithMethod(method, amount, dest, name)
                    },
                    onViewCepReceipt = viewModel::showCepReceipt,
                    onOpenBonusModal = viewModel::openAuthModal,
                    onOpenSupabaseCloud = viewModel::openSupabaseModal
                )
                4 -> ProfileScreen(
                    currentUser = currentUser,
                    balanceMxn = balance,
                    userVideos = userVideos,
                    onOpenAuthModal = viewModel::openAuthModal,
                    onOpenSupabaseModal = viewModel::openSupabaseModal
                )
            }

            // Simulated Banking / Payout Notification Banner at Top
            SimulatedPushNotification(
                message = simulatedNotification,
                onDismiss = viewModel::dismissBankNotification,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Modal: Auth & First 1 Million Welcome Bonus
    if (isAuthModalOpen) {
        AuthWelcomeBonusModal(
            onDismiss = viewModel::closeAuthModal,
            onLoginSuccess = { provider, email, username, isNewRegistration ->
                viewModel.registerOrLoginUser(provider, email, username, isNewRegistration)
            },
            onClaimBonus = viewModel::claimWelcomeBonus
        )
    }

    // Modal: Interactive Video Trivia Dialog
    activeTrivia?.let { trivia ->
        InteractiveTriviaDialog(
            trivia = trivia,
            onAnswerSelected = viewModel::submitTriviaAnswer,
            onDismiss = viewModel::dismissTrivia
        )
    }

    // Modal: Live Gifting Sheet for Creator Monetization
    if (isGiftingOpen && currentVideo != null) {
        LiveGiftingSheet(
            gifts = viewModel.availableGifts,
            balance = balance,
            creatorName = currentVideo.creatorName,
            onGiftSelected = viewModel::sendGift,
            onDismiss = viewModel::closeGiftingSheet
        )
    }

    // Modal: Banxico CEP Comprobante Electrónico de Pago
    activeCepReceipt?.let { cep ->
        CepDialog(
            receipt = cep,
            onDismiss = viewModel::closeCepReceipt,
            onShare = {
                Toast.makeText(
                    context,
                    "Comprobante CEP #${cep.trackingKey} copiado al portapapeles.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Modal: Supabase Cloud Hub (PostgREST & Todos Sync)
    if (isSupabaseModalOpen) {
        SupabaseCloudModal(
            healthStatus = supabaseHealth,
            todos = supabaseTodos,
            isLoading = isSupabaseLoading,
            onDismiss = viewModel::closeSupabaseModal,
            onRefresh = viewModel::refreshSupabaseData,
            onAddTodo = viewModel::addSupabaseTodo,
            onDeleteTodo = viewModel::deleteSupabaseTodo,
            onSyncWallet = viewModel::syncWalletToSupabase
        )
    }
}
