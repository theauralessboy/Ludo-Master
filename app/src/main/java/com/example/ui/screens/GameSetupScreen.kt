package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.LudoColor
import com.example.model.LudoTheme
import com.example.model.Player
import com.example.model.PlayerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(
    initialIsVsBot: Boolean = true,
    onStartGame: (List<Player>, GameMode, LudoTheme) -> Unit,
    onBack: () -> Unit
) {
    var playerCount by remember { mutableIntStateOf(4) }
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }
    var selectedTheme by remember { mutableStateOf(LudoTheme.CLASSIC) }

    val colors = listOf(LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW, LudoColor.BLUE)

    // Player configurations
    var player1Name by remember { mutableStateOf("Player 1") }
    var player1Type by remember { mutableStateOf(PlayerType.HUMAN) }

    var player2Name by remember { mutableStateOf(if (initialIsVsBot) "Bot Green" else "Player 2") }
    var player2Type by remember { mutableStateOf(if (initialIsVsBot) PlayerType.BOT_MEDIUM else PlayerType.HUMAN) }

    var player3Name by remember { mutableStateOf(if (initialIsVsBot) "Bot Yellow" else "Player 3") }
    var player3Type by remember { mutableStateOf(if (initialIsVsBot) PlayerType.BOT_MEDIUM else PlayerType.HUMAN) }

    var player4Name by remember { mutableStateOf(if (initialIsVsBot) "Bot Blue" else "Player 4") }
    var player4Type by remember { mutableStateOf(if (initialIsVsBot) PlayerType.BOT_MEDIUM else PlayerType.HUMAN) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Setup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("setup_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E88E5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player Count Selector (2, 3, 4)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF1E88E5))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Number of Players", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 4).forEach { count ->
                            val isSelected = playerCount == count
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { playerCount = count }
                                    .testTag("player_count_$count"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF1E88E5) else Color(0xFFF1F5F9),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$count Players",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Player Slots Customization
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Player Roster", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    // Player 1 (Red)
                    PlayerConfigRow(
                        color = LudoColor.RED,
                        name = player1Name,
                        type = player1Type,
                        onNameChange = { player1Name = it },
                        onTypeChange = { player1Type = it }
                    )

                    // Player 2 (Green)
                    PlayerConfigRow(
                        color = LudoColor.GREEN,
                        name = player2Name,
                        type = player2Type,
                        onNameChange = { player2Name = it },
                        onTypeChange = { player2Type = it }
                    )

                    // Player 3 (Yellow) - if 3 or 4 players
                    if (playerCount >= 3) {
                        PlayerConfigRow(
                            color = LudoColor.YELLOW,
                            name = player3Name,
                            type = player3Type,
                            onNameChange = { player3Name = it },
                            onTypeChange = { player3Type = it }
                        )
                    }

                    // Player 4 (Blue) - if 4 players
                    if (playerCount >= 4) {
                        PlayerConfigRow(
                            color = LudoColor.BLUE,
                            name = player4Name,
                            type = player4Type,
                            onNameChange = { player4Name = it },
                            onTypeChange = { player4Type = it }
                        )
                    }
                }
            }

            // Game Mode Selector (Classic, Quick, Golden Rush)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF1E88E5))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Game Rules Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GameMode.entries.forEach { mode ->
                        val isSelected = selectedMode == mode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedMode = mode }
                                .testTag("mode_${mode.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF1E88E5) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = mode.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color(0xFF1E88E5) else Color(0xFF1E293B)
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // Start Game Button
            Button(
                onClick = {
                    val players = mutableListOf<Player>()
                    // Player 1
                    players.add(Player(0, LudoColor.RED, player1Name.ifBlank { "Player 1" }, player1Type, isEnabled = true))
                    // Player 2
                    players.add(Player(1, LudoColor.GREEN, player2Name.ifBlank { "Player 2" }, player2Type, isEnabled = true))
                    // Player 3
                    if (playerCount >= 3) {
                        players.add(Player(2, LudoColor.YELLOW, player3Name.ifBlank { "Player 3" }, player3Type, isEnabled = true))
                    } else {
                        players.add(Player(2, LudoColor.YELLOW, "Yellow", PlayerType.BOT_EASY, isEnabled = false))
                    }
                    // Player 4
                    if (playerCount >= 4) {
                        players.add(Player(3, LudoColor.BLUE, player4Name.ifBlank { "Player 4" }, player4Type, isEnabled = true))
                    } else {
                        players.add(Player(3, LudoColor.BLUE, "Blue", PlayerType.BOT_EASY, isEnabled = false))
                    }

                    onStartGame(players, selectedMode, selectedTheme)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_match_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Match", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }
}

@Composable
private fun PlayerConfigRow(
    color: LudoColor,
    name: String,
    type: PlayerType,
    onNameChange: (String) -> Unit,
    onTypeChange: (PlayerType) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.containerColor.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(color.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = color.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = color.darkColor
                    )
                }

                // Type Toggle (Human vs Bot difficulties)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlayerType.entries.forEach { pType ->
                        val isSelected = type == pType
                        Surface(
                            modifier = Modifier
                                .clickable { onTypeChange(pType) }
                                .testTag("type_${color.name.lowercase()}_${pType.name.lowercase()}"),
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) color.primaryColor else Color.White.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = when (pType) {
                                    PlayerType.HUMAN -> "Human"
                                    PlayerType.BOT_EASY -> "Easy"
                                    PlayerType.BOT_MEDIUM -> "Med"
                                    PlayerType.BOT_HARD -> "Hard"
                                },
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Player Name", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}
