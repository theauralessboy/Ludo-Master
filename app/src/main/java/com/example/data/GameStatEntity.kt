package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_stats")
data class GameStatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameMode: String, // Classic, Quick, Rush
    val playerCount: Int,
    val winnerColor: String,
    val winnerName: String,
    val isHumanWinner: Boolean,
    val durationSeconds: Long,
    val totalTurns: Int,
    val totalCaptures: Int,
    val totalSixes: Int,
    val userRank: Int // 1st, 2nd, 3rd, 4th
)
