package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.RedeemRequest
import com.example.data.Transaction
import com.example.data.UserWallet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Candy(
    val id: Int, // Unique ID for stability
    val type: Int, // 0 to 5 for different candies
    val isMatched: Boolean = false,
    val isSelected: Boolean = false
)

enum class GameStatus {
    PLAYING,
    WON,
    LOST
}

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // User Wallet & History flows
    val wallet: StateFlow<UserWallet?> = repository.wallet.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val transactions: StateFlow<List<Transaction>> = repository.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val redeemRequests: StateFlow<List<RedeemRequest>> = repository.redeemRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Candy Match Game States
    private val _boardSize = 6
    private val _board = MutableStateFlow<List<Candy>>(emptyList())
    val board: StateFlow<List<Candy>> = _board.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _targetScore = MutableStateFlow(600)
    val targetScore: StateFlow<Int> = _targetScore.asStateFlow()

    private val _movesLeft = MutableStateFlow(20)
    val movesLeft: StateFlow<Int> = _movesLeft.asStateFlow()

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _gameStatus = MutableStateFlow(GameStatus.PLAYING)
    val gameStatus: StateFlow<GameStatus> = _gameStatus.asStateFlow()

    private val _selectedIdx = MutableStateFlow<Int?>(null)
    val selectedIdx: StateFlow<Int?> = _selectedIdx.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _gameFeedback = MutableStateFlow("")
    val gameFeedback: StateFlow<String> = _gameFeedback.asStateFlow()

    // Scratch Card States
    private val _scratchCardReward = MutableStateFlow(0)
    val scratchCardReward: StateFlow<Int> = _scratchCardReward.asStateFlow()

    private val _isScratchCardAvailable = MutableStateFlow(true)
    val isScratchCardAvailable: StateFlow<Boolean> = _isScratchCardAvailable.asStateFlow()

    private val _scratchProgress = MutableStateFlow(0f)
    val scratchProgress: StateFlow<Float> = _scratchProgress.asStateFlow()

    // -----------------------------------------------------
    // ADMIN MONETIZATION & REAL-TIME AD METRICS (ADS REVENUE)
    // -----------------------------------------------------
    private val _adImpressions = MutableStateFlow(48) // simulated base impressions
    val adImpressions: StateFlow<Int> = _adImpressions.asStateFlow()

    private val _adClicks = MutableStateFlow(4)
    val adClicks: StateFlow<Int> = _adClicks.asStateFlow()

    private val _adRevenue = MutableStateFlow(0.72) // simulated accrued USD for admin
    val adRevenue: StateFlow<Double> = _adRevenue.asStateFlow()

    // Interstitial ad trigger states
    private val _isShowingInterstitial = MutableStateFlow(false)
    val isShowingInterstitial: StateFlow<Boolean> = _isShowingInterstitial.asStateFlow()

    private val _interstitialRewardAmount = MutableStateFlow(0)
    val interstitialRewardAmount: StateFlow<Int> = _interstitialRewardAmount.asStateFlow()

    private val _isRewardDoubleClaim = MutableStateFlow(false)
    val isRewardDoubleClaim: StateFlow<Boolean> = _isRewardDoubleClaim.asStateFlow()

    // Log Ad Impression to update Admin Earnings
    fun recordAdImpression(adType: String) {
        val multiplier = if (adType == "INTERSTITIAL") 0.11 else 0.006
        _adImpressions.value += 1
        _adRevenue.value += multiplier
    }

    // Log Ad Click
    fun recordAdClick() {
        _adClicks.value += 1
        _adRevenue.value += 0.38 // CPC bonus
    }

    // Open Interstitial Video Ad
    fun triggerInterstitialAd(rewardAmount: Int, isDoubleLevel: Boolean = false) {
        _interstitialRewardAmount.value = rewardAmount
        _isRewardDoubleClaim.value = isDoubleLevel
        _isShowingInterstitial.value = true
        recordAdImpression("INTERSTITIAL")
    }

    // Close Interstitial Video Ad & Claim Reward
    fun closeAndClaimAdReward() {
        _isShowingInterstitial.value = false
        val rewardVal = _interstitialRewardAmount.value
        if (rewardVal > 0) {
            SoundManager.playDailyBonusSuccess()
            viewModelScope.launch {
                if (_isRewardDoubleClaim.value) {
                    repository.earnCoins(rewardVal, "Simulated Double Level Ad Bonus! 📺✨")
                } else {
                    repository.earnCoins(rewardVal, "Sponsored Ad Video Reward! 🪙📺")
                }
                _interstitialRewardAmount.value = 0
                _isRewardDoubleClaim.value = false
            }
        }
    }

    fun dismissInterstitialWithoutClaim() {
        _isShowingInterstitial.value = false
        _interstitialRewardAmount.value = 0
        _isRewardDoubleClaim.value = false
    }

    init {
        viewModelScope.launch {
            repository.ensureWalletExists()
            // Sync level
            wallet.collect { w ->
                if (w != null) {
                    _level.value = w.currentLevel
                }
            }
        }
        setupNewGame()
    }

    // -----------------------------------------------------
    // MATCH-3 GAME ACTIONS
    // -----------------------------------------------------
    fun setupNewGame() {
        val currentLvl = level.value
        _targetScore.value = currentLvl * 500 // Level 1 is 500, Level 2 is 1000, etc.
        _movesLeft.value = 20
        _score.value = 0
        _selectedIdx.value = null
        _gameStatus.value = GameStatus.PLAYING
        _isBusy.value = false
        _gameFeedback.value = "Let's Crush! 🍬"

        // Generate stable board without pre-existing matches
        var initialList: List<Candy>
        var attempts = 0
        do {
            initialList = List(_boardSize * _boardSize) { i ->
                Candy(id = i, type = Random.nextInt(6))
            }
            attempts++
        } while (hasMatches(initialList) && attempts < 50)

        // If still has matches, resolve them forcefully
        if (hasMatches(initialList)) {
            val mutableList = initialList.toMutableList()
            resolveBoardOfPreMatches(mutableList)
            _board.value = mutableList
        } else {
            _board.value = initialList
        }
    }

    private fun resolveBoardOfPreMatches(list: MutableList<Candy>) {
        for (row in 0 until _boardSize) {
            for (col in 0 until _boardSize) {
                val idx = row * _boardSize + col
                while (checkMatchAt(list, row, col)) {
                    list[idx] = list[idx].copy(type = (list[idx].type + 1) % 6)
                }
            }
        }
    }

    private fun checkMatchAt(list: List<Candy>, r: Int, c: Int): Boolean {
        val idx = r * _boardSize + c
        val type = list[idx].type
        // Horizontal check
        if (c >= 2) {
            if (list[r * _boardSize + c - 1].type == type && list[r * _boardSize + c - 2].type == type) return true
        }
        // Vertical check
        if (r >= 2) {
            if (list[(r - 1) * _boardSize + c].type == type && list[(r - 2) * _boardSize + c].type == type) return true
        }
        return false
    }

    fun selectCandy(index: Int) {
        if (_isBusy.value || _gameStatus.value != GameStatus.PLAYING) return

        val prevSelected = _selectedIdx.value
        val list = _board.value.toMutableList()

        if (prevSelected == null) {
            _selectedIdx.value = index
            list[index] = list[index].copy(isSelected = true)
            _board.value = list
        } else {
            if (prevSelected == index) {
                // Deselect
                _selectedIdx.value = null
                list[index] = list[index].copy(isSelected = false)
                _board.value = list
                return
            }

            // Check adjacency
            val r1 = prevSelected / _boardSize
            val c1 = prevSelected % _boardSize
            val r2 = index / _boardSize
            val c2 = index % _boardSize

            val isAdjacent = (r1 == r2 && kotlin.math.abs(c1 - c2) == 1) ||
                    (c1 == c2 && kotlin.math.abs(r1 - r2) == 1)

            if (isAdjacent) {
                // Deselect previous
                list[prevSelected] = list[prevSelected].copy(isSelected = false)
                _board.value = list
                _selectedIdx.value = null
                // Attempt swap
                executeSwap(prevSelected, index)
            } else {
                // Switch selection
                list[prevSelected] = list[prevSelected].copy(isSelected = false)
                list[index] = list[index].copy(isSelected = true)
                _board.value = list
                _selectedIdx.value = index
            }
        }
    }

    fun swipeCandy(index: Int, direction: String) {
        if (_isBusy.value || _gameStatus.value != GameStatus.PLAYING) return
        val row = index / _boardSize
        val col = index % _boardSize
        val targetIdx = when (direction) {
            "UP" -> if (row > 0) index - _boardSize else null
            "DOWN" -> if (row < _boardSize - 1) index + _boardSize else null
            "LEFT" -> if (col > 0) index - 1 else null
            "RIGHT" -> if (col < _boardSize - 1) index + 1 else null
            else -> null
        }
        if (targetIdx != null) {
            val list = _board.value.toMutableList()
            val prevSelected = _selectedIdx.value
            if (prevSelected != null) {
                list[prevSelected] = list[prevSelected].copy(isSelected = false)
            }
            // Ensure swiped element is also deselected
            list[index] = list[index].copy(isSelected = false)
            _board.value = list
            _selectedIdx.value = null
            executeSwap(index, targetIdx)
        }
    }

    private fun executeSwap(idx1: Int, idx2: Int) {
        viewModelScope.launch {
            _isBusy.value = true
            _gameFeedback.value = "Swapping..."

            // 1. Swap physically
            val currentList = _board.value.toMutableList()
            val temp = currentList[idx1]
            currentList[idx1] = currentList[idx2].copy(id = currentList[idx1].id)
            currentList[idx2] = temp.copy(id = currentList[idx2].id)
            _board.value = currentList
            delay(250)

            // 2. See if there are matches
            val matchedIndices = findMatchIndices(currentList)
            if (matchedIndices.isNotEmpty()) {
                // Success! Deduct move & process matches
                _movesLeft.value -= 1
                var combo = 1
                processMatchesCycle(currentList, matchedIndices, combo)
            } else {
                // Swap Back
                _gameFeedback.value = "No Match! Reverting..."
                val listRevert = _board.value.toMutableList()
                val temp2 = listRevert[idx1]
                listRevert[idx1] = listRevert[idx2].copy(id = listRevert[idx1].id)
                listRevert[idx2] = temp2.copy(id = listRevert[idx2].id)
                _board.value = listRevert
                delay(250)
                _gameFeedback.value = "Try another swap! 🍬"
                _isBusy.value = false
            }
        }
    }

    private suspend fun processMatchesCycle(boardList: MutableList<Candy>, initialMatches: Set<Int>, initialCombo: Int) {
        var combo = initialCombo
        var matches = initialMatches
        var activeBoard = boardList

        while (matches.isNotEmpty() && _gameStatus.value == GameStatus.PLAYING) {
            _gameFeedback.value = if (combo > 1) "Cascade Combo x$combo! 🔥" else "Delicious match! ✨"
            SoundManager.playCandyMatch(combo)

            // 1. Mark as matched in UI
            for (idx in matches) {
                activeBoard[idx] = activeBoard[idx].copy(isMatched = true)
            }
            _board.value = activeBoard.toList()
            delay(350)

            // 2. Score calculation
            val matchPoints = matches.size * 10 * combo
            _score.value += matchPoints
            SoundManager.playPointAccumulation()

            // 3. Reset matched places to empty (type = -1 representing empty)
            for (idx in matches) {
                activeBoard[idx] = activeBoard[idx].copy(type = -1, isMatched = false)
            }

            // 4. Compact downwards (Candies fall down)
            applyGravityPhysics(activeBoard)
            _board.value = activeBoard.toList()
            delay(250)

            // 5. Refill the empty boxes at top
            refillTopCandies(activeBoard)
            _board.value = activeBoard.toList()
            delay(250)

            // 6. See if new matches cascade
            val nextMatches = findMatchIndices(activeBoard)
            if (nextMatches.isNotEmpty()) {
                combo++
                matches = nextMatches
            } else {
                matches = emptySet()
            }
        }

        checkEndGameConditions()
        _isBusy.value = false
    }

    private fun applyGravityPhysics(list: MutableList<Candy>) {
        for (col in 0 until _boardSize) {
            // Read column from bottom (row 5) to top (row 0)
            val columnCandies = mutableListOf<Candy>()
            for (row in (_boardSize - 1) downTo 0) {
                val idx = row * _boardSize + col
                if (list[idx].type != -1) {
                    columnCandies.add(list[idx])
                }
            }

            // Fill empty boxes at top of buffer
            while (columnCandies.size < _boardSize) {
                // placeholder candy
                columnCandies.add(Candy(id = Random.nextInt(100000) + 2000, type = -1))
            }

            // Re-apply to board column
            for (row in 0 until _boardSize) {
                val boardIdx = row * _boardSize + col
                // map candy back (remember bottom list item goes to bottom board row)
                val candy = columnCandies[_boardSize - 1 - row]
                list[boardIdx] = candy
            }
        }
    }

    private fun refillTopCandies(list: MutableList<Candy>) {
        for (i in list.indices) {
            if (list[i].type == -1) {
                list[i] = Candy(id = Random.nextInt(100000) + 10000, type = Random.nextInt(6))
            }
        }
    }

    private fun findMatchIndices(list: List<Candy>): Set<Int> {
        val matches = mutableSetOf<Int>()

        // 1. Horizontal Matches (row checker)
        for (r in 0 until _boardSize) {
            var matchCount = 1
            var matchType = -1
            var startCol = -1

            for (c in 0 until _boardSize) {
                val idx = r * _boardSize + c
                val type = list[idx].type

                if (type == -1) {
                    // broken
                    if (matchCount >= 3) {
                        for (mc in 0 until matchCount) {
                            matches.add(r * _boardSize + (startCol + mc))
                        }
                    }
                    matchCount = 1
                    matchType = -1
                    continue
                }

                if (type == matchType) {
                    matchCount++
                } else {
                    if (matchCount >= 3) {
                        for (mc in 0 until matchCount) {
                            matches.add(r * _boardSize + (startCol + mc))
                        }
                    }
                    matchCount = 1
                    matchType = type
                    startCol = c
                }
            }
            if (matchCount >= 3) {
                for (mc in 0 until matchCount) {
                    matches.add(r * _boardSize + (startCol + mc))
                }
            }
        }

        // 2. Vertical Matches (column checker)
        for (c in 0 until _boardSize) {
            var matchCount = 1
            var matchType = -1
            var startRow = -1

            for (r in 0 until _boardSize) {
                val idx = r * _boardSize + c
                val type = list[idx].type

                if (type == -1) {
                    if (matchCount >= 3) {
                        for (mr in 0 until matchCount) {
                            matches.add((startRow + mr) * _boardSize + c)
                        }
                    }
                    matchCount = 1
                    matchType = -1
                    continue
                }

                if (type == matchType) {
                    matchCount++
                } else {
                    if (matchCount >= 3) {
                        for (mr in 0 until matchCount) {
                            matches.add((startRow + mr) * _boardSize + c)
                        }
                    }
                    matchCount = 1
                    matchType = type
                    startRow = r
                }
            }
            if (matchCount >= 3) {
                for (mr in 0 until matchCount) {
                    matches.add((startRow + mr) * _boardSize + c)
                }
            }
        }

        return matches
    }

    private fun hasMatches(list: List<Candy>): Boolean {
        return findMatchIndices(list).isNotEmpty()
    }

    private fun checkEndGameConditions() {
        val scoreVal = _score.value
        val targetVal = _targetScore.value
        val movesVal = _movesLeft.value

        if (scoreVal >= targetVal) {
            _gameStatus.value = GameStatus.WON
            _gameFeedback.value = "Awesome! Target Achieved! 🎉"
            creditLevelWinCoins()
        } else if (movesVal <= 0) {
            _gameStatus.value = GameStatus.LOST
            _gameFeedback.value = "Moves finished! Better luck next time. 💔"
        }
    }

    private fun creditLevelWinCoins() {
        val coinsEarned = 100 + (score.value - targetScore.value) / 10 // base 100 coins + speed bonus
        val currentLvl = level.value
        SoundManager.playLevelWin()
        viewModelScope.launch {
            repository.updateLevelAndHighScore(currentLvl + 1, score.value)
            repository.earnCoins(coinsEarned, "Candy Crush Level $currentLvl Cleared! 🍬")
        }
    }

    // -----------------------------------------------------
    // EARNING SYSTEM ACTIONS
    // -----------------------------------------------------
    fun claimDailyBonus(forceClaim: Boolean = false, onSuccess: (Int) -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            val result = repository.claimDailyBonus(forceClaim)
            val success = result.first
            val rewardAmount = result.second
            if (success) {
                SoundManager.playDailyBonusSuccess()
                onSuccess(rewardAmount)
            } else {
                SoundManager.playRedemptionFailure()
                onFailure()
            }
        }
    }

    fun resetDailyCheckInStreak() {
        viewModelScope.launch {
            repository.resetCheckInStreak()
        }
    }

    fun getRewardForDay(day: Int): Int {
        return repository.getRewardForDay(day)
    }

    // Scratch mini-game actions
    fun prepareScratchCard() {
        _scratchCardReward.value = Random.nextInt(20, 80) // 20-80 random coins!
        _scratchProgress.value = 0f
        _isScratchCardAvailable.value = true
    }

    fun updateScratchProgress(touchCountFactor: Float) {
        // Simple progress simulation mapping coordinates Touched to a percentage
        val nextProgress = _scratchProgress.value + touchCountFactor
        _scratchProgress.value = nextProgress.coerceIn(0f, 100f)

        if (_scratchProgress.value >= 60f && _isScratchCardAvailable.value) {
            // Successfully scratched 60% of card, award coins!
            _isScratchCardAvailable.value = false
            val rewardAmount = _scratchCardReward.value
            SoundManager.playDailyBonusSuccess()
            viewModelScope.launch {
                repository.earnCoins(rewardAmount, "Lucky Scratch Card Bonus! 🃏")
            }
        }
    }

    // Redeem system actions
    fun attemptRedemption(
        rewardName: String,
        coinsValue: Int,
        paymentDetails: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val walletDirect = repository.getWalletDirect()
            if (walletDirect.coins < coinsValue) {
                SoundManager.playRedemptionFailure()
                onFailure("Sufficient coins balance missing! Earn more coins by crushing candies.")
                return@launch
            }
            if (paymentDetails.isBlank()) {
                SoundManager.playRedemptionFailure()
                onFailure("Please enter valid transfer account or UPI details!")
                return@launch
            }

            val success = repository.spendCoins(coinsValue, rewardName, paymentDetails)
            if (success) {
                SoundManager.playRedemptionSuccess()
                onSuccess()
            } else {
                SoundManager.playRedemptionFailure()
                onFailure("Verification failed! Try again.")
            }
        }
    }
}
