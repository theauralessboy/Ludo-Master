package com.example.model

data class GridCoord(val col: Int, val row: Int)

object LudoCoordinates {
    // 52 tiles forming the outer common circuit (0 to 51)
    val commonTrack: List<GridCoord> = listOf(
        // 0..4: Red start & arm going right
        GridCoord(1, 6), GridCoord(2, 6), GridCoord(3, 6), GridCoord(4, 6), GridCoord(5, 6),
        // 5..10: Top arm going up
        GridCoord(6, 5), GridCoord(6, 4), GridCoord(6, 3), GridCoord(6, 2), GridCoord(6, 1), GridCoord(6, 0),
        // 11..12: Top turn
        GridCoord(7, 0), GridCoord(8, 0),
        // 13..17: Green start & Top arm going down
        GridCoord(8, 1), GridCoord(8, 2), GridCoord(8, 3), GridCoord(8, 4), GridCoord(8, 5),
        // 18..23: Right arm going right
        GridCoord(9, 6), GridCoord(10, 6), GridCoord(11, 6), GridCoord(12, 6), GridCoord(13, 6), GridCoord(14, 6),
        // 24..25: Right turn
        GridCoord(14, 7), GridCoord(14, 8),
        // 26..30: Yellow start & Right arm going left
        GridCoord(13, 8), GridCoord(12, 8), GridCoord(11, 8), GridCoord(10, 8), GridCoord(9, 8),
        // 31..36: Bottom arm going down
        GridCoord(8, 9), GridCoord(8, 10), GridCoord(8, 11), GridCoord(8, 12), GridCoord(8, 13), GridCoord(8, 14),
        // 37..38: Bottom turn
        GridCoord(7, 14), GridCoord(6, 14),
        // 39..43: Blue start & Bottom arm going up
        GridCoord(6, 13), GridCoord(6, 12), GridCoord(6, 11), GridCoord(6, 10), GridCoord(6, 9),
        // 44..49: Left arm going left
        GridCoord(5, 8), GridCoord(4, 8), GridCoord(3, 8), GridCoord(2, 8), GridCoord(1, 8), GridCoord(0, 8),
        // 50..51: Left turn
        GridCoord(0, 7), GridCoord(0, 6)
    )

    // 5 home corridor tiles for each color (steps 51 to 55)
    val redHomeCorridor: List<GridCoord> = listOf(
        GridCoord(1, 7), GridCoord(2, 7), GridCoord(3, 7), GridCoord(4, 7), GridCoord(5, 7)
    )
    val greenHomeCorridor: List<GridCoord> = listOf(
        GridCoord(7, 1), GridCoord(7, 2), GridCoord(7, 3), GridCoord(7, 4), GridCoord(7, 5)
    )
    val yellowHomeCorridor: List<GridCoord> = listOf(
        GridCoord(13, 7), GridCoord(12, 7), GridCoord(11, 7), GridCoord(10, 7), GridCoord(9, 7)
    )
    val blueHomeCorridor: List<GridCoord> = listOf(
        GridCoord(7, 13), GridCoord(7, 12), GridCoord(7, 11), GridCoord(7, 10), GridCoord(7, 9)
    )

    // Center Home Goal coordinate (step 56)
    val centerHomeGoal = GridCoord(7, 7)

    // Safe squares indices on the 52-track
    val startSafeIndices = setOf(0, 13, 26, 39)
    val starSafeIndices = setOf(8, 21, 34, 47)
    val allSafeIndices = startSafeIndices + starSafeIndices

    // Safe coordinates
    val safeGridCoords: Set<GridCoord> = allSafeIndices.map { commonTrack[it] }.toSet()
    val starGridCoords: Set<GridCoord> = starSafeIndices.map { commonTrack[it] }.toSet()
    val startGridCoords: Set<GridCoord> = startSafeIndices.map { commonTrack[it] }.toSet()

    // 4 base parking slot positions (colFloat, rowFloat) for tokens in base
    val baseSlots: Map<LudoColor, List<Pair<Float, Float>>> = mapOf(
        LudoColor.RED to listOf(
            Pair(1.7f, 10.7f), Pair(3.7f, 10.7f),
            Pair(1.7f, 12.7f), Pair(3.7f, 12.7f)
        ),
        LudoColor.GREEN to listOf(
            Pair(1.7f, 1.7f), Pair(3.7f, 1.7f),
            Pair(1.7f, 3.7f), Pair(3.7f, 3.7f)
        ),
        LudoColor.YELLOW to listOf(
            Pair(10.7f, 1.7f), Pair(12.7f, 1.7f),
            Pair(10.7f, 3.7f), Pair(12.7f, 3.7f)
        ),
        LudoColor.BLUE to listOf(
            Pair(10.7f, 10.7f), Pair(12.7f, 10.7f),
            Pair(10.7f, 12.7f), Pair(12.7f, 12.7f)
        )
    )

    // Home goal parking positions for finished tokens
    val homeGoalSlots: Map<LudoColor, List<Pair<Float, Float>>> = mapOf(
        LudoColor.RED to listOf(
            Pair(6.3f, 6.7f), Pair(6.3f, 7.3f),
            Pair(6.7f, 6.9f), Pair(6.7f, 7.1f)
        ),
        LudoColor.GREEN to listOf(
            Pair(6.7f, 6.3f), Pair(7.3f, 6.3f),
            Pair(6.9f, 6.7f), Pair(7.1f, 6.7f)
        ),
        LudoColor.YELLOW to listOf(
            Pair(7.7f, 6.7f), Pair(7.7f, 7.3f),
            Pair(7.3f, 6.9f), Pair(7.3f, 7.1f)
        ),
        LudoColor.BLUE to listOf(
            Pair(6.7f, 7.7f), Pair(7.3f, 7.7f),
            Pair(6.9f, 7.3f), Pair(7.1f, 7.3f)
        )
    )

    /**
     * Converts a player's token relative step (0..56) to board grid coordinate.
     * Step 0: Starting tile right outside base
     * Step 0..50: 51 steps on common track
     * Step 51..55: 5 steps on colored home corridor
     * Step 56: Reached center home
     */
    fun getGridCoordForStep(color: LudoColor, relativeStep: Int): GridCoord {
        if (relativeStep < 0) return GridCoord(-1, -1) // Base
        if (relativeStep in 0..50) {
            val commonIndex = (color.startIndex + relativeStep) % 52
            return commonTrack[commonIndex]
        }
        val corridorIndex = (relativeStep - 51).coerceIn(0, 4)
        return when (color) {
            LudoColor.RED -> redHomeCorridor[corridorIndex]
            LudoColor.GREEN -> greenHomeCorridor[corridorIndex]
            LudoColor.YELLOW -> yellowHomeCorridor[corridorIndex]
            LudoColor.BLUE -> blueHomeCorridor[corridorIndex]
        }
    }

    /**
     * Checks if a relative step is on a safe square.
     */
    fun isSafeStep(color: LudoColor, relativeStep: Int): Boolean {
        if (relativeStep < 0) return true // In base
        if (relativeStep >= 51) return true // In home stretch or home
        val commonIndex = (color.startIndex + relativeStep) % 52
        return allSafeIndices.contains(commonIndex)
    }

    /**
     * Gets common board track index for a token on outer track (0..50).
     * Returns null if token is in base, in home corridor, or home goal.
     */
    fun getCommonTrackIndex(color: LudoColor, relativeStep: Int): Int? {
        if (relativeStep in 0..50) {
            return (color.startIndex + relativeStep) % 52
        }
        return null
    }
}
