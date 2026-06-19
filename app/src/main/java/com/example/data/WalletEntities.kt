package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_wallet")
data class UserWallet(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 0,
    val lifetimeCoins: Int = 0,
    val currentLevel: Int = 1,
    val highScore: Int = 0,
    val lastCheckInDate: Long = 0L,
    val checkInStreak: Int = 0
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val coinAmount: Int, // e.g. +50, -200
    val cashEquivalent: Double, // e.g. +0.50, -2.00
    val timestamp: Long = System.currentTimeMillis(),
    val type: String // "EARNED", "REDEEMED"
)

@Entity(tableName = "redeem_requests")
data class RedeemRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rewardName: String, // e.g. "Paytm UPI ₹5"
    val coinCost: Int, // e.g. 500
    val paymentDetails: String, // UPI ID or Phone number
    val status: String = "PENDING", // PENDING, APPROVED
    val timestamp: Long = System.currentTimeMillis()
)
