package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val startX: Float,
    val speedY: Float,
    val speedX: Float,
    val rotationSpeed: Float,
    val color: Color,
    val width: Float,
    val height: Float
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFFE53935), Color(0xFF43A047), Color(0xFFFDD835),
            Color(0xFF1E88E5), Color(0xFFFF4081), Color(0xFF7C4DFF), Color(0xFF00E676)
        )
        (0 until 70).map {
            ConfettiParticle(
                startX = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.7f + 0.4f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                color = colors.random(),
                width = Random.nextFloat() * 12f + 8f,
                height = Random.nextFloat() * 8f + 5f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val p = progress.value

        for (particle in particles) {
            val curY = (particle.speedY * p * h * 1.2f) % (h + 50f) - 50f
            val curX = (particle.startX * w) + (particle.speedX * p * w)
            val currentRotation = particle.rotationSpeed * p

            rotate(degrees = currentRotation, pivot = Offset(curX, curY)) {
                drawRect(
                    color = particle.color.copy(alpha = (1f - (p * 0.4f)).coerceIn(0f, 1f)),
                    topLeft = Offset(curX, curY),
                    size = Size(particle.width, particle.height)
                )
            }
        }
    }
}
