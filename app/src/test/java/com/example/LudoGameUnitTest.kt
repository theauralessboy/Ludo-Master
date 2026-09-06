package com.example

import com.example.model.GameMode
import com.example.model.GridCoord
import com.example.model.LudoColor
import com.example.model.LudoCoordinates
import com.example.model.Player
import com.example.model.PlayerType
import com.example.model.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LudoGameUnitTest {

    @Test
    fun testStartSquareMapping() {
        assertEquals(GridCoord(1, 6), LudoCoordinates.getGridCoordForStep(LudoColor.RED, 0))
        assertEquals(GridCoord(8, 1), LudoCoordinates.getGridCoordForStep(LudoColor.GREEN, 0))
        assertEquals(GridCoord(13, 8), LudoCoordinates.getGridCoordForStep(LudoColor.YELLOW, 0))
        assertEquals(GridCoord(6, 13), LudoCoordinates.getGridCoordForStep(LudoColor.BLUE, 0))
    }

    @Test
    fun testSafeSquares() {
        // Red start is safe
        assertTrue(LudoCoordinates.isSafeStep(LudoColor.RED, 0))
        // Milestone star (step 8 for Red) is safe
        assertTrue(LudoCoordinates.isSafeStep(LudoColor.RED, 8))
        // Regular track tile is not safe
        assertFalse(LudoCoordinates.isSafeStep(LudoColor.RED, 2))
        assertFalse(LudoCoordinates.isSafeStep(LudoColor.RED, 5))
    }

    @Test
    fun testHomeGoalStep() {
        assertEquals(56, LudoCoordinates.TOTAL_STEPS_TO_HOME)
        val homeCoord = LudoCoordinates.getGridCoordForStep(LudoColor.RED, 56)
        assertEquals(GridCoord(7, 7), homeCoord)
    }

    @Test
    fun testHomeCorridor() {
        // Red home corridor is column 1..5, row 7
        for (step in 51..55) {
            val coord = LudoCoordinates.getGridCoordForStep(LudoColor.RED, step)
            assertEquals(7, coord.row)
            assertTrue(coord.col in 1..5)
        }
    }

    @Test
    fun testPlayerTokenState() {
        val player = Player(
            id = 0,
            color = LudoColor.RED,
            name = "Player 1",
            type = PlayerType.HUMAN,
            isEnabled = true
        )

        assertEquals(4, player.tokens.size)
        assertEquals(4, player.tokensInBaseCount)
        assertEquals(0, player.homeTokensCount)
        assertFalse(player.hasWon(GameMode.CLASSIC))

        val movedTokens = player.tokens.map { it.copy(relativeStep = 56) }
        val wonPlayer = player.copy(tokens = movedTokens)
        assertEquals(4, wonPlayer.homeTokensCount)
        assertTrue(wonPlayer.hasWon(GameMode.CLASSIC))
    }
}
