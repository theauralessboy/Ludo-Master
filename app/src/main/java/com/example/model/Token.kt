package com.example.model

data class Token(
    val id: Int,
    val color: LudoColor,
    val relativeStep: Int = -1, // -1: Base, 0..50: Track, 51..55: Home Stretch, 56: Home
    val isMovable: Boolean = false,
    val isMoving: Boolean = false,
    val isRecentlyCaptured: Boolean = false
) {
    val isInBase: Boolean get() = relativeStep < 0
    val isReachedHome: Boolean get() = relativeStep == 56
    val isOnTrack: Boolean get() = relativeStep in 0..50
    val isInHomeCorridor: Boolean get() = relativeStep in 51..55

    fun canMove(diceValue: Int): Boolean {
        if (isReachedHome) return false
        if (isInBase) return diceValue == 6
        return (relativeStep + diceValue) <= 56
    }

    fun targetStep(diceValue: Int): Int {
        if (isInBase && diceValue == 6) return 0
        return (relativeStep + diceValue).coerceAtMost(56)
    }
}
