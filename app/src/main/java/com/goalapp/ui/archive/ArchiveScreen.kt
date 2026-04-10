package com.goalapp.ui.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goalapp.R
import com.goalapp.data.GoalEntity
import com.goalapp.ui.components.GoalCard
import com.goalapp.util.toEpochDayFromUtcDate
import com.goalapp.util.toUtcStartOfDayMillis
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onGoalClick: (Long) -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    var showCalendarPicker by remember { mutableStateOf(false) }
    
    // ✨ Tek bir UI State - daha temiz ve performanslı!
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.archive_title), fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        // Loading State
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        
        // Error State
        if (uiState.error != null) {
            ErrorState(
                error = uiState.error,
                onRetry = { viewModel.clearError() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            return@Scaffold
        }
        
        // Empty Archive State (hiç arşiv yok)
        if (uiState.hasNoArchive) {
            EmptyArchiveState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
            return@Scaffold
        }
        
        // Content - Arşiv var
        val availableDays = uiState.availableDays
        val selectedDay = uiState.selectedDay
        val archivedGoals = uiState.archivedGoals

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDay?.toDisplayDate() ?: stringResource(R.string.archive_select_day),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = { showCalendarPicker = true }) {
                    Text(stringResource(R.string.archive_open_calendar))
                }
            }

            if (uiState.hasNoGoalsForSelectedDay) {
                EmptySelectedDayState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 32.dp)
                )
            } else {
                ArchiveDaySummaryCard(
                    goals = archivedGoals,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = padding.calculateBottomPadding() + 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(archivedGoals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = { onGoalClick(goal.id) }
                        )
                    }
                }
            }
        }

        // Calendar Picker Dialog - Scaffold içinde
        if (showCalendarPicker) {
            val archivedDateMillis = availableDays.map { it.toUtcStartOfDayMillis() }.toSet()
            
            val selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis in archivedDateMillis
                }
            }
            
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDay?.toUtcStartOfDayMillis(),
                selectableDates = selectableDates
            )
            DatePickerDialog(
                onDismissRequest = { showCalendarPicker = false },
                confirmButton = {
                    Button(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                viewModel.selectDay(selectedMillis.toEpochDayFromUtcDate())
                            }
                            showCalendarPicker = false
                        }
                    ) {
                        Text(stringResource(R.string.archive_calendar_select))
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showCalendarPicker = false }) {
                        Text(stringResource(R.string.archive_calendar_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "⚠️",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.error_retry))
        }
    }
}

@Composable
private fun EmptySelectedDayState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.archive_empty_day_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.archive_empty_day_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ArchiveDaySummaryCard(
    goals: List<GoalEntity>,
    modifier: Modifier = Modifier
) {
    val totalGoals = goals.size
    val completedGoals = goals.count { it.isCompleted }
    val avgProgress = if (goals.isEmpty()) 0f
    else goals.map { it.progressPercent }.average().toFloat()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = stringResource(R.string.archive_stats_total),
                value = "$totalGoals",
                modifier = Modifier.weight(1f)
            )
            
            StatItem(
                label = stringResource(R.string.archive_stats_completed),
                value = "$completedGoals",
                modifier = Modifier.weight(1f)
            )
            
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { avgProgress / 100f },
                            modifier = Modifier.size(60.dp),
                            strokeWidth = 6.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "${avgProgress.toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.archive_stats_avg_progress),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun Long.toDisplayDate(): String {
    val date = LocalDate.ofEpochDay(this)
    val today = LocalDate.now()
    val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(date, today).toInt()
    
    return when {
        daysDiff == 0 -> stringResource(R.string.date_today)
        daysDiff == 1 -> stringResource(R.string.date_yesterday)
        daysDiff in 2..6 -> stringResource(R.string.date_days_ago, daysDiff)
        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())
            date.format(formatter)
        }
    }
}

@Composable
private fun EmptyArchiveState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "📦",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            stringResource(R.string.archive_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.archive_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.archive_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

