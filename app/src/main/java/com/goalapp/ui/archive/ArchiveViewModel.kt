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
            // 1 gün önce
            goal("10.000 adım", 10000f, 8600f, "adım", "#006874", 1, 8, 10),
            goal("30 sayfa kitap", 30f, 30f, "sayfa", "#6650A4", 1, 9, 45),
            goal("2 litre su", 2f, 1.6f, "L", "#1B6299", 1, 11, 20),

            // 2 gün önce
            goal("45 dk spor", 45f, 35f, "dk", "#B5264C", 2, 7, 50),
            goal("Meditasyon", 20f, 20f, "dk", "#006E2C", 2, 21, 5),
            goal("Sabah koşusu", 5f, 5f, "km", "#7D5700", 2, 6, 30),

            // 3 gün önce
            goal("8 km yürüyüş", 8f, 6.2f, "km", "#7D5700", 3, 18, 30),
            goal("Erken yat", 1f, 1f, "gün", "#6650A4", 3, 23, 15),
            goal("Kitap oku", 50f, 42f, "sayfa", "#1B6299", 3, 20, 0),

            // 4 gün önce
            goal("60 dk odak çalışma", 60f, 52f, "dk", "#006874", 4, 10, 0),
            goal("Günlük yazma", 1f, 1f, "gün", "#1B6299", 4, 22, 10),
            goal("3 öğün düzenli", 3f, 3f, "öğün", "#006E2C", 4, 19, 30),

            // 5 gün önce
            goal("Protein hedefi", 120f, 95f, "gr", "#B5264C", 5, 20, 40),
            goal("15 dk esneme", 15f, 12f, "dk", "#006E2C", 5, 7, 25),
            goal("İngilizce pratik", 30f, 25f, "dk", "#6650A4", 5, 16, 0),

            // 6 gün önce
            goal("5 km koşu", 5f, 5f, "km", "#7D5700", 6, 6, 55),
            goal("Ekran süresi azaltılsın", 3f, 2.2f, "saat", "#6650A4", 6, 23, 0),
            goal("Su içme", 2.5f, 2f, "L", "#1B6299", 6, 15, 20),

            // 7 gün önce
            goal("100 kelime ingilizce", 100f, 80f, "kelime", "#006874", 7, 19, 35),
            goal("20 sayfa teknik okuma", 20f, 20f, "sayfa", "#1B6299", 7, 21, 10),
            goal("Vitamin al", 1f, 1f, "gün", "#006E2C", 7, 8, 0),

            // 8 gün önce
            goal("Yeni beceri öğren", 1f, 1f, "gün", "#006E2C", 8, 15, 0),
            goal("Sebze tüketimi", 5f, 4f, "porsiyon", "#7D5700", 8, 19, 20),
            goal("Mola ver", 8f, 6f, "kez", "#B5264C", 8, 14, 30),

            // 9 gün önce
            goal("Yoga", 30f, 25f, "dk", "#B5264C", 9, 6, 30),
            goal("Podcast dinle", 1f, 1f, "bölüm", "#006874", 9, 14, 45),
            goal("Temizlik yap", 1f, 0.8f, "saat", "#7D5700", 9, 10, 0),

            // 10 gün önce
            goal("Aile ile vakit", 2f, 2f, "saat", "#6650A4", 10, 18, 0),
            goal("Müzik dinle", 45f, 45f, "dk", "#006874", 10, 20, 15),

            // 11 gün önce
            goal("Sağlıklı kahvaltı", 1f, 1f, "gün", "#006E2C", 11, 7, 30),
            goal("Telefon kullanımı azalt", 2f, 1.5f, "saat", "#B5264C", 11, 22, 0),

            // 12 gün önce  
            goal("Proje ilerlet", 3f, 2.5f, "saat", "#1B6299", 12, 14, 0),
            goal("Nefes egzersizi", 10f, 10f, "dk", "#006E2C", 12, 12, 0),

            // 13 gün önce
            goal("Arkadaşla görüş", 1f, 1f, "gün", "#6650A4", 13, 19, 0),
            goal("Film izle", 1f, 1f, "film", "#7D5700", 13, 21, 30),

            // 14 gün önce
            goal("Ders çalış", 90f, 75f, "dk", "#1B6299", 14, 16, 0),
            goal("Organik gıda", 5f, 4f, "porsiyon", "#006E2C", 14, 13, 30)
        )
    }
}

