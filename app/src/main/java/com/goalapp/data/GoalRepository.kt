package com.goalapp.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(private val dao: GoalDao) {

    fun getAllGoals(): Flow<List<GoalEntity>> = dao.getAllGoals()

    suspend fun getGoalById(id: Long): GoalEntity? = dao.getGoalById(id)

    suspend fun insertGoal(goal: GoalEntity): Long = dao.insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)

    suspend fun updateProgress(id: Long, value: Float) = dao.updateProgress(id, value)

    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)
}
