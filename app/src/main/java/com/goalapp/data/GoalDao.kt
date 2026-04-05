package com.goalapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun getGoalCount(): Int

    @Query(
        "SELECT * FROM goals " +
            "WHERE isArchived = 0 AND createdAt >= :startOfDayMillis AND createdAt < :endOfDayMillis " +
            "ORDER BY createdAt DESC"
    )
    fun getActiveGoalsForDay(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<GoalEntity>>

    @Query("UPDATE goals SET isArchived = 1, archivedAt = :archivedAtMillis WHERE isArchived = 0 AND createdAt < :startOfDayMillis")
    suspend fun archiveGoalsBefore(startOfDayMillis: Long, archivedAtMillis: Long): Int

    @Query("SELECT * FROM goals WHERE isArchived = 1 ORDER BY archivedAt DESC, createdAt DESC")
    fun getArchivedGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("UPDATE goals SET currentValue = :value WHERE id = :id")
    suspend fun updateProgress(id: Long, value: Float)
}
