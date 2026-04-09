package com.goalapp.ui.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalapp.data.GoalEntity
import com.goalapp.data.GoalRepository
import com.goalapp.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddGoalViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {
    
    companion object {
        private const val TAG = "AddGoalViewModel"
    }

    fun saveGoal(
        title: String,
        description: String,
        targetValue: Float,
        unit: String,
        colorHex: String,
        createdAt: Long = System.currentTimeMillis(),
        notificationEnabled: Boolean = false,
        notificationHour: Int? = null,
        notificationMinute: Int? = null,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || targetValue <= 0f) return

        viewModelScope.launch {
            val goalId = repository.insertGoal(
                GoalEntity(
                    title = title.trim(),
                    description = description.trim(),
                    targetValue = targetValue,
                    unit = unit.trim(),
                    colorHex = colorHex,
                    createdAt = createdAt,
                    notificationEnabled = notificationEnabled,
                    notificationHour = notificationHour,
                    notificationMinute = notificationMinute
                )
            )
            
            Log.d(TAG, "Goal saved with ID: $goalId")
            
            // Bildirim ayarlanmışsa schedule et
            if (notificationEnabled && notificationHour != null && notificationMinute != null) {
                Log.d(TAG, "Scheduling notification for goal $goalId")
                val goal = repository.getGoalById(goalId)
                goal?.let { 
                    Log.d(TAG, "Goal retrieved: ${it.title}, createdAt: ${it.createdAt}")
                    notificationScheduler.scheduleNotification(it) 
                }
            }
            
            onSuccess()
        }
    }
}
