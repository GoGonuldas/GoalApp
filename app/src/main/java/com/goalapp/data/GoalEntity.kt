package com.goalapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetValue: Float,
    val currentValue: Float = 0f,
    val unit: String = "",
    val colorHex: String = "#6650A4",
    val createdAt: Long = System.currentTimeMillis(),
    val deadline: Long? = null,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
    val notificationEnabled: Boolean = false,
    val notificationHour: Int? = null,  // 0-23
    val notificationMinute: Int? = null  // 0-59
) {
    val progressPercent: Float
        get() = if (targetValue > 0f)
            (currentValue / targetValue * 100f).coerceIn(0f, 100f)
        else 0f

    val isCompleted: Boolean get() = progressPercent >= 100f
}
