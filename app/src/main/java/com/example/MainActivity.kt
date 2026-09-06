package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GameMode
import com.example.model.LudoColor
import com.example.model.LudoTheme
import com.example.model.Player
import com.example.model.PlayerType
import com.example.ui.components.RulesDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.GameSetupScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LudoViewModel

enum class Screen {
    HOME,
    SETUP,
    GAME,
    STATS
}

class MainActivity : ComponentActivity() {
    private val viewModel: LudoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LudoApp(viewModel)
            }
        }
    }
}

@Composable
fun LudoApp(viewModel: LudoViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var setupIsVsBot by remember { mutableStateOf(true) }

    var showHomeRulesDialog by remember { mutableStateOf(false) }
    var showHomeSettingsDialog by remember { mutableStateOf(false) }

    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val allStats by viewModel.allStats.collectAsStateWithLifecycle()
    val totalGames by viewModel.totalGames.collectAsStateWithLifecycle()
    val totalWins by viewModel.totalWins.collectAsStateWithLifecycle()
    val totalCaptures by viewModel.totalCaptures.collectAsStateWithLifecycle()
    val totalSixes by viewModel.totalSixes.collectAsStateWithLifecycle()

    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    when (currentScreen) {
        Screen.HOME -> {
            HomeScreen(
                totalGames = totalGames,
                totalWins = totalWins,
                onQuickPlay = {
                    // Start standard 4-player game (1 Human vs 3 Bots)
                    viewModel.startNewGame(
                        playersConfig = listOf(
                            Player(0, LudoColor.RED, "Player 1", PlayerType.HUMAN, isEnabled = true),
                            Player(1, LudoColor.GREEN, "Bot Green", PlayerType.BOT_MEDIUM, isEnabled = true),
                            Player(2, LudoColor.YELLOW, "Bot Yellow", PlayerType.BOT_MEDIUM, isEnabled = true),
                            Player(3, LudoColor.BLUE, "Bot Blue", PlayerType.BOT_MEDIUM, isEnabled = true)
                        ),
                        mode = GameMode.CLASSIC
                    )
                    currentScreen = Screen.GAME
                },
                onVsBotClick = {
                    setupIsVsBot = true
                    currentScreen = Screen.SETUP
                },
                onPassAndPlayClick = {
                    setupIsVsBot = false
                    currentScreen = Screen.SETUP
                },
                onRushModeClick = {
                    viewModel.startNewGame(
                        playersConfig = listOf(
                            Player(0, LudoColor.RED, "Player 1", PlayerType.HUMAN, isEnabled = true),
                            Player(1, LudoColor.GREEN, "Bot Green", PlayerType.BOT_HARD, isEnabled = true),
                            Player(2, LudoColor.YELLOW, "Bot Yellow", PlayerType.BOT_HARD, isEnabled = true),
                            Player(3, LudoColor.BLUE, "Bot Blue", PlayerType.BOT_HARD, isEnabled = true)
                        ),
                        mode = GameMode.RUSH
                    )
                    currentScreen = Screen.GAME
                },
                onStatsClick = { currentScreen = Screen.STATS },
                onRulesClick = { showHomeRulesDialog = true },
                onSettingsClick = { showHomeSettingsDialog = true }
            )

            if (showHomeRulesDialog) {
                RulesDialog(onDismiss = { showHomeRulesDialog = false })
            }

            if (showHomeSettingsDialog) {
                SettingsDialog(
                    soundEnabled = gameState.soundEnabled,
                    hapticsEnabled = gameState.hapticsEnabled,
                    currentTheme = gameState.theme,
                    onSoundToggle = { viewModel.setSoundEnabled(it) },
                    onHapticsToggle = { viewModel.setHapticsEnabled(it) },
                    onThemeSelect = { viewModel.setTheme(it) },
                    onDismiss = { showHomeSettingsDialog = false }
                )
            }
        }

        Screen.SETUP -> {
            GameSetupScreen(
                initialIsVsBot = setupIsVsBot,
                onStartGame = { players, mode, theme ->
                    viewModel.startNewGame(players, mode, theme)
                    currentScreen = Screen.GAME
                },
                onBack = { currentScreen = Screen.HOME }
            )
        }

        Screen.GAME -> {
            GameScreen(
                gameState = gameState,
                onRollDice = { viewModel.rollDice() },
                onTokenClick = { viewModel.onTokenClicked(it) },
                onRestartGame = {
                    viewModel.startNewGame(gameState.players, gameState.mode, gameState.theme)
                },
                onBackToHome = { currentScreen = Screen.HOME },
                onSoundToggle = { viewModel.setSoundEnabled(it) },
                onHapticsToggle = { viewModel.setHapticsEnabled(it) },
                onThemeSelect = { viewModel.setTheme(it) }
            )
        }

        Screen.STATS -> {
            StatisticsScreen(
                statsList = allStats,
                totalGames = totalGames,
                totalWins = totalWins,
                totalCaptures = totalCaptures ?: 0,
                totalSixes = totalSixes ?: 0,
                onClearStats = { viewModel.clearStats() },
                onBack = { currentScreen = Screen.HOME }
            )
        }
    }
}
