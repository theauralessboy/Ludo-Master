package com.example.model

enum class GameMode(
    val title: String,
    val description: String,
    val tokensNeededToWin: Int,
    val tokenCount: Int
) {
    CLASSIC(
        title = "Classic Ludo",
        description = "All 4 tokens must reach the home triangle to win.",
        tokensNeededToWin = 4,
        tokenCount = 4
    ),
    QUICK(
        title = "Quick Match",
        description = "Fast-paced match! First player to bring 2 tokens home wins.",
        tokensNeededToWin = 2,
        tokenCount = 4
    ),
    RUSH(
        title = "Golden Rush",
        description = "Instant thrill! First single token to reach home wins the game.",
        tokensNeededToWin = 1,
        tokenCount = 4
    )
}
