package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.*
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

enum class AppTab {
    PLAY,
    WALLET,
    REDEEM
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppScreen(viewModel: GameViewModel) {
    val walletState by viewModel.wallet.collectAsState()
    val isShowingInterstitial by viewModel.isShowingInterstitial.collectAsState()
    var currentTab by remember { mutableStateOf(AppTab.PLAY) }

    val currentCoins = walletState?.coins ?: 0
    val cashValue = currentCoins / 100.0

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.PLAY,
                    onClick = { currentTab = AppTab.PLAY },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Play Game") },
                    label = { Text("Crush Game", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_play")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.WALLET,
                    onClick = { 
                        currentTab = AppTab.WALLET 
                        viewModel.prepareScratchCard()
                    },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                    label = { Text("Earning", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_wallet")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.REDEEM,
                    onClick = { currentTab = AppTab.REDEEM },
                    icon = { Icon(Icons.Default.CardGiftcard, contentDescription = "Redeem Vouchers") },
                    label = { Text("Redeem", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_redeem")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CaramelDarkBackground,
                            Color(0xFF2C0A27)
                        )
                    )
                )
        ) {
            // Elegant Dashboard Top Header Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Game Logo Status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(SweetMagentaPrimary, Color(0xFFC2185B))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Games,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Candy Cash Match",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Level ${walletState?.currentLevel ?: 1}",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Balance display chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.secondary, SweetMagentaPrimary)
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = "Coins balance",
                        tint = GoldenCoinSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$currentCoins",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier.testTag("wallet_balance_header")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(₹${String.format("%.2f", cashValue)})",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = Color(0xFF3F193D), thickness = 1.dp)

            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width else -width } + fadeIn() with
                            slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width else width } + fadeOut()
                },
                modifier = Modifier.weight(1f)
            ) { targetTab ->
                when (targetTab) {
                    AppTab.PLAY -> PlayGameScreen(viewModel)
                    AppTab.WALLET -> WalletScreen(viewModel)
                    AppTab.REDEEM -> RedeemScreen(viewModel)
                }
            }

            // Persistent Scrolling Banner Ad (Increments Admin Revenue stats live)
            SimulatedBannerAd(viewModel = viewModel)
        }
    }

    // Interstitial Ad overlay over everything when active
    if (isShowingInterstitial) {
        AdInterstitialOverlay(viewModel = viewModel)
    }
    }
}

