package com.goalapp.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalapp.data.GoalEntity
import com.goalapp.data.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: GoalRepository
) : ViewModel() {

    private val allArchivedGoals: StateFlow<List<GoalEntity>> = repository
        .getArchivedGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val availableEpochDays: StateFlow<List<Long>> = allArchivedGoals
        .map { goals ->
            goals
                .map { it.createdAt.toEpochDay() }
                .distinct()
                .sortedDescending()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _selectedEpochDay = MutableStateFlow<Long?>(null)
    val selectedEpochDay: StateFlow<Long?> = _selectedEpochDay.asStateFlow()

    val archivedGoals: StateFlow<List<GoalEntity>> = combine(allArchivedGoals, _selectedEpochDay) { goals, selected ->
        if (selected == null) emptyList()
        else goals.filter { it.createdAt.toEpochDay() == selected }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        seedArchiveSampleDataIfNeeded()

        viewModelScope.launch {
            availableEpochDays.collect { days ->
                if (days.isNotEmpty() && _selectedEpochDay.value !in days) {
                    _selectedEpochDay.value = days.first()
                }
            }
        }
    }

    fun selectDay(epochDay: Long) {
        _selectedEpochDay.value = epochDay
    }

    private fun Long.toEpochDay(): Long {
        val zone = ZoneId.systemDefault()
        return Instant.ofEpochMilli(this)
            .atZone(zone)
            .toLocalDate()
            .toEpochDay()
    }

    private fun seedArchiveSampleDataIfNeeded() {
        viewModelScope.launch {
            if (repository.getGoalCount() > 0) return@launch
            repository.insertGoals(buildArchiveSampleGoals())
        }
    }

    private fun buildArchiveSampleGoals(): List<GoalEntity> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        fun atMillis(day: LocalDate, hour: Int, minute: Int): Long =
            day.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

        fun goal(
            title: String,
            target: Float,
            current: Float,
            unit: String,
            color: String,
            createdDayOffset: Long,
            createdHour: Int,
            createdMinute: Int
        ): GoalEntity {
            val createdDay = today.minusDays(createdDayOffset)
            return GoalEntity(
                title = title,
                description = "Ornek arsiv verisi",
                targetValue = target,
                currentValue = current,
                unit = unit,
                colorHex = color,
                createdAt = atMillis(createdDay, createdHour, createdMinute),
                isArchived = true,
                archivedAt = atMillis(createdDay.plusDays(1), 0, 1)
            )
        }

        return listOf(
            // 1 gun once (4 Nisan)
            goal("10.000 adim", 10000f, 8600f, "adim", "#006874", 1, 8, 10),
            goal("30 sayfa kitap", 30f, 30f, "sayfa", "#6650A4", 1, 9, 45),
            goal("2 litre su", 2f, 1.6f, "L", "#1B6299", 1, 11, 20),

            // 2 gun once (3 Nisan)
            goal("45 dk spor", 45f, 35f, "dk", "#B5264C", 2, 7, 50),
            goal("Meditasyon", 20f, 20f, "dk", "#006E2C", 2, 21, 5),

            // 3 gun once (2 Nisan)
            goal("8 km yuruyus", 8f, 6.2f, "km", "#7D5700", 3, 18, 30),
            goal("Erken yat", 1f, 1f, "gun", "#6650A4", 3, 23, 15),

            // 4 gun once (1 Nisan)
            goal("60 dk odak calisma", 60f, 52f, "dk", "#006874", 4, 10, 0),
            goal("Gunluk yazma", 1f, 1f, "gun", "#1B6299", 4, 22, 10),

            // 5 gun once (31 Mart)
            goal("Protein hedefi", 120f, 95f, "gr", "#B5264C", 5, 20, 40),
            goal("15 dk esneme", 15f, 12f, "dk", "#006E2C", 5, 7, 25),

            // 6 gun once (30 Mart)
            goal("5 km kosu", 5f, 5f, "km", "#7D5700", 6, 6, 55),
            goal("Ekran suresi azaltilsin", 3f, 2.2f, "saat", "#6650A4", 6, 23, 0),

            // 7 gun once (29 Mart)
            goal("100 kelime ingilizce", 100f, 80f, "kelime", "#006874", 7, 19, 35),
            goal("20 sayfa teknik okuma", 20f, 20f, "sayfa", "#1B6299", 7, 21, 10),

            // 8 gun once (28 Mart)
            goal("Yeni beceri ogren", 1f, 1f, "gun", "#006E2C", 8, 15, 0),
            goal("Sebze tuketimi", 5f, 4f, "porsiyon", "#7D5700", 8, 19, 20),

            // 9 gun once (27 Mart)
            goal("Yoga", 30f, 25f, "dk", "#B5264C", 9, 6, 30),
            goal("Podcast dinle", 1f, 1f, "bolum", "#006874", 9, 14, 45),

            // 10 gun once (26 Mart)
            goal("Aile ile vakit", 2f, 2f, "saat", "#6650A4", 10, 18, 0),
            goal("Gunluk rutin", 1f, 1f, "gun", "#1B6299", 10, 22, 30)
        )
    }
}

