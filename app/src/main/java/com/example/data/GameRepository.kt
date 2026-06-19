package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val walletDao: WalletDao) {
    val wallet: Flow<UserWallet?> = walletDao.getWallet()
    val transactions: Flow<List<Transaction>> = walletDao.getAllTransactions()
    val redeemRequests: Flow<List<RedeemRequest>> = walletDao.getAllRedeemRequests()

    suspend fun getWalletDirect(): UserWallet {
        ensureWalletExists()
        return walletDao.getWalletDirect() ?: UserWallet()
    }

    suspend fun ensureWalletExists() {
        if (walletDao.getWalletDirect() == null) {
            walletDao.insertWallet(UserWallet(coins = 200, lifetimeCoins = 200)) // give a welcome bonus of 200 Coins!
            walletDao.insertTransaction(
                Transaction(
                    title = "Joining Welcome Reward! 🪙",
                    coinAmount = 200,
                    cashEquivalent = 2.00,
                    type = "EARNED"
                )
            )
        }
    }

    suspend fun earnCoins(amount: Int, reason: String) {
        ensureWalletExists()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        val newCoins = currentWallet.coins + amount
        val newLifetime = currentWallet.lifetimeCoins + amount
        walletDao.insertWallet(
            currentWallet.copy(
                coins = newCoins,
                lifetimeCoins = newLifetime
            )
        )
        walletDao.insertTransaction(
            Transaction(
                title = reason,
                coinAmount = amount,
                cashEquivalent = amount / 100.0,
                type = "EARNED"
            )
        )
    }

    suspend fun spendCoins(amount: Int, rewardName: String, details: String): Boolean {
        ensureWalletExists()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        if (currentWallet.coins < amount) return false

        val newCoins = currentWallet.coins - amount
        walletDao.insertWallet(currentWallet.copy(coins = newCoins))

        walletDao.insertTransaction(
            Transaction(
                title = "Redeemed $rewardName 🎁",
                coinAmount = -amount,
                cashEquivalent = -amount / 100.0,
                type = "REDEEMED"
            )
        )

        walletDao.insertRedeemRequest(
            RedeemRequest(
                rewardName = rewardName,
                coinCost = amount,
                paymentDetails = details,
                status = "PENDING"
            )
        )
        return true
    }

    suspend fun updateLevelAndHighScore(level: Int, score: Int) {
        ensureWalletExists()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        val newLevel = if (level > currentWallet.currentLevel) level else currentWallet.currentLevel
        val newHighScore = if (score > currentWallet.highScore) score else currentWallet.highScore
        walletDao.insertWallet(
            currentWallet.copy(
                currentLevel = newLevel,
                highScore = newHighScore
            )
        )
    }

    fun getRewardForDay(day: Int): Int {
        return when (day) {
            1 -> 50
            2 -> 75
            3 -> 100
            4 -> 125
            5 -> 150
            6 -> 220
            7 -> 400
            else -> 50
        }
    }

    suspend fun claimDailyBonus(forceClaim: Boolean = false): Pair<Boolean, Int> {
        ensureWalletExists()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val twoDaysMillis = 48 * 60 * 60 * 1000L

        val canClaim = forceClaim || (currentWallet.lastCheckInDate == 0L) || (now - currentWallet.lastCheckInDate >= oneDayMillis)
        
        if (!canClaim) {
            return Pair(false, 0)
        }

        // Determine next streak day
        val elapsed = now - currentWallet.lastCheckInDate
        val nextStreak = when {
            currentWallet.lastCheckInDate == 0L -> 1
            forceClaim -> {
                val s = currentWallet.checkInStreak + 1
                if (s > 7) 1 else s
            }
            elapsed in oneDayMillis..twoDaysMillis -> {
                val s = currentWallet.checkInStreak + 1
                if (s > 7) 1 else s
            }
            else -> {
                // If elapsed > 48h (missed) or other
                1
            }
        }

        val rewardAmount = getRewardForDay(nextStreak)
        val newCoins = currentWallet.coins + rewardAmount
        val newLifetime = currentWallet.lifetimeCoins + rewardAmount

        walletDao.insertWallet(
            currentWallet.copy(
                coins = newCoins,
                lifetimeCoins = newLifetime,
                lastCheckInDate = now,
                checkInStreak = nextStreak
            )
        )

        walletDao.insertTransaction(
            Transaction(
                title = "Day $nextStreak Check-In Bonus! 🗓️🎁",
                coinAmount = rewardAmount,
                cashEquivalent = rewardAmount / 100.0,
                type = "EARNED"
            )
        )

        return Pair(true, rewardAmount)
    }

    suspend fun resetCheckInStreak() {
        ensureWalletExists()
        val currentWallet = walletDao.getWalletDirect() ?: UserWallet()
        walletDao.insertWallet(
            currentWallet.copy(
                checkInStreak = 0,
                lastCheckInDate = 0L
            )
        )
    }
}
