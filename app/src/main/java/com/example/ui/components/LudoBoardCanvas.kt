package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameState
import com.example.model.GridCoord
import com.example.model.LudoColor
import com.example.model.LudoCoordinates
import com.example.model.LudoTheme
import com.example.model.Player
import com.example.model.Token
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LudoBoard(
    gameState: GameState,
    onTokenClick: (Token) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = gameState.theme
    val infiniteTransition = rememberInfiniteTransition(label = "BoardPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TokenPulse"
    )

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp))
            .testTag("ludo_board"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = theme.frameBorderColor)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(theme.boardBackground)
        ) {
            val boardSize = maxWidth
            val cellSize = boardSize / 15f

            // 1. Draw static Board background, bases, paths, safe stars, and home triangles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLudoBoardCanvas(size, theme, gameState.currentPlayer?.color)
            }

            // 2. Overlay Tokens accurately positioned
            TokensOverlay(
                gameState = gameState,
                cellSize = cellSize,
                pulseScale = pulseScale,
                onTokenClick = onTokenClick
            )
        }
    }
}

private fun DrawScope.drawLudoBoardCanvas(
    canvasSize: Size,
    theme: LudoTheme,
    activeColor: LudoColor?
) {
    val w = canvasSize.width
    val cellSize = w / 15f
    val baseBlockSize = cellSize * 6f

    // 1. Draw 4 Corner Bases
    // Top-Left: Green
    drawLudoBase(
        topLeft = Offset(0f, 0f),
        size = baseBlockSize,
        color = LudoColor.GREEN,
        isActive = activeColor == LudoColor.GREEN
    )

    // Top-Right: Yellow
    drawLudoBase(
        topLeft = Offset(w - baseBlockSize, 0f),
        size = baseBlockSize,
        color = LudoColor.YELLOW,
        isActive = activeColor == LudoColor.YELLOW
    )

    // Bottom-Left: Red
    drawLudoBase(
        topLeft = Offset(0f, w - baseBlockSize),
        size = baseBlockSize,
        color = LudoColor.RED,
        isActive = activeColor == LudoColor.RED
    )

    // Bottom-Right: Blue
    drawLudoBase(
        topLeft = Offset(w - baseBlockSize, w - baseBlockSize),
        size = baseBlockSize,
        color = LudoColor.BLUE,
        isActive = activeColor == LudoColor.BLUE
    )

    // 2. Draw 15x15 Track Cells
    val strokeWidth = 1.2f
    for (col in 0 until 15) {
        for (row in 0 until 15) {
            // Check if cell is part of the 4 bases or center triangle
            val isInBase = (col < 6 && row < 6) || (col >= 9 && row < 6) ||
                    (col < 6 && row >= 9) || (col >= 9 && row >= 9)
            val isInCenter = (col in 6..8 && row in 6..8)

            if (!isInBase && !isInCenter) {
                val cellLeft = col * cellSize
                val cellTop = row * cellSize
                val coord = GridCoord(col, row)

                // Cell fill
                var fillColor = theme.cellFillColor

                // Check starting squares
                if (coord == GridCoord(1, 6)) fillColor = LudoColor.RED.primaryColor
                if (coord == GridCoord(8, 1)) fillColor = LudoColor.GREEN.primaryColor
                if (coord == GridCoord(13, 8)) fillColor = LudoColor.YELLOW.primaryColor
                if (coord == GridCoord(6, 13)) fillColor = LudoColor.BLUE.primaryColor

                // Check Home Corridors
                if (row == 7 && col in 1..5) fillColor = LudoColor.RED.lightColor.copy(alpha = 0.85f)
                if (col == 7 && row in 1..5) fillColor = LudoColor.GREEN.lightColor.copy(alpha = 0.85f)
                if (row == 7 && col in 9..13) fillColor = LudoColor.YELLOW.lightColor.copy(alpha = 0.85f)
                if (col == 7 && row in 9..13) fillColor = LudoColor.BLUE.lightColor.copy(alpha = 0.85f)

                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(cellLeft + 1f, cellTop + 1f),
                    size = Size(cellSize - 2f, cellSize - 2f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Grid cell border
                drawRoundRect(
                    color = theme.gridBorderColor,
                    topLeft = Offset(cellLeft, cellTop),
                    size = Size(cellSize, cellSize),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = strokeWidth)
                )

                // Draw Stars on Safe Milestone Tiles
                if (LudoCoordinates.starGridCoords.contains(coord)) {
                    drawStarShape(
                        center = Offset(cellLeft + cellSize / 2f, cellTop + cellSize / 2f),
                        radius = cellSize * 0.32f,
                        color = theme.starColor
                    )
                }

                // Draw Starting Arrows on start squares
                if (LudoCoordinates.startGridCoords.contains(coord)) {
                    drawStarShape(
                        center = Offset(cellLeft + cellSize / 2f, cellTop + cellSize / 2f),
                        radius = cellSize * 0.28f,
                        color = Color.White
                    )
                }
            }
        }
    }

    // 3. Draw Center Home Goal (3x3 area)
    val centerLeft = 6f * cellSize
    val centerTop = 6f * cellSize
    val centerSize = 3f * cellSize
    val centerPoint = Offset(centerLeft + centerSize / 2f, centerTop + centerSize / 2f)

    // Red Left Triangle
    val redPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerPoint.x, centerPoint.y)
        lineTo(centerLeft, centerTop + centerSize)
        close()
    }
    drawPath(redPath, LudoColor.RED.primaryColor)

    // Green Top Triangle
    val greenPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerPoint.x, centerPoint.y)
        lineTo(centerLeft + centerSize, centerTop)
        close()
    }
    drawPath(greenPath, LudoColor.GREEN.primaryColor)

    // Yellow Right Triangle
    val yellowPath = Path().apply {
        moveTo(centerLeft + centerSize, centerTop)
        lineTo(centerPoint.x, centerPoint.y)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        close()
    }
    drawPath(yellowPath, LudoColor.YELLOW.primaryColor)

    // Blue Bottom Triangle
    val bluePath = Path().apply {
        moveTo(centerLeft, centerTop + centerSize)
        lineTo(centerPoint.x, centerPoint.y)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        close()
    }
    drawPath(bluePath, LudoColor.BLUE.primaryColor)

    // Center Gold Crown Circle
    drawCircle(
        color = Color(0xFFFFD54F),
        radius = cellSize * 0.55f,
        center = centerPoint
    )
    drawCircle(
        color = Color(0xFFFFA000),
        radius = cellSize * 0.55f,
        center = centerPoint,
        style = Stroke(width = 2.5f)
    )
    drawStarShape(
        center = centerPoint,
        radius = cellSize * 0.32f,
        color = Color(0xFF5D4037)
    )
}

