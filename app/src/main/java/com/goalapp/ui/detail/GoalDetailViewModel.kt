package com.goalapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalapp.data.GoalEntity
import com.goalapp.data.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val repository: GoalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val goalId: Long = checkNotNull(savedStateHandle["goalId"])

    private val _goal = MutableStateFlow<GoalEntity?>(null)
    val goal: StateFlow<GoalEntity?> = _goal.asStateFlow()

    init {
        viewModelScope.launch {
            _goal.value = repository.getGoalById(goalId)
        }
    }

    fun updateProgress(newValue: Float, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateProgress(goalId, newValue)
            _goal.value = repository.getGoalById(goalId)
            onSaved()
        }
    }

    fun deleteGoal(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _goal.value?.let { repository.deleteGoal(it) }
            onDeleted()
        }
    }
}
