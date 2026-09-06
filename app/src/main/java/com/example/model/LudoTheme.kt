package com.example.model

import androidx.compose.ui.graphics.Color

enum class LudoTheme(
    val title: String,
    val boardBackground: Color,
    val gridBorderColor: Color,
    val cellFillColor: Color,
    val frameBorderColor: Color,
    val starColor: Color,
    val arrowColor: Color,
    val isDark: Boolean
) {
    CLASSIC(
        title = "Classic Board",
        boardBackground = Color(0xFFFAF9F6),
        gridBorderColor = Color(0xFF2C3E50).copy(alpha = 0.25f),
        cellFillColor = Color(0xFFFFFFFF),
        frameBorderColor = Color(0xFF4E342E),
        starColor = Color(0xFFFFB300),
        arrowColor = Color(0xFF78909C),
        isDark = false
    ),
    ROYAL_NAVY(
        title = "Royal Velvet",
        boardBackground = Color(0xFF0F172A),
        gridBorderColor = Color(0xFF334155),
        cellFillColor = Color(0xFF1E293B),
        frameBorderColor = Color(0xFFD97706),
        starColor = Color(0xFFFBBF24),
        arrowColor = Color(0xFF94A3B8),
        isDark = true
    ),
    WOOD_CRAFT(
        title = "Artisan Wood",
        boardBackground = Color(0xFFD7CCC8),
        gridBorderColor = Color(0xFF5D4037).copy(alpha = 0.4f),
        cellFillColor = Color(0xFFEFEBE9),
        frameBorderColor = Color(0xFF3E2723),
        starColor = Color(0xFFEF6C00),
        arrowColor = Color(0xFF8D6E63),
        isDark = false
    ),
    NEON_CYBER(
        title = "Neon Glow",
        boardBackground = Color(0xFF090A0F),
        gridBorderColor = Color(0xFF1E293B),
        cellFillColor = Color(0xFF111827),
        frameBorderColor = Color(0xFF06B6D4),
        starColor = Color(0xFFF43F5E),
        arrowColor = Color(0xFF22D3EE),
        isDark = true
    )
}
