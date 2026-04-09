package com.goalapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.goalapp.MainActivity
import com.goalapp.R
import com.goalapp.data.GoalRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class GoalNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: GoalRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "goal_notifications"
        const val CHANNEL_NAME = "Hedef Hatırlatıcılar"
        const val GOAL_ID_KEY = "goal_id"
        private const val TAG = "GoalNotificationWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork() started")
        
        val goalId = inputData.getLong(GOAL_ID_KEY, -1L)
        Log.d(TAG, "Goal ID: $goalId")
        
        if (goalId == -1L) {
            Log.e(TAG, "Invalid goal ID")
            return Result.failure()
        }

        val goal = repository.getGoalById(goalId)
        if (goal == null) {
            Log.e(TAG, "Goal not found for ID: $goalId")
            return Result.failure()
        }
        
        Log.d(TAG, "Goal found: ${goal.title}")
        
        createNotificationChannel()
        showNotification(goal.id, goal.title, goal.description)

        Log.d(TAG, "doWork() completed successfully")
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Hedef hatırlatmaları için bildirimler"
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun showNotification(goalId: Long, title: String, description: String) {
        Log.d(TAG, "showNotification() called for: $title")
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            goalId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🎯 $title")
            .setContentText(description.ifBlank { "Hedefinizi tamamlamayı unutmayın!" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        Log.d(TAG, "Has notification permission: $hasPermission")
        
        if (hasPermission) {
            NotificationManagerCompat.from(applicationContext)
                .notify(goalId.toInt(), notification)
            Log.d(TAG, "Notification shown successfully")
        } else {
            Log.e(TAG, "No notification permission!")
        }
    }
}

