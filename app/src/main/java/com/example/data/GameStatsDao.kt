package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatsDao {
    @Query("SELECT * FROM game_stats ORDER BY timestamp DESC")
    fun getAllStats(): Flow<List<GameStatEntity>>

    @Query("SELECT * FROM game_stats ORDER BY timestamp DESC LIMIT 10")
    fun getRecentStats(): Flow<List<GameStatEntity>>

    @Query("SELECT COUNT(*) FROM game_stats")
    fun getTotalGames(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_stats WHERE isHumanWinner = 1")
    fun getTotalWins(): Flow<Int>

    @Query("SELECT SUM(totalCaptures) FROM game_stats")
    fun getTotalCaptures(): Flow<Int?>

    @Query("SELECT SUM(totalSixes) FROM game_stats")
    fun getTotalSixes(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStat(stat: GameStatEntity): Long

    @Query("DELETE FROM game_stats")
    suspend fun clearAllStats()
}
