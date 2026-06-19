package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM user_wallet WHERE id = 1 LIMIT 1")
    fun getWallet(): Flow<UserWallet?>

    @Query("SELECT * FROM user_wallet WHERE id = 1 LIMIT 1")
    suspend fun getWalletDirect(): UserWallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: UserWallet)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM redeem_requests ORDER BY timestamp DESC")
    fun getAllRedeemRequests(): Flow<List<RedeemRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedeemRequest(request: RedeemRequest)
}
