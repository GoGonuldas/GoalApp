package com.goalapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalapp.data.GoalEntity
import com.goalapp.data.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GoalRepository
) : ViewModel() {

    private val dayBounds = currentDayBounds()

    init {
        viewModelScope.launch {
            repository.archiveGoalsBefore(
                startOfDayMillis = dayBounds.first,
                archivedAtMillis = System.currentTimeMillis()
            )
        }
    }

    val goals: StateFlow<List<GoalEntity>> = repository
        .getActiveGoalsForDay(
            startOfDayMillis = dayBounds.first,
            endOfDayMillis = dayBounds.second
        )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val completedCount: StateFlow<Int> = goals
        .map { list -> list.count { it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch { repository.deleteGoal(goal) }
    }

    private fun currentDayBounds(nowMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zone)
        val start = now.toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
        val end = now.toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }
}