// ---------------------------------------------------------------------
// 1. PLAY GAME SCREEN (Candy Crush UI & Engine Interface)
// ---------------------------------------------------------------------
@Composable
fun PlayGameScreen(viewModel: GameViewModel) {
    val boardState by viewModel.board.collectAsState()
    val scoreState by viewModel.score.collectAsState()
    val targetScoreState by viewModel.targetScore.collectAsState()
    val movesLeftState by viewModel.movesLeft.collectAsState()
    val levelState by viewModel.level.collectAsState()
    val gameStatusState by viewModel.gameStatus.collectAsState()
    val feedbackState by viewModel.gameFeedback.collectAsState()
    val isBusyState by viewModel.isBusy.collectAsState()
    val selectedState by viewModel.selectedIdx.collectAsState()

    // 1. Smooth real-time score count animation
    val animatedScore by animateIntAsState(
        targetValue = scoreState,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "scoreCountAnimation"
    )

    // 2. Bubble scale pop effect when score increases
    var scoreScale by remember { mutableStateOf(1f) }
    LaunchedEffect(scoreState) {
        if (scoreState > 0) {
            scoreScale = 1.35f
            delay(120)
            scoreScale = 1f
        }
    }
    val animatedScale by animateFloatAsState(
        targetValue = scoreScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scoreScaleAnimation"
    )

    // 3. Floating points addition popup notification (+30, +60 points, etc.)
    var prevScore by remember { mutableStateOf(scoreState) }
    var scoreDiff by remember { mutableStateOf(0) }
    var showPointsAnim by remember { mutableStateOf(false) }

    LaunchedEffect(scoreState) {
        if (scoreState > prevScore) {
            scoreDiff = scoreState - prevScore
            showPointsAnim = true
            delay(1000)
            showPointsAnim = false
        }
        prevScore = scoreState
    }

    val driftAnim by animateFloatAsState(
        targetValue = if (showPointsAnim) -18f else 10f,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
        label = "driftAnimation"
    )
    val fadeAnim by animateFloatAsState(
        targetValue = if (showPointsAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "fadeAnimation"
    )

    // Progress moves as the score counts up
    val progress = (animatedScore.toFloat() / targetScoreState.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Levels and Goal Progress Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LEVEL OBJECTIVE",
                        color = DarkTextSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Score $targetScoreState Points",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Moves Display Box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3B1537))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "MOVES LEFT",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$movesLeftState",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("game_moves_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.width(65.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "$animatedScore",
                        color = SweetMagentaPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                            }
                            .testTag("game_score_text")
                    )

                    // Floating Score Addition Badge popup (using float transitions to bypass scope receiver bugs)
                    if (fadeAnim > 0.01f) {
                        Text(
                            text = "+$scoreDiff",
                            color = GoldenCoinSecondary,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = driftAnim
                                    alpha = fadeAnim
                                }
                                .padding(start = 42.dp)
                                .background(Color(0xE6250623), RoundedCornerShape(4.dp))
                                .border(1.dp, SweetMagentaPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A1E45))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SweetMagentaPrimary, GoldenCoinSecondary, SoftMintTertiary)
                                )
                            )
                    )
                }

                Text(
                    text = "$targetScoreState",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .width(50.dp),
                    textAlign = TextAlign.End
                )
            }
        }

        // Live Dynamic State feedback text
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C0F28))
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = feedbackState,
                color = if (feedbackState.contains("Combo")) SoftMintTertiary else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        // Game 6x6 Matching Grid Layout
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF2E0C2C))
                .border(2.dp, Color(0xFF4E164A), RoundedCornerShape(20.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (row in 0 until 6) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0 until 6) {
                            val index = row * 6 + col
                            val candy = boardState.getOrNull(index)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (candy != null && candy.type != -1) {
                                    CandyCell(
                                        candy = candy,
                                        isSelectTarget = selectedState == index,
                                        onClick = { viewModel.selectCandy(index) },
                                        onSwipe = { direction -> viewModel.swipeCandy(index, direction) },
                                        modifier = Modifier.testTag("candy_cell_$index")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Lock click overlay if calculating physics cascades
            if (isBusyState) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .clickable(enabled = true, onClick = {})
                )
            }
        }

        // Action Buttons bottom control
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.setupNewGame() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFF6F2669)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_reset_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart Game")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Board Reset / Shuffle", fontWeight = FontWeight.Bold)
            }
        }
    }

    // End-Game Dialogue Overlays (Won / Lost screens)
    if (gameStatusState != GameStatus.PLAYING) {
        AlertDialog(
            onDismissRequest = { /* Force action */ },
            containerColor = PlumDarkSurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (gameStatusState == GameStatus.WON) Icons.Default.Celebration else Icons.Default.SentimentVeryDissatisfied,
                        contentDescription = "Outcome Icon",
                        tint = if (gameStatusState == GameStatus.WON) GoldenCoinSecondary else SweetMagentaPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (gameStatusState == GameStatus.WON) "Level Cleared! 🎉" else "Game Over 💔",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (gameStatusState == GameStatus.WON)
                            "Swaaad aa gaya! You successfully reached the goal score in time!"
                        else
                            "No moves remaining but don't worry, your wallet is still secure.",
                        color = DarkText,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (gameStatusState == GameStatus.WON) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CaramelDarkBackground),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "REWARD CREDIT",
                                    color = DarkTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = "Coins",
                                        tint = GoldenCoinSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+${100 + (scoreState - targetScoreState).coerceAtLeast(0) / 10} Coins",
                                        color = GoldenCoinSecondary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Text(
                                    text = "Added directly to your balance!",
                                    color = SoftMintTertiary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Double Level Win Ad Bonus Option
                        Spacer(modifier = Modifier.height(12.dp))
                        val winBonusCoins = 100 + (scoreState - targetScoreState).coerceAtLeast(0) / 10
                        Button(
                            onClick = {
                                viewModel.triggerInterstitialAd(rewardAmount = winBonusCoins, isDoubleLevel = true)
                                viewModel.setupNewGame()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldenCoinSecondary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("double_reward_ad_btn")
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = "Double", tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Double Level Coins (+${winBonusCoins}) 📺", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    } else {
                        Text(
                            text = "Target score was $targetScoreState points.\nKeep playing to convert points to real cash!",
                            color = DarkTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.setupNewGame() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (gameStatusState == GameStatus.WON) SoftMintTertiary else SweetMagentaPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("game_result_confirm_btn")
                ) {
                    Text(
                        text = if (gameStatusState == GameStatus.WON) "Play Next Level 🚀" else "Play Again 🔄",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        )
    }
}

// Draw Candy Elements dynamically via luxurious 3D-effect canvas drawing
@Composable
fun CandyCell(
    candy: Candy,
    isSelectTarget: Boolean,
    onClick: () -> Unit,
    onSwipe: (direction: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (candy.isMatched) 0f else if (isSelectTarget) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val blinkState = rememberInfiniteTransition()
    val blinkGlow by blinkState.animateColor(
        initialValue = Color.White.copy(alpha = 0.2f),
        targetValue = Color.White.copy(alpha = 0.8f),
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    var dragX by remember { mutableStateOf(0f) }
    var dragY by remember { mutableStateOf(0f) }
    var swipeTriggered by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(candy.id) {
                detectDragGestures(
                    onDragStart = {
                        dragX = 0f
                        dragY = 0f
                        swipeTriggered = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!swipeTriggered) {
                            dragX += dragAmount.x
                            dragY += dragAmount.y
                            val threshold = 35f
                            if (kotlin.math.abs(dragX) > threshold || kotlin.math.abs(dragY) > threshold) {
                                swipeTriggered = true
                                if (kotlin.math.abs(dragX) > kotlin.math.abs(dragY)) {
                                    if (dragX > 0) onSwipe("RIGHT") else onSwipe("LEFT")
                                } else {
                                    if (dragY > 0) onSwipe("DOWN") else onSwipe("UP")
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        swipeTriggered = false
                    },
                    onDragEnd = {
                        swipeTriggered = false
                    }
                )
            }
            .clickable(onClick = onClick)
            .border(
                width = if (isSelectTarget) 3.dp else 1.dp,
                color = if (isSelectTarget) blinkGlow else Color(0xFF4C1048),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectTarget) Color(0xFF4A1046) else Color(0xFF330B30)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize(scale)
                    .aspectRatio(1f)
            ) {
                drawCustomCandyElement(candy.type)
            }
        }
    }
}

// Canvas Drawings for Sweet vector Candies (🍓, 🍊, 🍋, 🍏, 🫐, 🍇)
fun DrawScope.drawCustomCandyElement(type: Int) {
    val sizePx = size.width
    val center = Offset(sizePx / 2, sizePx / 2)
    val radius = sizePx * 0.38f

    when (type) {
        0 -> { // Strawberry Gem (Red Hearts)
            val path = Path().apply {
                val topLX = sizePx * 0.25f
                val topLY = sizePx * 0.35f
                val topRX = sizePx * 0.75f
                val topRY = sizePx * 0.35f
                val bottomX = sizePx * 0.5f
                val bottomY = sizePx * 0.82f

                moveTo(bottomX, bottomY)
                cubicTo(sizePx * 0.3f, sizePx * 0.65f, sizePx * 0.1f, sizePx * 0.45f, topLX, topLY)
                cubicTo(sizePx * 0.35f, sizePx * 0.15f, sizePx * 0.45f, sizePx * 0.25f, bottomX, sizePx * 0.35f)
                cubicTo(sizePx * 0.55f, sizePx * 0.25f, sizePx * 0.65f, sizePx * 0.15f, topRX, topRY)
                cubicTo(sizePx * 0.9f, sizePx * 0.45f, sizePx * 0.7f, sizePx * 0.65f, bottomX, bottomY)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF5252), CandyRed, Color(0xFF880E4F)),
                    center = center
                )
            )
            // Shine Reflection dot
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = sizePx * 0.08f,
                center = Offset(sizePx * 0.35f, sizePx * 0.35f)
            )
        }
        1 -> { // Orange Star Facet
            val path = Path().apply {
                val numPoints = 5
                val outerRadius = radius * 1.1f
                val innerRadius = outerRadius * 0.45f
                var angle = -Math.PI / 2
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val x = center.x + r * Math.cos(angle)
                    val y = center.y + r * Math.sin(angle)
                    if (i == 0) moveTo(x.toFloat(), y.toFloat()) else lineTo(x.toFloat(), y.toFloat())
                    angle += Math.PI / 5
                }
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(CandyOrange, Color(0xFFE65100), CandyOrange)
                )
            )
            // Inner Core Glow
            drawCircle(
                color = Color(0xFFFFB74D),
                radius = radius * 0.3f,
                center = center
            )
        }
        2 -> { // Golden Lemon Diamond Gym
            val path = Path().apply {
                moveTo(center.x, center.y - radius * 1.15f) // top
                lineTo(center.x + radius, center.y) // right
                lineTo(center.x, center.y + radius * 1.15f) // bottom
                lineTo(center.x - radius, center.y) // left
                close()
            }
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFF8D), CandyYellow, Color(0xFFF57F17)),
                    center = center
                )
            )
            // Glass cut facet line drawing
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(center.x, center.y - radius * 1.15f),
                end = Offset(center.x, center.y + radius * 1.15f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 2.dp.toPx()
            )
        }
        3 -> { // Bright Lime Chew Teardrop
            val path = Path().apply {
                moveTo(center.x, center.y - radius * 1.1f)
                cubicTo(
                    center.x + radius * 1.1f, center.y - radius * 0.2f,
                    center.x + radius, center.y + radius,
                    center.x, center.y + radius * 1.1f
                )
                cubicTo(
                    center.x - radius, center.y + radius,
                    center.x - radius * 1.1f, center.y - radius * 0.2f,
                    center.x, center.y - radius * 1.1f
                )
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB9F6CA), CandyGreen, Color(0xFF1B5E20))
                )
            )
            // Shiny highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = radius * 0.15f,
                center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f)
            )
        }
        4 -> { // Blueberry Sphere orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE0F7FA), CandyBlue, Color(0xFF0D47A1)),
                    center = Offset(center.x - radius * 0.15f, center.y - radius * 0.15f)
                ),
                radius = radius,
                center = center
            )
            // Mini circular 3D glass crest ring
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = radius * 0.5f,
                center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        5 -> { // Grapes Round Gum Purple
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFF3E5F5), CandyPurple, Color(0xFF4A148C))
                ),
                topLeft = Offset(center.x - radius, center.y - radius * 0.6f),
                size = Size(radius * 2f, radius * 1.2f)
            )
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, CandyPurple.copy(alpha = 0.7f), Color(0xFF2D0063))
                ),
                topLeft = Offset(center.x - radius * 0.6f, center.y - radius),
                size = Size(radius * 1.2f, radius * 2f)
            )
            // Small lighting reflection point
            drawCircle(
                color = Color.White,
                radius = radius * 0.1f,
                center = Offset(center.x + radius * 0.4f, center.y - radius * 0.2f)
            )
        }
    }
}