private fun DrawScope.drawLudoBase(
    topLeft: Offset,
    size: Float,
    color: LudoColor,
    isActive: Boolean
) {
    // Outer colored base container
    drawRoundRect(
        color = color.primaryColor,
        topLeft = topLeft,
        size = Size(size, size),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // Active glow indicator if current turn
    if (isActive) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = topLeft,
            size = Size(size, size),
            cornerRadius = CornerRadius(16f, 16f),
            style = Stroke(width = 4f)
        )
    }

    // Inner White circular pad
    val innerPadMargin = size * 0.15f
    val innerPadSize = size - (innerPadMargin * 2f)
    drawRoundRect(
        color = color.containerColor,
        topLeft = Offset(topLeft.x + innerPadMargin, topLeft.y + innerPadMargin),
        size = Size(innerPadSize, innerPadSize),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // 4 circular token sockets
    val socketRadius = size * 0.12f
    val offset1 = size * 0.32f
    val offset2 = size * 0.68f

    val socketPositions = listOf(
        Offset(topLeft.x + offset1, topLeft.y + offset1),
        Offset(topLeft.x + offset2, topLeft.y + offset1),
        Offset(topLeft.x + offset1, topLeft.y + offset2),
        Offset(topLeft.x + offset2, topLeft.y + offset2)
    )

    for (pos in socketPositions) {
        drawCircle(
            color = color.primaryColor.copy(alpha = 0.25f),
            radius = socketRadius,
            center = pos
        )
        drawCircle(
            color = color.primaryColor,
            radius = socketRadius,
            center = pos,
            style = Stroke(width = 2.5f)
        )
    }
}

private fun DrawScope.drawStarShape(
    center: Offset,
    radius: Float,
    color: Color,
    points: Int = 5
) {
    val innerRadius = radius * 0.45f
    val path = Path()
    val step = PI / points

    for (i in 0 until (points * 2)) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * step - (PI / 2.0)
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Fill)
}

