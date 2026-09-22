package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.PayoutRequestEntity
import com.example.data.model.SpeiAccountEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.model.UserPaymentMethodEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoViewEntity
import com.example.data.model.WalletTransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WalletTransactionEntity::class,
        SpeiAccountEntity::class,
        UserEntity::class,
        VideoEntity::class,
        VideoViewEntity::class,
        UserPaymentMethodEntity::class,
        PayoutRequestEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun speiAccountDao(): SpeiAccountDao
    abstract fun userDao(): UserDao
    abstract fun videoDao(): VideoDao
    abstract fun videoViewDao(): VideoViewDao
    abstract fun userPaymentMethodDao(): UserPaymentMethodDao
    abstract fun payoutRequestDao(): PayoutRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spei_reels_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val walletDao = database.walletDao()
                val speiDao = database.speiAccountDao()
                val userDao = database.userDao()
                val videoDao = database.videoDao()
                val paymentMethodDao = database.userPaymentMethodDao()

                val now = System.currentTimeMillis()

                // Initial User with First 1 Million Bonus ($150 MXN = 15,000 Coins)
                val initialUser = UserEntity(
                    id = "usr_vip_001",
                    authProvider = "email",
                    email = "creator@speireels.mx",
                    username = "Alex_ReelsMX",
                    coinBalance = 15000L,
                    usdBalance = 8.50,
                    mxnBalance = 150.0,
                    hasReceivedWelcomeBonus = true,
                    userRankNumber = 48291,
                    avatarUrl = ""
                )
                userDao.insertUser(initialUser)

                // Initial Mercado Pago & PayPal payment methods
                paymentMethodDao.insertMethod(
                    UserPaymentMethodEntity(
                        userId = "usr_vip_001",
                        type = "MERCADO_PAGO",
                        fullName = "Carlos Mendoza Rivera",
                        accountIdentifier = "012180015498263828",
                        isDefault = true,
                        bankOrProvider = "Mercado Pago / BBVA"
                    )
                )

                paymentMethodDao.insertMethod(
                    UserPaymentMethodEntity(
                        userId = "usr_vip_001",
                        type = "PAYPAL",
                        fullName = "Carlos Mendoza",
                        accountIdentifier = "carlos.mendoza@payouts.net",
                        isDefault = false,
                        bankOrProvider = "PayPal Inc."
                    )
                )

                // Initial Videos
                videoDao.insertVideos(
                    listOf(
                        VideoEntity(
                            id = "vid_001",
                            creatorId = "creator_sofia",
                            creatorName = "Sofía Finanzas",
                            creatorHandle = "@sofiafintech",
                            title = "¿Cómo retirar tus ganancias de TikTok al instante con SPEI?",
                            description = "No esperes 30 días para cobrar tu dinero. Aprende a conectar tu CLABE interbancaria y recibe depósitos 24/7 en segundos con #WatchToEarn.",
                            videoUrl = "sample_video_fintech",
                            rewardCoins = 50,
                            category = "Fintech",
                            likesCount = 4820,
                            commentsCount = 210,
                            sharesCount = 530,
                            sponsorBrand = "SPEI / Banxico",
                            soundTitle = "Fintech Vibes - SPEI Original"
                        ),
                        VideoEntity(
                            id = "vid_002",
                            creatorId = "creator_pedro",
                            creatorName = "Pedro Gamer Pro",
                            creatorHandle = "@pedrotech",
                            title = "Probando el setup cyberpunk más rápido de México 🚀🎮",
                            description = "Configuré una estación de batalla con refrigeración líquida y pantalla OLED vertical. ¿Valió la pena la inversión? Cuéntame en los comentarios.",
                            videoUrl = "sample_video_gaming",
                            rewardCoins = 60,
                            category = "Gaming",
                            likesCount = 9430,
                            commentsCount = 480,
                            sharesCount = 1120,
                            soundTitle = "Cyber City 2077 - Beat Synth"
                        ),
                        VideoEntity(
                            id = "vid_003",
                            creatorId = "creator_elena",
                            creatorName = "Elena Lifestyle",
                            creatorHandle = "@elenalife",
                            title = "Mi rutina matutina para monetizar creando contenido diario ☕✨",
                            description = "De espectadora a creadora verificada. 3 tips clave para subir videos virales y ganar monedas con cada reproducción activa.",
                            videoUrl = "sample_video_lifestyle",
                            rewardCoins = 45,
                            category = "Lifestyle",
                            likesCount = 3120,
                            commentsCount = 135,
                            sharesCount = 310,
                            sponsorBrand = "Mercado Pago",
                            soundTitle = "Morning Chillhop - Acoustic Guitar"
                        ),
                        VideoEntity(
                            id = "vid_004",
                            creatorId = "creator_carlos",
                            creatorName = "Carlos Tech Review",
                            creatorHandle = "@carlostech",
                            title = "Gemini 2.5 Flash: ¿La IA más rápida para crear videos cortos?",
                            description = "Probamos la moderación de contenido y recomendación inteligente de 'Para Ti' generada en tiempo real con Google AI Studio.",
                            videoUrl = "sample_video_ai",
                            rewardCoins = 75,
                            category = "Tech",
                            likesCount = 6720,
                            commentsCount = 340,
                            sharesCount = 890,
                            sponsorBrand = "Google AI Studio",
                            soundTitle = "Neural Network - Synthwave"
                        )
                    )
                )

                // Seed pre-verified default CLABE
                speiDao.insertAccount(
                    SpeiAccountEntity(
                        clabe = "012180015498263828",
                        bankCode = "012",
                        bankName = "BBVA México",
                        beneficiaryName = "Carlos Mendoza Rivera",
                        isDefault = true,
                        createdAt = now - 86400000L * 2
                    )
                )

                // Seed initial transactions
                walletDao.insertTransaction(
                    WalletTransactionEntity(
                        amountMxn = 150.0,
                        type = TransactionType.WATCH_REWARD,
                        description = "🎉 ¡Bono de $150 MXN asignado por ser de los primeros 1,000,000 de usuarios!",
                        timestamp = now - 3600000L * 2,
                        status = TransactionStatus.COMPLETED,
                        sanitizedConcept = "BONO PRIMEROS 1M USUARIOS"
                    )
                )
            }
        }
    }
}
