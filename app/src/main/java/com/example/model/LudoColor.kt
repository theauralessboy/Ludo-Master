package com.example.model

import androidx.compose.ui.graphics.Color

enum class LudoColor(
    val displayName: String,
    val primaryColor: Color,
    val darkColor: Color,
    val lightColor: Color,
    val containerColor: Color,
    val onColor: Color = Color.White,
    val startIndex: Int // starting step on 52-tile board
) {
    RED(
        displayName = "Red",
        primaryColor = Color(0xFFE53935),
        darkColor = Color(0xFFB71C1C),
        lightColor = Color(0xFFFF8A80),
        containerColor = Color(0xFFFFEBEE),
        onColor = Color.White,
        startIndex = 0
    ),
    GREEN(
        displayName = "Green",
        primaryColor = Color(0xFF2E7D32),
        darkColor = Color(0xFF1B5E20),
        lightColor = Color(0xFF81C784),
        containerColor = Color(0xFFE8F5E9),
        onColor = Color.White,
        startIndex = 13
    ),
    YELLOW(
        displayName = "Yellow",
        primaryColor = Color(0xFFF9A825),
        darkColor = Color(0xFFE65100),
        lightColor = Color(0xFFFFF176),
        containerColor = Color(0xFFFFFDE7),
        onColor = Color(0xFF212121),
        startIndex = 26
    ),
    BLUE(
        displayName = "Blue",
        primaryColor = Color(0xFF1976D2),
        darkColor = Color(0xFF0D47A1),
        lightColor = Color(0xFF64B5F6),
        containerColor = Color(0xFFE3F2FD),
        onColor = Color.White,
        startIndex = 39
    );

    val nextColor: LudoColor
        get() = when (this) {
            RED -> GREEN
            GREEN -> YELLOW
            YELLOW -> BLUE
            BLUE -> RED
        }
}
