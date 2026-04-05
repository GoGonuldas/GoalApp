package com.goalapp.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(private val dao: GoalDao) {

    suspend fun getGoalCount(): Int = dao.getGoalCount()

    suspend fun insertGoals(goals: List<GoalEntity>) = dao.insertGoals(goals)

    fun getArchivedGoals(): Flow<List<GoalEntity>> = dao.getArchivedGoals()

    fun getActiveGoalsForDay(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<GoalEntity>> =
        dao.getActiveGoalsForDay(startOfDayMillis, endOfDayMillis)

    suspend fun archiveGoalsBefore(startOfDayMillis: Long, archivedAtMillis: Long): Int =
        dao.archiveGoalsBefore(startOfDayMillis, archivedAtMillis)

    suspend fun getGoalById(id: Long): GoalEntity? = dao.getGoalById(id)

    suspend fun insertGoal(goal: GoalEntity): Long = dao.insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)

    suspend fun updateProgress(id: Long, value: Float) = dao.updateProgress(id, value)

    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)
}
