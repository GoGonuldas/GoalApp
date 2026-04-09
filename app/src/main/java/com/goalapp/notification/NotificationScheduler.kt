package com.goalapp.notification

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.goalapp.data.GoalEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val TAG = "NotificationScheduler"
    }

    fun scheduleNotification(goal: GoalEntity) {
        Log.d(TAG, "scheduleNotification called for goal: ${goal.id}, title: ${goal.title}")
        Log.d(TAG, "notificationEnabled: ${goal.notificationEnabled}, hour: ${goal.notificationHour}, minute: ${goal.notificationMinute}")
        
        if (!goal.notificationEnabled || goal.notificationHour == null || goal.notificationMinute == null) {
            Log.d(TAG, "Notification disabled or time not set, canceling")
            cancelNotification(goal.id)
            return
        }

        val notificationTime = calculateNotificationTime(
            goal.createdAt,
            goal.notificationHour,
            goal.notificationMinute
        )

        val currentTime = System.currentTimeMillis()
        val delay = notificationTime - currentTime
        
        Log.d(TAG, "Current time: $currentTime")
        Log.d(TAG, "Notification time: $notificationTime")
        Log.d(TAG, "Delay: $delay ms (${delay / 1000} seconds)")

        if (delay <= 0) {
            Log.d(TAG, "Notification time is in the past, canceling")
            cancelNotification(goal.id)
            return
        }

        val inputData = Data.Builder()
            .putLong(GoalNotificationWorker.GOAL_ID_KEY, goal.id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<GoalNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("goal_notification_${goal.id}")
            .build()

        workManager.enqueueUniqueWork(
            "goal_notification_${goal.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        Log.d(TAG, "Notification scheduled successfully for goal ${goal.id} with delay ${delay / 1000} seconds")
    }

    fun cancelNotification(goalId: Long) {
        Log.d(TAG, "Canceling notification for goal $goalId")
        workManager.cancelUniqueWork("goal_notification_$goalId")
    }

    private fun calculateNotificationTime(
        goalDateMillis: Long,
        hour: Int,
        minute: Int
    ): Long {
        val zoneId = ZoneId.systemDefault()
        val instant = java.time.Instant.ofEpochMilli(goalDateMillis)
        val goalDate = instant.atZone(zoneId).toLocalDate()
        
        Log.d(TAG, "Goal date: $goalDate, hour: $hour, minute: $minute")
        
        val notificationDateTime = LocalDateTime.of(
            goalDate.year,
            goalDate.month,
            goalDate.dayOfMonth,
            hour,
            minute,
            0
        )

        return notificationDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }
}


