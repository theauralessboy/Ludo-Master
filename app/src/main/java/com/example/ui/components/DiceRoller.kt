package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiceState
import com.example.model.GameState
import com.example.model.LudoColor
import com.example.model.Player

@Composable
fun DiceControlConsole(
    gameState: GameState,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentP = gameState.currentPlayer ?: return
    val diceNumber = if (gameState.diceState == DiceState.ROLLING) {
        gameState.diceAnimNumber
    } else {
        gameState.diceValue
    }
    val isRolling = gameState.diceState == DiceState.ROLLING
    val canRoll = gameState.diceState == DiceState.WAITING_ROLL && !currentP.isBot && !gameState.isGameOver

    val infiniteTransition = rememberInfiniteTransition(label = "DiceGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAnim"
    )

    // Shaking rotation animation during rolling
    val rotationAnim = remember { Animatable(0f) }
    LaunchedEffect(isRolling) {
        if (isRolling) {
            rotationAnim.animateTo(
                targetValue = 360f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
            rotationAnim.snapTo(0f)
        }
    }

    Card(
        modifier = modifier
            .shadow(8.dp, shape = RoundedCornerShape(20.dp))
            .testTag("dice_console"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = currentP.color.containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player Profile badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(currentP.color.primaryColor)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentP.isBot) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Bot",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = currentP.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentP.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1E293B)
                        )
                        if (currentP.isBot) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = currentP.color.primaryColor.copy(alpha = 0.2f),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "AI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentP.color.darkColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    val statusText = when (gameState.diceState) {
                        DiceState.WAITING_ROLL -> if (currentP.isBot) "AI thinking..." else "Tap to roll dice"
                        DiceState.ROLLING -> "Rolling..."
                        DiceState.ROLLED_CHOICE -> "Select a token to move"
                        DiceState.ANIMATING_MOVE -> "Moving token..."
                        DiceState.TURN_TRANSITION -> "Passing turn..."
                        DiceState.GAME_OVER -> "Match Finished!"
                    }

                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Interactive Dice Face Box
            Box(
                modifier = Modifier
                    .scale(if (canRoll) glowScale else 1f)
                    .rotate(rotationAnim.value)
                    .clickable(
                        enabled = canRoll,
                        onClick = onRollClick
                    )
                    .testTag("roll_dice_button"),
                contentAlignment = Alignment.Center
            ) {
                // Outer Dice Shadow
                DiceFace(
                    value = diceNumber,
                    playerColor = currentP.color,
                    isRolling = isRolling,
                    canRoll = canRoll
                )
            }
        }
    }
}

@Composable
fun DiceFace(
    value: Int,
    playerColor: LudoColor,
    isRolling: Boolean,
    canRoll: Boolean,
    modifier: Modifier = Modifier
) {
    val size = 58.dp
    val dotSize = 10.dp

    Box(
        modifier = modifier
            .size(size)
            .shadow(6.dp, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF1F5F9)
                    )
                )
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val dotColor = playerColor.primaryColor

        when (value) {
            1 -> {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            2 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Start) {
                        DiceDot(dotSize, dotColor)
                    }
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                        DiceDot(dotSize, dotColor)
                    }
                }
            }
            3 -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.align(Alignment.TopStart)) { DiceDot(dotSize, dotColor) }
                    Box(modifier = Modifier.align(Alignment.Center)) { DiceDot(dotSize, dotColor) }
                    Box(modifier = Modifier.align(Alignment.BottomEnd)) { DiceDot(dotSize, dotColor) }
                }
            }
            4 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DiceDot(dotSize, dotColor)
                        DiceDot(dotSize, dotColor)
                    }
                    Row(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DiceDot(dotSize, dotColor)
                        DiceDot(dotSize, dotColor)
                    }
                }
            }
            5 -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.align(Alignment.TopStart)) { DiceDot(dotSize, dotColor) }
                    Box(modifier = Modifier.align(Alignment.TopEnd)) { DiceDot(dotSize, dotColor) }
                    Box(modifier = Modifier.align(Alignment.Center)) { DiceDot(dotSize, dotColor) }
                    Box(modifier = Modifier.align(Alignment.BottomStart)) { DiceDot(dotSize, dotColor) }
                    Box(modifier = Modifier.align(Alignment.BottomEnd)) { DiceDot(dotSize, dotColor) }
                }
            }
            6 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DiceDot(dotSize, dotColor)
                        DiceDot(dotSize, dotColor)
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DiceDot(dotSize, dotColor)
                        DiceDot(dotSize, dotColor)
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DiceDot(dotSize, dotColor)
                        DiceDot(dotSize, dotColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiceDot(size: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .shadow(1.dp, CircleShape)
    )
}
