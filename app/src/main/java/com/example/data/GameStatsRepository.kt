package com.example.data

import kotlinx.coroutines.flow.Flow

class GameStatsRepository(private val dao: GameStatsDao) {
    val allStats: Flow<List<GameStatEntity>> = dao.getAllStats()
    val recentStats: Flow<List<GameStatEntity>> = dao.getRecentStats()
    val totalGames: Flow<Int> = dao.getTotalGames()
    val totalWins: Flow<Int> = dao.getTotalWins()
    val totalCaptures: Flow<Int?> = dao.getTotalCaptures()
    val totalSixes: Flow<Int?> = dao.getTotalSixes()

    suspend fun recordGame(stat: GameStatEntity): Long {
        return dao.insertStat(stat)
    }

    suspend fun clearStats() {
        dao.clearAllStats()
    }
}
