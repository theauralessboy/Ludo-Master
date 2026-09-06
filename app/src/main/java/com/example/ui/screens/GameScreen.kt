package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameState
import com.example.model.LudoTheme
import com.example.model.Token
import com.example.ui.components.DiceControlConsole
import com.example.ui.components.GameLogTicker
import com.example.ui.components.GameOverDialog
import com.example.ui.components.LudoBoard
import com.example.ui.components.PlayersStatusRow
import com.example.ui.components.RulesDialog
import com.example.ui.components.SettingsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameState: GameState,
    onRollDice: () -> Unit,
    onTokenClick: (Token) -> Unit,
    onRestartGame: () -> Unit,
    onBackToHome: () -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onThemeSelect: (LudoTheme) -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val theme = gameState.theme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = gameState.mode.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${gameState.activePlayers.size} Players Match",
                            fontSize = 11.sp,
                            color = Color(0xFFE3F2FD)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showExitDialog = true },
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Match")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSoundToggle(!gameState.soundEnabled) },
                        modifier = Modifier.testTag("quick_sound_toggle")
                    ) {
                        Icon(
                            imageVector = if (gameState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Toggle Sound"
                        )
                    }
                    IconButton(
                        onClick = { showRulesDialog = true },
                        modifier = Modifier.testTag("game_rules_button")
                    ) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Rules")
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("game_settings_button")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E88E5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = if (theme.isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Players Status Mini Cards Header
            PlayersStatusRow(
                gameState = gameState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 2. The 15x15 Ludo Board (Responsive Box)
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LudoBoard(
                    gameState = gameState,
                    onTokenClick = onTokenClick,
                    modifier = Modifier.fillMaxWidth(0.98f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Live Game Action History Ticker
            GameLogTicker(
                latestLog = gameState.gameLogs.lastOrNull(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Interactive Player & Animated Dice Controller Console
            DiceControlConsole(
                gameState = gameState,
                onRollClick = onRollDice,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Game Over Dialog
        if (gameState.isGameOver) {
            GameOverDialog(
                gameState = gameState,
                onPlayAgain = onRestartGame,
                onBackToHome = onBackToHome
            )
        }

        // Exit / Surrender Confirmation Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Quit Match?") },
                text = { Text("Do you want to abandon the current match and return to the main menu?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitDialog = false
                            onBackToHome()
                        }
                    ) {
                        Text("Quit Match", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Keep Playing")
                    }
                }
            )
        }

        // Rules Guide Dialog
        if (showRulesDialog) {
            RulesDialog(onDismiss = { showRulesDialog = false })
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                soundEnabled = gameState.soundEnabled,
                hapticsEnabled = gameState.hapticsEnabled,
                currentTheme = gameState.theme,
                onSoundToggle = onSoundToggle,
                onHapticsToggle = onHapticsToggle,
                onThemeSelect = onThemeSelect,
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}