@Composable
private fun TokensOverlay(
    gameState: GameState,
    cellSize: Dp,
    pulseScale: Float,
    onTokenClick: (Token) -> Unit
) {
    // Group tokens by their board location (or base / home)
    val allTokens = gameState.players.flatMap { it.tokens }

    // Map each token to its (x, y) Dp offset
    for (player in gameState.players) {
        if (!player.isEnabled) continue
        for (token in player.tokens) {
            val (xOffset, yOffset) = calculateTokenOffset(token, player, cellSize, gameState)
            val isMovable = token.isMovable && gameState.diceState == com.example.model.DiceState.ROLLED_CHOICE

            TokenPawn(
                token = token,
                isMovable = isMovable,
                pulseScale = if (isMovable) pulseScale else 1f,
                cellSize = cellSize,
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .clickable(
                        enabled = isMovable,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onTokenClick(token)
                    }
                    .testTag("token_${token.color.name.lowercase()}_${token.id}")
            )
        }
    }
}

private fun calculateTokenOffset(
    token: Token,
    player: Player,
    cellSize: Dp,
    gameState: GameState
): Pair<Dp, Dp> {
    val baseSlots = LudoCoordinates.baseSlots[token.color] ?: emptyList()
    val homeGoalSlots = LudoCoordinates.homeGoalSlots[token.color] ?: emptyList()

    if (token.isInBase) {
        val slot = baseSlots.getOrNull(token.id) ?: Pair(0f, 0f)
        return Pair(cellSize * slot.first, cellSize * slot.second)
    }

    if (token.isReachedHome) {
        val slot = homeGoalSlots.getOrNull(token.id) ?: Pair(7f, 7f)
        return Pair(cellSize * slot.first, cellSize * slot.second)
    }

    val coord = LudoCoordinates.getGridCoordForStep(token.color, token.relativeStep)

    // Token stacking offset: if multiple tokens are on the same cell, offset them slightly
    val otherTokensOnSameCell = gameState.players.flatMap { it.tokens }
        .filter { it != token && !it.isInBase && !it.isReachedHome }
        .filter {
            val c = LudoCoordinates.getGridCoordForStep(it.color, it.relativeStep)
            c == coord
        }

    val stackIndex = otherTokensOnSameCell.count { it.color.ordinal < token.color.ordinal || (it.color == token.color && it.id < token.id) }
    val shiftX = when (stackIndex) {
        1 -> (cellSize * 0.15f)
        2 -> (-cellSize * 0.15f)
        3 -> (cellSize * 0.15f)
        else -> 0.dp
    }
    val shiftY = when (stackIndex) {
        1 -> (-cellSize * 0.15f)
        2 -> (cellSize * 0.15f)
        3 -> (cellSize * 0.15f)
        else -> 0.dp
    }

    return Pair(
        (cellSize * coord.col) + (cellSize * 0.1f) + shiftX,
        (cellSize * coord.row) + (cellSize * 0.1f) + shiftY
    )
}

@Composable
fun TokenPawn(
    token: Token,
    isMovable: Boolean,
    pulseScale: Float,
    cellSize: Dp,
    modifier: Modifier = Modifier
) {
    val pawnSize = cellSize * 0.8f
    val color = token.color

    Box(
        modifier = modifier
            .size(pawnSize)
            .scale(if (isMovable) pulseScale else 1f),
        contentAlignment = Alignment.Center
    ) {
        // Glowing movable halo
        if (isMovable) {
            Box(
                modifier = Modifier
                    .size(pawnSize * 1.25f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f))
            )
        }

        // Drop shadow circle
        Box(
            modifier = Modifier
                .size(pawnSize * 0.9f)
                .offset(y = 2.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
        )

        // Main 3D Pawn Body
        Box(
            modifier = Modifier
                .size(pawnSize * 0.88f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.lightColor,
                            color.primaryColor,
                            color.darkColor
                        ),
                        center = Offset(0.3f, 0.3f),
                        radius = 40f
                    )
                )
                .shadow(elevation = 4.dp, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner glossy dome top
            Box(
                modifier = Modifier
                    .size(pawnSize * 0.46f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                color.lightColor,
                                color.primaryColor
                            )
                        )
                    )
            )

            // Center Token ID or Star
            if (token.isReachedHome) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Home",
                    tint = Color.White,
                    modifier = Modifier.size(pawnSize * 0.4f)
                )
            } else {
                Text(
                    text = "${token.id + 1}",
                    color = Color.White,
                    fontSize = (pawnSize.value * 0.35f).sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
