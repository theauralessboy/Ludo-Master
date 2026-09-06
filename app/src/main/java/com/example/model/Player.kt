package com.example.model

enum class PlayerType(val label: String) {
    HUMAN("Human"),
    BOT_EASY("Bot (Easy)"),
    BOT_MEDIUM("Bot (Normal)"),
    BOT_HARD("Bot (Master)")
}

data class Player(
    val id: Int,
    val color: LudoColor,
    val name: String,
    val type: PlayerType = PlayerType.HUMAN,
    val isEnabled: Boolean = true,
    val tokens: List<Token> = (0..3).map { Token(id = it, color = color) },
    val rank: Int = 0, // 0 = playing, 1 = 1st place, etc.
    val consecutiveSixes: Int = 0,
    val capturesCount: Int = 0,
    val sixesRolled: Int = 0,
    val totalRolls: Int = 0
) {
    val isHuman: Boolean get() = type == PlayerType.HUMAN
    val isBot: Boolean get() = !isHuman

    val homeTokensCount: Int get() = tokens.count { it.isReachedHome }
    val baseTokensCount: Int get() = tokens.count { it.isInBase }
    val activeTokensCount: Int get() = tokens.count { !it.isInBase && !it.isReachedHome }

    fun hasFinished(tokensNeededToWin: Int = 4): Boolean {
        return homeTokensCount >= tokensNeededToWin
    }
}
