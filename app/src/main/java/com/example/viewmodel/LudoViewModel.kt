package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.GameStatEntity
import com.example.data.GameStatsRepository
import com.example.data.LudoDatabase
import com.example.model.DiceState
import com.example.model.GameLogEntry
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.GridCoord
import com.example.model.LudoColor
import com.example.model.LudoCoordinates
import com.example.model.LudoTheme
import com.example.model.Player
import com.example.model.PlayerType
import com.example.model.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class LudoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = LudoDatabase.getDatabase(application)
    private val repository = GameStatsRepository(database.gameStatsDao())
    val soundManager = SoundManager(application)

    val allStats = repository.allStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val totalGames = repository.totalGames.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val totalWins = repository.totalWins.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val totalCaptures = repository.totalCaptures.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val totalSixes = repository.totalSixes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var botJob: Job? = null
    private var rollAnimationJob: Job? = null
    private var tokenMoveJob: Job? = null

    init {
        // Initialize default 4-player game (1 Human vs 3 Bots)
        startNewGame(
            playersConfig = listOf(
                Player(0, LudoColor.RED, "Player 1", PlayerType.HUMAN, isEnabled = true),
                Player(1, LudoColor.GREEN, "Bot Green", PlayerType.BOT_MEDIUM, isEnabled = true),
                Player(2, LudoColor.YELLOW, "Bot Yellow", PlayerType.BOT_MEDIUM, isEnabled = true),
                Player(3, LudoColor.BLUE, "Bot Blue", PlayerType.BOT_MEDIUM, isEnabled = true)
            ),
            mode = GameMode.CLASSIC,
            theme = LudoTheme.CLASSIC
        )
    }

    fun startNewGame(
        playersConfig: List<Player>,
        mode: GameMode = _gameState.value.mode,
        theme: LudoTheme = _gameState.value.theme
    ) {
        cancelAllJobs()
        val initialPlayers = playersConfig.map { p ->
            p.copy(
                tokens = (0 until 4).map { Token(id = it, color = p.color) },
                rank = 0,
                consecutiveSixes = 0,
                capturesCount = 0,
                sixesRolled = 0,
                totalRolls = 0
            )
        }

        // Find first enabled player
        val firstActiveIndex = initialPlayers.indexOfFirst { it.isEnabled }.coerceAtLeast(0)

        _gameState.value = GameState(
            players = initialPlayers,
            currentPlayerIndex = firstActiveIndex,
            diceValue = 1,
            diceState = DiceState.WAITING_ROLL,
            diceAnimNumber = 1,
            mode = mode,
            theme = theme,
            startTime = System.currentTimeMillis(),
            gameLogs = listOf(
                GameLogEntry(text = "Game started! Mode: ${mode.title}", isHighlight = true),
                GameLogEntry(text = "${initialPlayers[firstActiveIndex].name}'s turn to roll.")
            ),
            soundEnabled = soundManager.isSoundEnabled,
            hapticsEnabled = soundManager.isHapticsEnabled
        )

        checkBotTurn()
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundManager.isSoundEnabled = enabled
        _gameState.value = _gameState.value.copy(soundEnabled = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        soundManager.isHapticsEnabled = enabled
        _gameState.value = _gameState.value.copy(hapticsEnabled = enabled)
    }

    fun setTheme(theme: LudoTheme) {
        _gameState.value = _gameState.value.copy(theme = theme)
    }

    fun rollDice() {
        val state = _gameState.value
        if (state.diceState != DiceState.WAITING_ROLL || state.isGameOver) return
        val currentP = state.currentPlayer ?: return

        cancelAllJobs()
        _gameState.value = state.copy(
            diceState = DiceState.ROLLING
        )

        soundManager.playDiceRoll()

        rollAnimationJob = viewModelScope.launch(Dispatchers.Main) {
            // Rapidly flicker dice numbers for animation
            val rollTicks = 8
            for (i in 0 until rollTicks) {
                _gameState.value = _gameState.value.copy(
                    diceAnimNumber = Random.nextInt(1, 7)
                )
                delay(40)
            }

            // Final rolled dice value
            val finalRoll = Random.nextInt(1, 7)
            val updatedConsecutive = if (finalRoll == 6) currentP.consecutiveSixes + 1 else 0
            val updatedSixes = if (finalRoll == 6) currentP.sixesRolled + 1 else currentP.sixesRolled
            val updatedRolls = currentP.totalRolls + 1

            val updatedPlayers = state.players.mapIndexed { idx, p ->
                if (idx == state.currentPlayerIndex) {
                    p.copy(
                        consecutiveSixes = updatedConsecutive,
                        sixesRolled = updatedSixes,
                        totalRolls = updatedRolls
                    )
                } else p
            }

            _gameState.value = _gameState.value.copy(
                players = updatedPlayers,
                diceValue = finalRoll,
                diceAnimNumber = finalRoll
            )

            // Rule: 3 consecutive sixes forfeits turn
            if (updatedConsecutive >= 3) {
                addLog("${currentP.name} rolled three 6s in a row! Turn passed.", currentP.color, true)
                delay(900)
                nextTurn(grantExtraTurn = false)
                return@launch
            }

            // Calculate valid movable tokens
            val movableTokens = currentP.tokens.filter { it.canMove(finalRoll) }

            if (movableTokens.isEmpty()) {
                addLog("${currentP.name} rolled a $finalRoll. (No valid moves)", currentP.color)
                _gameState.value = _gameState.value.copy(diceState = DiceState.TURN_TRANSITION)
                delay(700)
                nextTurn(grantExtraTurn = false)
            } else {
                // Mark movable tokens in state
                val playersWithMovable = updatedPlayers.mapIndexed { idx, p ->
                    if (idx == state.currentPlayerIndex) {
                        p.copy(tokens = p.tokens.map { token ->
                            token.copy(isMovable = token.canMove(finalRoll))
                        })
                    } else p
                }

                _gameState.value = _gameState.value.copy(
                    players = playersWithMovable,
                    diceState = DiceState.ROLLED_CHOICE
                )

                if (currentP.isBot) {
                    // Bot AI decision
                    delay(500)
                    val chosenToken = chooseBestTokenForBot(currentP, movableTokens, finalRoll)
                    moveToken(chosenToken)
                } else if (movableTokens.size == 1) {
                    // Only 1 valid move for human -> auto-move smoothly
                    delay(350)
                    moveToken(movableTokens.first())
                }
            }
        }
    }

    fun onTokenClicked(token: Token) {
        val state = _gameState.value
        val currentP = state.currentPlayer ?: return
        if (state.diceState != DiceState.ROLLED_CHOICE) return
        if (currentP.isBot) return // Ignore manual taps during bot turn
        if (token.color != currentP.color) return
        if (!token.isMovable) return

        moveToken(token)
    }

    private fun moveToken(token: Token) {
        val state = _gameState.value
        val currentP = state.currentPlayer ?: return
        val dice = state.diceValue

        cancelAllJobs()

        tokenMoveJob = viewModelScope.launch(Dispatchers.Main) {
            _gameState.value = _gameState.value.copy(
                diceState = DiceState.ANIMATING_MOVE,
                movingToken = token
            )

            val startStep = token.relativeStep
            val targetStep = token.targetStep(dice)

            if (startStep == -1) {
                // Token exiting base directly to 0
                soundManager.playStepHop()
                delay(120)
                updateTokenStep(token.color, token.id, 0)
                addLog("${currentP.name} moved a token out of base!", currentP.color)
            } else {
                // Step-by-step hop animation
                for (step in (startStep + 1)..targetStep) {
                    _gameState.value = _gameState.value.copy(currentHopStep = step)
                    updateTokenStep(token.color, token.id, step)
                    soundManager.playStepHop()
                    delay(110)
                }
            }

            _gameState.value = _gameState.value.copy(
                movingToken = null,
                currentHopStep = null
            )

            // Evaluate Landing Outcomes
            var grantBonus = false
            var bonusReason: String? = null

            // 1. Check if token reached Home (step 56)
            if (targetStep == 56) {
                soundManager.playHomeGoal()
                addLog("🏆 ${currentP.name}'s token reached the Home Triangle!", currentP.color, true)
                grantBonus = true
                bonusReason = "Token reached Home! +1 Turn"

                // Check if player has finished the game
                val updatedCurrentP = _gameState.value.players[state.currentPlayerIndex]
                if (updatedCurrentP.hasFinished(state.mode.tokensNeededToWin) && updatedCurrentP.rank == 0) {
                    val nextRank = _gameState.value.winners.size + 1
                    val finishedPlayer = updatedCurrentP.copy(rank = nextRank)
                    val newWinners = _gameState.value.winners + finishedPlayer

                    val updatedPlayers = _gameState.value.players.mapIndexed { idx, p ->
                        if (idx == state.currentPlayerIndex) finishedPlayer else p
                    }

                    _gameState.value = _gameState.value.copy(
                        players = updatedPlayers,
                        winners = newWinners
                    )

                    addLog("👑 ${currentP.name} finished in Rank #$nextRank!", currentP.color, true)

                    // Check Game Over condition (e.g. only 1 active player left or 1st winner in Rush mode)
                    val remaining = updatedPlayers.filter { it.isEnabled && !it.hasFinished(state.mode.tokensNeededToWin) }
                    if (remaining.size <= 1 || state.mode == GameMode.RUSH) {
                        endGame(newWinners)
                        return@launch
                    }
                }
            }

            // 2. Check Capture of Opponent Token
            if (targetStep in 0..50 && !LudoCoordinates.isSafeStep(token.color, targetStep)) {
                val landedCommonIndex = (token.color.startIndex + targetStep) % 52
                var capturedOpponent: Token? = null
                var capturedPlayerName: String = ""

                val playersAfterCapture = _gameState.value.players.map { otherPlayer ->
                    if (otherPlayer.color != token.color && otherPlayer.isEnabled) {
                        val newTokens = otherPlayer.tokens.map { opponentToken ->
                            if (opponentToken.isOnTrack) {
                                val opponentCommonIndex = (opponentToken.color.startIndex + opponentToken.relativeStep) % 52
                                if (opponentCommonIndex == landedCommonIndex && capturedOpponent == null) {
                                    capturedOpponent = opponentToken
                                    capturedPlayerName = otherPlayer.name
                                    opponentToken.copy(relativeStep = -1, isRecentlyCaptured = true)
                                } else opponentToken
                            } else opponentToken
                        }
                        otherPlayer.copy(tokens = newTokens)
                    } else otherPlayer
                }

                if (capturedOpponent != null) {
                    soundManager.playCapture()
                    grantBonus = true
                    bonusReason = "Captured opponent! +1 Turn"

                    // Increment captures count for current player
                    val finalPlayers = playersAfterCapture.mapIndexed { idx, p ->
                        if (idx == state.currentPlayerIndex) {
                            p.copy(capturesCount = p.capturesCount + 1)
                        } else p
                    }

                    _gameState.value = _gameState.value.copy(
                        players = finalPlayers,
                        capturedToken = capturedOpponent
                    )

                    addLog("💥 ${currentP.name} captured $capturedPlayerName's token!", currentP.color, true)
                    delay(300)
                }
            }

            // 3. Safe Star Landing Sound
            if (LudoCoordinates.isSafeStep(token.color, targetStep) && targetStep > 0 && targetStep < 56) {
                soundManager.playSafeTile()
            }

            // 4. Bonus from Rolling 6
            if (dice == 6 && !grantBonus) {
                grantBonus = true
                bonusReason = "Rolled a 6! +1 Turn"
                soundManager.playBonusTurn()
                addLog("🎲 ${currentP.name} rolled a 6! Bonus turn.", currentP.color)
            }

            // Clear movable flags on tokens
            val clearedPlayers = _gameState.value.players.map { p ->
                p.copy(tokens = p.tokens.map { it.copy(isMovable = false, isRecentlyCaptured = false) })
            }
            _gameState.value = _gameState.value.copy(players = clearedPlayers)

            delay(350)
            nextTurn(grantExtraTurn = grantBonus, reason = bonusReason)
        }
    }

    private fun updateTokenStep(color: LudoColor, tokenId: Int, newStep: Int) {
        val updated = _gameState.value.players.map { p ->
            if (p.color == color) {
                p.copy(tokens = p.tokens.map { if (it.id == tokenId) it.copy(relativeStep = newStep) else it })
            } else p
        }
        _gameState.value = _gameState.value.copy(players = updated)
    }

    private fun nextTurn(grantExtraTurn: Boolean, reason: String? = null) {
        val state = _gameState.value
        if (state.isGameOver) return

        if (grantExtraTurn) {
            val currentP = state.currentPlayer ?: return
            if (currentP.hasFinished(state.mode.tokensNeededToWin)) {
                // If current player finished with this move, pass turn to next player
                advanceToNextPlayer()
            } else {
                _gameState.value = state.copy(
                    diceState = DiceState.WAITING_ROLL,
                    isExtraTurn = true,
                    extraTurnReason = reason
                )
                checkBotTurn()
            }
        } else {
            advanceToNextPlayer()
        }
    }

    private fun advanceToNextPlayer() {
        val state = _gameState.value
        val activeList = state.players
        if (activeList.isEmpty()) return

        var nextIndex = (state.currentPlayerIndex + 1) % activeList.size
        var safetyCounter = 0

        // Find next enabled player who hasn't finished
        while (safetyCounter < activeList.size) {
            val candidate = activeList[nextIndex]
            if (candidate.isEnabled && !candidate.hasFinished(state.mode.tokensNeededToWin)) {
                break
            }
            nextIndex = (nextIndex + 1) % activeList.size
            safetyCounter++
        }

        val nextPlayer = activeList[nextIndex]
        _gameState.value = state.copy(
            currentPlayerIndex = nextIndex,
            diceState = DiceState.WAITING_ROLL,
            isExtraTurn = false,
            extraTurnReason = null,
            players = state.players.mapIndexed { idx, p ->
                if (idx != nextIndex) p.copy(consecutiveSixes = 0) else p
            }
        )

        addLog("${nextPlayer.name}'s turn.", nextPlayer.color)
        checkBotTurn()
    }

    private fun checkBotTurn() {
        val state = _gameState.value
        val currentP = state.currentPlayer ?: return
        if (currentP.isBot && state.diceState == DiceState.WAITING_ROLL && !state.isGameOver) {
            botJob = viewModelScope.launch(Dispatchers.Main) {
                delay(600)
                rollDice()
            }
        }
    }

    private fun chooseBestTokenForBot(player: Player, movableTokens: List<Token>, dice: Int): Token {
        if (movableTokens.size == 1) return movableTokens.first()

        val difficulty = player.type
        val evaluatedTokens = movableTokens.map { token ->
            val target = token.targetStep(dice)
            var score = 10.0

            // Priority 1: Reach Home Goal (Highest)
            if (target == 56) {
                score += 1200.0
            }

            // Priority 2: Capture opponent token
            if (target in 0..50 && !LudoCoordinates.isSafeStep(token.color, target)) {
                val targetCommonIndex = (token.color.startIndex + target) % 52
                val canCapture = _gameState.value.players.any { other ->
                    other.color != token.color && other.isEnabled && other.tokens.any { opp ->
                        opp.isOnTrack && (opp.color.startIndex + opp.relativeStep) % 52 == targetCommonIndex
                    }
                }
                if (canCapture) {
                    score += 700.0
                }
            }

            // Priority 3: Bring token out of base on 6
            if (token.isInBase && dice == 6) {
                score += if (player.activeTokensCount < 2) 300.0 else 180.0
            }

            // Priority 4: Land on safe star tile
            if (LudoCoordinates.isSafeStep(token.color, target)) {
                score += 150.0
            }

            // Priority 5: Enter safe home stretch (51..55)
            if (target in 51..55 && token.relativeStep < 51) {
                score += 200.0
            }

            // Priority 6: Forward progression
            score += target * 2.0

            // Strategic adjustments based on difficulty level
            when (difficulty) {
                PlayerType.BOT_EASY -> {
                    // Add substantial randomness for easy bot
                    score += Random.nextDouble(0.0, 180.0)
                }
                PlayerType.BOT_MEDIUM -> {
                    score += Random.nextDouble(0.0, 40.0)
                }
                PlayerType.BOT_HARD -> {
                    // Escape danger: If currently vulnerable to capture, high incentive to move
                    if (token.isOnTrack && !LudoCoordinates.isSafeStep(token.color, token.relativeStep)) {
                        val currentCommon = (token.color.startIndex + token.relativeStep) % 52
                        val inDanger = _gameState.value.players.any { other ->
                            other.color != token.color && other.isEnabled && other.tokens.any { opp ->
                                if (opp.isOnTrack) {
                                    val oppCommon = (opp.color.startIndex + opp.relativeStep) % 52
                                    val dist = (currentCommon - oppCommon + 52) % 52
                                    dist in 1..6
                                } else false
                            }
                        }
                        if (inDanger) score += 120.0
                    }
                    score += Random.nextDouble(0.0, 8.0)
                }
                else -> {}
            }

            Pair(token, score)
        }

        return evaluatedTokens.maxByOrNull { it.second }?.first ?: movableTokens.first()
    }

    private fun endGame(winners: List<Player>) {
        soundManager.playVictory()
        _gameState.value = _gameState.value.copy(
            diceState = DiceState.GAME_OVER,
            winners = winners
        )

        val firstWinner = winners.firstOrNull() ?: return
        val humanPlayer = _gameState.value.players.firstOrNull { it.isHuman }
        val isHumanWinner = firstWinner.isHuman
        val userRank = if (humanPlayer != null) {
            val idx = winners.indexOfFirst { it.id == humanPlayer.id }
            if (idx >= 0) idx + 1 else winners.size + 1
        } else 0

        val durationSec = (System.currentTimeMillis() - _gameState.value.startTime) / 1000

        val totalCaptures = _gameState.value.players.sumOf { it.capturesCount }
        val totalSixes = _gameState.value.players.sumOf { it.sixesRolled }
        val totalTurns = _gameState.value.players.sumOf { it.totalRolls }

        viewModelScope.launch(Dispatchers.IO) {
            repository.recordGame(
                GameStatEntity(
                    gameMode = _gameState.value.mode.title,
                    playerCount = _gameState.value.activePlayers.size,
                    winnerColor = firstWinner.color.displayName,
                    winnerName = firstWinner.name,
                    isHumanWinner = isHumanWinner,
                    durationSeconds = durationSec,
                    totalTurns = totalTurns,
                    totalCaptures = totalCaptures,
                    totalSixes = totalSixes,
                    userRank = userRank
                )
            )
        }

        addLog("🎉 GAME OVER! Winner: ${firstWinner.name} (${firstWinner.color.displayName})", firstWinner.color, true)
    }

    fun clearStats() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearStats()
        }
    }

    private fun addLog(text: String, color: LudoColor? = null, isHighlight: Boolean = false) {
        val entry = GameLogEntry(text = text, color = color, isHighlight = isHighlight)
        val updated = (_gameState.value.gameLogs + entry).takeLast(25)
        _gameState.value = _gameState.value.copy(gameLogs = updated)
    }

    private fun cancelAllJobs() {
        botJob?.cancel()
        rollAnimationJob?.cancel()
        tokenMoveJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
    }
}
