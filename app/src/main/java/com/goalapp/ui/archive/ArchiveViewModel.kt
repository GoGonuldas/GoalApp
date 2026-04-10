package com.goalapp.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goalapp.data.GoalEntity
import com.goalapp.data.GoalRepository
import com.goalapp.data.SampleGoalsProvider
import com.goalapp.util.toEpochDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val sampleGoalsProvider: SampleGoalsProvider
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val allArchivedGoals: StateFlow<List<GoalEntity>> = repository
        .getArchivedGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
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
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    private val _selectedEpochDay = MutableStateFlow<Long?>(null)
    
    // Otomatik olarak ilk günü seç
    val selectedEpochDay: StateFlow<Long?> = combine(
        _selectedEpochDay,
        availableEpochDays
    ) { selected, availableDays ->
        when {
            selected != null && selected in availableDays -> selected
            availableDays.isNotEmpty() -> availableDays.first()
            else -> null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = null
    )

    val archivedGoals: StateFlow<List<GoalEntity>> = combine(
        allArchivedGoals, 
        selectedEpochDay
    ) { goals, selected ->
        if (selected == null) emptyList()
        else goals.filter { it.createdAt.toEpochDay() == selected }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    init {
        seedArchiveSampleDataIfNeeded()
    }

    fun selectDay(epochDay: Long) {
        _selectedEpochDay.value = epochDay
    }

    private fun seedArchiveSampleDataIfNeeded() {
        viewModelScope.launch {
            try {
                val existingCount = repository.getGoalCount()
                if (existingCount > 0) return@launch
                
                // TODO: Production'da bu kodu kaldırın veya BuildConfig.DEBUG ile çevreleyın
                repository.insertGoals(sampleGoalsProvider.buildArchiveSampleGoals())
            } catch (e: Exception) {
                // Error handling - gerçek uygulamada loglama yapılmalı
                e.printStackTrace()
            }
        }
    }
}