// ---------------------------------------------------------------------
// 2. WALLET & EARNING HISTORY SCREEN (Scratch Cards & Claims)
// ---------------------------------------------------------------------
@Composable
fun WalletScreen(viewModel: GameViewModel) {
    val walletState by viewModel.wallet.collectAsState()
    val transactionsState by viewModel.transactions.collectAsState()
    val isScratchAvailable by viewModel.isScratchCardAvailable.collectAsState()
    val scratchProgress by viewModel.scratchProgress.collectAsState()
    val scratchRewardAmount by viewModel.scratchCardReward.collectAsState()

    val context = LocalContext.current
    var isCheckingIn by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Earning metrics dashboard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PlumDarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF50194B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL WALLET CASH",
                        color = DarkTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "₹${String.format("%.2f", (walletState?.coins ?: 0) / 100.0)}",
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("wallet_cash_total")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Coins", color = DarkTextSecondary, fontSize = 11.sp)
                            Text("${walletState?.coins ?: 0} 🪙", color = GoldenCoinSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF4C1548)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lifetime Coins", color = DarkTextSecondary, fontSize = 11.sp)
                            Text("${walletState?.lifetimeCoins ?: 0} 🪙", color = SweetMagentaPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Interactive 7-Day Daily Check-In Hub with Streak multipliers
                    var lastCheckInDate = walletState?.lastCheckInDate ?: 0L
                    val currentStreak = walletState?.checkInStreak ?: 0
                    val now = System.currentTimeMillis()
                    val oneDayMillis = 24 * 60 * 60 * 1000L
                    val elapsed = now - lastCheckInDate
                    val canClaimToday = lastCheckInDate == 0L || elapsed >= oneDayMillis

                    var checkInRewardMessage by remember { mutableStateOf<String?>(null) }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFF50194B), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Streak Stars",
                                tint = SoftMintTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAILY REWARDS HUB",
                                color = SoftMintTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0x33FFB300), RoundedCornerShape(10.dp))
                                .border(1.dp, GoldenCoinSecondary, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$currentStreak Days Streak 🔥",
                                color = GoldenCoinSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Claim daily to grow your streak! If you miss a day, the sequence restarts back to Day 1.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days Grid (Days 1 to 4)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (day in 1..4) {
                            val isClaimed = day <= currentStreak
                            val isNext = day == currentStreak + 1 && canClaimToday
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isClaimed -> Color(0x3000C853)
                                            isNext -> Color(0x40FF2E83)
                                            else -> Color(0xFF1E071D)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            isClaimed -> Color(0xFF00C853)
                                            isNext -> SweetMagentaPrimary
                                            else -> Color(0x1AFFFFFF)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Day $day",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+${viewModel.getRewardForDay(day)}",
                                        color = if (isClaimed) Color(0xFF00C853) else GoldenCoinSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Days Grid (Days 5 to 7)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (day in 5..7) {
                            val isClaimed = day <= currentStreak
                            val isNext = day == currentStreak + 1 && canClaimToday
                            Box(
                                modifier = Modifier
                                    .weight(if (day == 7) 1.2f else 1.0f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isClaimed -> Color(0x3000C853)
                                            isNext -> Color(0x40FF2E83)
                                            day == 7 -> Color(0x20FFD700)
                                            else -> Color(0xFF1E071D)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            isClaimed -> Color(0xFF00C853)
                                            isNext -> SweetMagentaPrimary
                                            day == 7 -> GoldenCoinSecondary
                                            else -> Color(0x1AFFFFFF)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Day $day",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (day == 7) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("👑", fontSize = 8.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+${viewModel.getRewardForDay(day)}",
                                        color = if (isClaimed) Color(0xFF00C853) else GoldenCoinSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AnimatedVisibility(visible = checkInRewardMessage != null) {
                        Text(
                            text = checkInRewardMessage ?: "",
                            color = SoftMintTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Regular Claim Button
                        Button(
                            onClick = {
                                isCheckingIn = true
                                viewModel.claimDailyBonus(
                                    forceClaim = false,
                                    onSuccess = { reward ->
                                        isCheckingIn = false
                                        val nextStreakDay = if (currentStreak >= 7) 1 else currentStreak + 1
                                        checkInRewardMessage = "Day $nextStreakDay Bonus +$reward Coins Claimed! 🎉"
                                    },
                                    onFailure = {
                                        isCheckingIn = false
                                        checkInRewardMessage = "Already checked-in today! Please return tomorrow."
                                    }
                                )
                            },
                            enabled = canClaimToday,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SweetMagentaPrimary,
                                disabledContainerColor = Color(0x26FF2E83)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("daily_checkin_button")
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = "Gift box", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (canClaimToday) "Claim Today" else "Claimed today!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Cooldown fast-forward test button (Developer/Simulator option)
                        Button(
                            onClick = {
                                viewModel.claimDailyBonus(
                                    forceClaim = true,
                                    onSuccess = { reward ->
                                        val nextStreakDay = if (currentStreak >= 7) 1 else currentStreak + 1
                                        checkInRewardMessage = "Test Success: claimed Day $nextStreakDay reward of +$reward Coins! 🚀"
                                    },
                                    onFailure = {}
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B0A29)),
                            border = BorderStroke(1.dp, GoldenCoinSecondary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("admin_bypass_cooldown_btn")
                        ) {
                            Text("Fast-Forward Streak ⏳", color = GoldenCoinSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!canClaimToday) {
                        val hoursLeft = (23 - elapsed / (1000 * 60 * 60)).coerceAtLeast(0)
                        val minsLeft = (59 - (elapsed / (1000 * 60)) % 60).coerceAtLeast(0)
                        Text(
                            text = "Next login bonus available in ~${hoursLeft}h ${minsLeft}m",
                            color = DarkTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Come back tomorrow to increase your consecutive check-in streak!",
                            color = DarkTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Admin Monetization Ads Partner Console (Fulfill explicit prompt requirements)
        item {
            val adImpressionsState by viewModel.adImpressions.collectAsState()
            val adClicksState by viewModel.adClicks.collectAsState()
            val adRevenueState by viewModel.adRevenue.collectAsState()

            var adDetailsExpanded by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16031A)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, GoldenCoinSecondary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Green, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ADMIN MONETIZATION PANEL",
                                    color = GoldenCoinSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Admin Earnings: $${String.format("%.4f", adRevenueState)} USD",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { adDetailsExpanded = !adDetailsExpanded }
                        ) {
                            Icon(
                                imageVector = if (adDetailsExpanded) Icons.Default.TrendingUp else Icons.Default.Equalizer,
                                tint = GoldenCoinSecondary,
                                contentDescription = "Expand ad analytics stats"
                            )
                        }
                    }

                    if (adDetailsExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFF50194B), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Ad Impressions", color = DarkTextSecondary, fontSize = 10.sp)
                                Text("$adImpressionsState Views 👁️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Ad Clicks (CTR)", color = DarkTextSecondary, fontSize = 10.sp)
                                Text("$adClicksState Clicks 🖱️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Interstitial Video eCPM", color = DarkTextSecondary, fontSize = 10.sp)
                                Text("$11.00 USD 📈", color = SoftMintTertiary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Banner Ad eCPM", color = DarkTextSecondary, fontSize = 10.sp)
                                Text("$6.00 USD 📈", color = SoftMintTertiary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.triggerInterstitialAd(rewardAmount = 100, isDoubleLevel = false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF330932)),
                        border = BorderStroke(1.dp, SweetMagentaPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_trigger_video_ad_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play ad", tint = GoldenCoinSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Watch Paid Video (Add +100 Coins) 📺", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // Mini-Game: Scratch Card Claimer (Real-time gesture drag tracking)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0D26)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF5A1C53)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LUCKY SCRATCH CARD 🃏",
                        color = GoldenCoinSecondary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Scratch 60%+ of the card to reveal your coins bonus!",
                        color = DarkTextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isScratchAvailable) {
                        Box(
                            modifier = Modifier
                                .size(width = 240.dp, height = 130.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF3B1536))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, _ ->
                                        change.consume()
                                        // Update progress dynamically by touch position
                                        viewModel.updateScratchProgress(1.2f)
                                    }
                                }
                                .testTag("scratch_canvas_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Underneath Reward value Layer
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = "Golden Trophy",
                                    tint = GoldenCoinSecondary,
                                    modifier = Modifier.size(34.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$scratchRewardAmount",
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Coins Reward Alert!",
                                    fontSize = 11.sp,
                                    color = SoftMintTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Scratch Overlay Canvas (disappears linearly as scratch progress rises)
                            val brushColor = Color(0xFF7E727C)
                            val brushColorDark = Color(0xFF5C515B)
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val sizeWidth = size.width
                                val sizeHeight = size.height

                                // Cover with nice scratching grey patterns
                                drawRoundRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(brushColor, brushColorDark)
                                    ),
                                    size = size,
                                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                                    alpha = ((100f - scratchProgress) / 100f).coerceIn(0f, 1f)
                                )

                                if (scratchProgress < 60f) {
                                    // Scratch lines simulation
                                    val progressPct = "${scratchProgress.toInt()}% Scratched"
                                }
                            }

                            // Instruction hint overlay
                            if (scratchProgress < 10f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Touch & Drag here to scratch!",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Card Scratched state celebration
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.TaskAlt,
                                contentDescription = "Scratched Done",
                                tint = SoftMintTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Congrats +$scratchRewardAmount Coins Earned!",
                                color = SoftMintTertiary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.prepareScratchCard() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color(0xFF6F1D64)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Get New Scratch Card 🔄", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Ledger of recent wallet credits / transactions
        item {
            Text(
                text = "TRANSACTION LEDGER & EARNINGS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (transactionsState.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HistoryToggleOff, contentDescription = "No history", tint = DarkTextSecondary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No earning points registered yet.\nGo crush some candies!", color = DarkTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(transactionsState) { tx ->
                TransactionRow(tx)
            }
        }
    }
}

// Ledger row display for credits/debit transactions
@Composable
fun TransactionRow(tx: Transaction) {
    val formatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val dateStr = formatter.format(Date(tx.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = PlumDarkSurface.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_history_row_${tx.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (tx.coinAmount > 0) Color(0xFF1B4E28) else Color(0xFF5A122E),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.coinAmount > 0) Icons.Default.ChevronRight else Icons.Default.CurrencyExchange,
                        contentDescription = "Tx Type",
                        tint = if (tx.coinAmount > 0) SoftMintTertiary else SweetMagentaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Text(
                        text = dateStr,
                        color = DarkTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = if (tx.coinAmount > 0) "+${tx.coinAmount}" else "${tx.coinAmount}",
                    color = if (tx.coinAmount > 0) SoftMintTertiary else SweetMagentaPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (tx.coinAmount > 0) "₹${String.format("%.2f", tx.cashEquivalent)}" else "-₹${String.format("%.2f", -tx.cashEquivalent)}",
                    color = DarkTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// 3. REDEEM REWARDS SCREEN (Claim UPI/Paytm Forms & History List)
// ---------------------------------------------------------------------
data class RewardOption(
    val id: String,
    val title: String,
    val coinCost: Int,
    val cashValue: Int,
    val icon: ImageVector,
    val payoutPlatform: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RedeemScreen(viewModel: GameViewModel) {
    val walletState by viewModel.wallet.collectAsState()
    val requestsState by viewModel.redeemRequests.collectAsState()

    val currentCoins = walletState?.coins ?: 0

    val rewards = listOf(
        RewardOption("paytm_1", "Paytm Cash Transfer", 500, 5, Icons.Default.AccountBalanceWallet, "Paytm App"),
        RewardOption("paytm_2", "Paytm Bonus Cash", 1000, 10, Icons.Default.AccountBalanceWallet, "Paytm App"),
        RewardOption("upi_1", "Direct UPI Transfer", 1000, 10, Icons.Default.TrendingUp, "UPI Payout"),
        RewardOption("upi_2", "Direct UPI Payout", 2500, 25, Icons.Default.TrendingUp, "UPI Payout"),
        RewardOption("play_1", "Google Play Gift Key", 1500, 15, Icons.Default.Games, "Google Play Store"),
        RewardOption("amazon_1", "Amazon Pay Voucher", 2000, 20, Icons.Default.CardGiftcard, "Amazon App")
    )

    var selectedReward by remember { mutableStateOf<RewardOption?>(null) }
    var accountDetailsInput by remember { mutableStateOf("") }
    var redeemStatusMsg by remember { mutableStateOf("") }
    var redeemSuccessFlag by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Redemptions Intro Alert box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PlumDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF5F235A))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = "Safe Shield",
                        tint = SoftMintTertiary,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Guaranteed Instant Rewards!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "100 Game Coins = ₹1 INR Cash out value. Redemptions are sent within 24 hours to your linked payment profile.",
                            color = DarkTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Selected Redemption Claim Form layout
        if (selectedReward != null) {
            item {
                val reward = selectedReward!!
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C0A27)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, GoldenCoinSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Claim Form",
                                color = GoldenCoinSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { selectedReward = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        // Selected reward details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PlumDarkSurface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(reward.icon, contentDescription = "Icon", tint = SweetMagentaPrimary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(reward.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Cost: ${reward.coinCost} Coins  |  Value: ₹${reward.cashValue}", color = GoldenCoinSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // TextField detail entry
                        OutlinedTextField(
                            value = accountDetailsInput,
                            onValueChange = { accountDetailsInput = it },
                            label = { Text("UPI ID or Paytm Number", color = DarkTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SweetMagentaPrimary,
                                unfocusedBorderColor = Color(0xFF63285E),
                                focusedContainerColor = PlumDarkSurface,
                                unfocusedContainerColor = PlumDarkSurface
                            ),
                            placeholder = { Text("e.g. mobileNo or abc@upi", color = DarkTextSecondary.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("redeem_payout_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val checkBalanceOk = currentCoins >= reward.coinCost

                        Button(
                            onClick = {
                                viewModel.attemptRedemption(
                                    rewardName = reward.title,
                                    coinsValue = reward.coinCost,
                                    paymentDetails = accountDetailsInput,
                                    onSuccess = {
                                        redeemSuccessFlag = true
                                        redeemStatusMsg = "Recharge/Payout claim submitted successfully!"
                                        accountDetailsInput = ""
                                        selectedReward = null
                                    },
                                    onFailure = { err ->
                                        redeemStatusMsg = err
                                    }
                                )
                            },
                            enabled = checkBalanceOk,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (checkBalanceOk) SoftMintTertiary else Color.Gray,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("redeem_submit_claim_btn")
                        ) {
                            Text(
                                text = if (checkBalanceOk) "Confirm & Claim ₹${reward.cashValue} Cash" else "Insufficient Coins Balance",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        // Payout validation message
                        if (redeemStatusMsg.isNotEmpty()) {
                            Text(
                                text = redeemStatusMsg,
                                color = if (redeemSuccessFlag) SoftMintTertiary else SweetMagentaPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .testTag("redeem_status_text")
                            )
                        }
                    }
                }
            }
        }

        // Voucher Cards selection Grid
        item {
            Text(
                text = "CHOOSE YOUR REWARD REDEEM OPTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldenCoinSecondary,
                letterSpacing = 1.sp
            )
        }

        items(rewards.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (rewardItem in pair) {
                    RewardItemBox(
                        item = rewardItem,
                        userBalanceOk = currentCoins >= rewardItem.coinCost,
                        onSelect = {
                            selectedReward = rewardItem
                            redeemStatusMsg = ""
                            redeemSuccessFlag = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reward_item_card_${rewardItem.id}")
                    )
                }
            }
        }

        // History list header
        item {
            Text(
                text = "YOUR RECENT REDEMPTIONS STATUS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (requestsState.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No prior claims registered. Crush candies to earn and make requests!",
                        color = DarkTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(requestsState) { req ->
                RedeemStatusRow(req)
            }
        }
    }
}

// Reward option item card
@Composable
fun RewardItemBox(
    item: RewardOption,
    userBalanceOk: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PlumDarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (userBalanceOk) Color(0xFF5F255A) else Color(0xFF331430)
        ),
        modifier = modifier.clickable { onSelect() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF3A1137), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = "Payout platform logo",
                    tint = if (userBalanceOk) GoldenCoinSecondary else DarkTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "₹${item.cashValue} Cashout",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.payoutPlatform,
                color = DarkTextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Cost tag
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF230620))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MonetizationOn, contentDescription = "Coin", tint = GoldenCoinSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${item.coinCost}",
                    color = GoldenCoinSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// History redemption list row
@Composable
fun RedeemStatusRow(req: RedeemRequest) {
    val formatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val dateStr = formatter.format(Date(req.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = PlumDarkSurface.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Output,
                    contentDescription = "Payout indicator",
                    tint = SweetMagentaPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(req.rewardName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Sent to: ${req.paymentDetails}", color = DarkTextSecondary, fontSize = 11.sp, maxLines = 1)
                    Text(dateStr, color = DarkTextSecondary, fontSize = 10.sp)
                }
            }

            // Status Badge
            Surface(
                color = if (req.status == "PENDING") Color(0xFF332007) else Color(0xFF0F351B),
                border = BorderStroke(
                    1.dp,
                    if (req.status == "PENDING") GoldenCoinSecondary else SoftMintTertiary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = req.status,
                    color = if (req.status == "PENDING") GoldenCoinSecondary else SoftMintTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SimulatedBannerAd(viewModel: GameViewModel) {
    val bannerAds = remember {
        listOf(
            Triple("GROWW INVESTMENT", "Invest in Stocks & Mutual Funds directly with 0% commission!", "OPEN"),
            Triple("HOSTINGER INDIA", "Host your websites with 90% OFF + Free SSL & Domain!", "CLAIM DEAL"),
            Triple("PAYTM REWARDS", "Instant Cashback of up to ₹250 on scanner transfers!", "LINK NOW"),
            Triple("DREAM11 FANTASY", "Make your team today & win Cash prizes up to ₹1 Crore!", "PLAY GAME")
        )
    }

    var activeAdIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(7500) // cycle banners every 7.5 seconds
            activeAdIndex = (activeAdIndex + 1) % bannerAds.size
            viewModel.recordAdImpression("BANNER")
        }
    }

    val activeAd = bannerAds[activeAdIndex]

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF25031D)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SweetMagentaPrimary.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                viewModel.recordAdClick()
            }
            .testTag("banner_ad_wrapper")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x26FF2E83), RoundedCornerShape(3.dp))
                            .border(0.5.dp, SweetMagentaPrimary, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("AD", color = SweetMagentaPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeAd.first,
                        color = GoldenCoinSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activeAd.second,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SweetMagentaPrimary)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = activeAd.third,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun AdInterstitialOverlay(viewModel: GameViewModel) {
    val rewardAmount by viewModel.interstitialRewardAmount.collectAsState()
    val isDoubleLevel by viewModel.isRewardDoubleClaim.collectAsState()

    var secondsLeft by remember { mutableStateOf(5) }
    var skipEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        skipEnabled = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E0A1E))
                .border(2.dp, SweetMagentaPrimary, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x33FFB300), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AD • SPONSORED",
                        color = Color(0xFFFFB300),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = {
                        if (skipEnabled) {
                            viewModel.closeAndClaimAdReward()
                        }
                    },
                    enabled = skipEnabled,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (skipEnabled) Color.White else Color.Gray
                    )
                ) {
                    if (skipEnabled) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Skip Ad", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Close, contentDescription = "Skip", modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Text("Skip in ${secondsLeft}s", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2E002D))
                    .clickable {
                        viewModel.recordAdClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val scaleState = rememberInfiniteTransition()
                    val pulse by scaleState.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "adPulse"
                    )

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer {
                                scaleX = pulse
                                scaleY = pulse
                            }
                            .background(
                                Brush.linearGradient(listOf(GoldenCoinSecondary, SweetMagentaPrimary)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "RUPAY PAYTM BONUS CASH",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Claim ₹100 instantly on download!",
                        color = DarkTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Watch this sponsor's promo to claim your reward of:",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isDoubleLevel) "+$rewardAmount Coins (Doubled Level Bonus!)" else "+$rewardAmount Coins (Bonus)",
                color = GoldenCoinSecondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.recordAdClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldenCoinSecondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Install / Visit Sponsor Portal 🚀", color = Color.Black, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Admin receives monetization eCPM commission for every install click.",
                color = DarkTextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
