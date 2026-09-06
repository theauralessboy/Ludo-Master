package com.example.model

enum class DiceState {
    WAITING_ROLL,
    ROLLING,
    ROLLED_CHOICE,
    ANIMATING_MOVE,
    TURN_TRANSITION,
    GAME_OVER
}

data class GameLogEntry(
    val id: Long = System.nanoTime(),
    val text: String,
    val color: LudoColor? = null,
    val isHighlight: Boolean = false
)

data class GameState(
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val diceValue: Int = 1,
    val diceState: DiceState = DiceState.WAITING_ROLL,
    val diceAnimNumber: Int = 1,
    val isExtraTurn: Boolean = false,
    val extraTurnReason: String? = null,
    val consecutiveSixes: Int = 0,
    val winners: List<Player> = emptyList(),
    val mode: GameMode = GameMode.CLASSIC,
    val theme: LudoTheme = LudoTheme.CLASSIC,
    val gameLogs: List<GameLogEntry> = emptyList(),
    val movingToken: Token? = null,
    val currentHopStep: Int? = null,
    val capturedToken: Token? = null,
    val startTime: Long = System.currentTimeMillis(),
    val isPaused: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val isAutoMoving: Boolean = false
) {
    val currentPlayer: Player?
        get() = if (players.isNotEmpty() && currentPlayerIndex in players.indices) {
            players[currentPlayerIndex]
        } else null

    val isGameOver: Boolean get() = diceState == DiceState.GAME_OVER

    val activePlayers: List<Player> get() = players.filter { it.isEnabled }
    val remainingActivePlayers: List<Player> get() = activePlayers.filter { !it.hasFinished(mode.tokensNeededToWin) }
}
