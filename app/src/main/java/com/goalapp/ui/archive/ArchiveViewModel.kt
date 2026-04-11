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

    /**
     * UI State sarmalayıcısı - tüm UI ile ilgili state'leri tek yerde toplar
     */
    data class ArchiveUiState(
        val availableDays: List<Long> = emptyList(),
        val selectedDay: Long? = null,
        val archivedGoals: List<GoalEntity> = emptyList(),
        val allArchivedGoals: List<GoalEntity> = emptyList(), // Haftalık grafik için
        val isLoading: Boolean = true,
        val error: String? = null,
        val isEmpty: Boolean = true
    ) {
        /**
         * Arşivde hiç hedef var mı?
         */
        val hasNoArchive: Boolean get() = !isLoading && availableDays.isEmpty() && error == null
        
        /**
         * Seçili günde hedef var mı?
         */
        val hasNoGoalsForSelectedDay: Boolean get() = 
            !isLoading && selectedDay != null && archivedGoals.isEmpty() && error == null
    }

    private val allArchivedGoals: StateFlow<List<GoalEntity>> = repository
        .getArchivedGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    private val _selectedEpochDay = MutableStateFlow<Long?>(null)
    private val _error = MutableStateFlow<String?>(null)
    
    /**
     * Tek bir UI State - Compose tarafından tek seferde observe edilir
     */
    val uiState: StateFlow<ArchiveUiState> = combine(
        allArchivedGoals,
        _selectedEpochDay,
        _error
    ) { goals, manuallySelected, error ->
        // Hata varsa hata durumunu döndür
        if (error != null) {
            return@combine ArchiveUiState(
                isLoading = false,
                error = error,
                isEmpty = goals.isEmpty()
            )
        }
        
        // Mevcut günleri hesapla
        val availableDays = goals
            .map { it.createdAt.toEpochDay() }
            .distinct()
            .sortedDescending()
        
        // Seçili günü belirle (manuel seçim yoksa ilk günü otomatik seç)
        val selectedDay = when {
            manuallySelected != null && manuallySelected in availableDays -> manuallySelected
            availableDays.isNotEmpty() -> availableDays.first()
            else -> null
        }
        
        // Seçili güne ait hedefleri filtrele
        val filteredGoals = if (selectedDay != null) {
            goals.filter { it.createdAt.toEpochDay() == selectedDay }
        } else {
            emptyList()
        }
        
        ArchiveUiState(
            availableDays = availableDays,
            selectedDay = selectedDay,
            archivedGoals = filteredGoals,
            allArchivedGoals = goals, // Tüm arşiv hedefleri - grafik için
            isLoading = false,
            error = null,
            isEmpty = goals.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = ArchiveUiState()
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
                _error.value = null // Hatayı temizle
                val existingCount = repository.getGoalCount()
                if (existingCount > 0) return@launch
                
                // TODO: Production'da bu kodu kaldırın veya BuildConfig.DEBUG ile çevreleyın
                repository.insertGoals(sampleGoalsProvider.buildArchiveSampleGoals())
            } catch (e: Exception) {
                // Hata durumunda UI'ya bildir
                _error.value = "Örnek veriler yüklenirken bir hata oluştu: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Hata mesajını temizler
     */
    fun clearError() {
        _error.value = null
    }
}

