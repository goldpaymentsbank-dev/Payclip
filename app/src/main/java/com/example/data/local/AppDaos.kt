package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PayoutRequestEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserPaymentMethodEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoViewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET coinBalance = coinBalance + :coins, mxnBalance = mxnBalance + :mxnAmount, usdBalance = usdBalance + (:mxnAmount / 18.0) WHERE id = :userId")
    suspend fun creditRewards(userId: String, coins: Long, mxnAmount: Double)

    @Query("UPDATE users SET coinBalance = CASE WHEN coinBalance >= :coins THEN coinBalance - :coins ELSE 0 END, mxnBalance = CASE WHEN mxnBalance >= :mxnAmount THEN mxnBalance - :mxnAmount ELSE 0.0 END, usdBalance = CASE WHEN usdBalance >= (:mxnAmount / 18.0) THEN usdBalance - (:mxnAmount / 18.0) ELSE 0.0 END WHERE id = :userId")
    suspend fun debitBalance(userId: String, coins: Long, mxnAmount: Double)
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos")
    fun getAllVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos")
    suspend fun getAllVideos(): List<VideoEntity>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: String): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("UPDATE videos SET isLiked = :isLiked, likesCount = likesCount + :delta WHERE id = :id")
    suspend fun updateLike(id: String, isLiked: Boolean, delta: Int)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: String)
}

@Dao
interface VideoViewDao {
    @Query("SELECT * FROM video_views WHERE userId = :userId ORDER BY timestamp DESC")
    fun getViewsForUser(userId: String): Flow<List<VideoViewEntity>>

    @Query("SELECT * FROM video_views WHERE userId = :userId ORDER BY timestamp DESC LIMIT 20")
    suspend fun getRecentViews(userId: String): List<VideoViewEntity>

    @Insert
    suspend fun recordView(view: VideoViewEntity)
}

@Dao
interface UserPaymentMethodDao {
    @Query("SELECT * FROM user_payment_methods WHERE userId = :userId")
    fun getPaymentMethodsFlow(userId: String): Flow<List<UserPaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethod(method: UserPaymentMethodEntity): Long

    @Query("DELETE FROM user_payment_methods WHERE id = :id")
    suspend fun deleteMethod(id: Long)
}

@Dao
interface PayoutRequestDao {
    @Query("SELECT * FROM payout_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPayoutRequestsFlow(userId: String): Flow<List<PayoutRequestEntity>>

    @Query("SELECT * FROM payout_requests ORDER BY createdAt DESC")
    fun getAllPayoutRequestsFlow(): Flow<List<PayoutRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayoutRequest(request: PayoutRequestEntity)

    @Query("UPDATE payout_requests SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: String, newStatus: String)
}
