package com.goalapp.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalapp.data.GoalEntity
import com.goalapp.data.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddGoalViewModel @Inject constructor(
    private val repository: GoalRepository
) : ViewModel() {

    fun saveGoal(
        title: String,
        description: String,
        targetValue: Float,
        unit: String,
        colorHex: String,
        createdAt: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || targetValue <= 0f) return

        viewModelScope.launch {
            repository.insertGoal(
                GoalEntity(
                    title = title.trim(),
                    description = description.trim(),
                    targetValue = targetValue,
                    unit = unit.trim(),
                    colorHex = colorHex,
                    createdAt = createdAt
                )
            )
            onSuccess()
        }
    }
}
