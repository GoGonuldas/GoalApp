package com.goalapp.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.goalapp.ui.theme.parseColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

val PRESET_COLORS = listOf(
    "#6650A4", "#B5264C", "#006874",
    "#7D5700", "#006E2C", "#1B6299"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AddGoalViewModel = hiltViewModel()
) {
    fun parseTarget(input: String): Float? {
        val normalized = input.trim().replace(',', '.')
        return normalized.toFloatOrNull()
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetValueStr by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PRESET_COLORS.first()) }
    
    // Tarih seçimi için state'ler
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    var selectedDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Bildirim için state'ler
    var notificationEnabled by remember { mutableStateOf(false) }
    var notificationHour by remember { mutableStateOf(9) }
    var notificationMinute by remember { mutableStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }

    val parsedTarget = parseTarget(targetValueStr) ?: 0f
    val isValid = title.isNotBlank() && parsedTarget > 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Hedef", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Hedef başlığı *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama (opsiyonel)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = targetValueStr,
                    onValueChange = { targetValueStr = it },
                    label = { Text("Hedef değer *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Birim") },
                    placeholder = { Text("km, sayfa…") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Tarih seçici
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Hedef Tarihi",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = selectedDate.format(
                                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Tarih seç",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Bildirim seçici
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Hatırlatıcı",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { notificationEnabled = it }
                        )
                    }
                    
                    if (notificationEnabled) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Saat: %02d:%02d".format(notificationHour, notificationMinute))
                        }
                    }
                }
            }

            // Renk seçici
            Column {
                Text(
                    "Renk seç",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PRESET_COLORS.forEach { hex ->
                        val color = parseColor(hex)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (hex == selectedColor)
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape
                                        )
                                    else Modifier
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (hex == selectedColor) {
                                Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    // Convert selected date to milliseconds at start of day
                    val createdAtMillis = selectedDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    
                    viewModel.saveGoal(
                        title = title,
                        description = description,
                        targetValue = parsedTarget,
                        unit = unit,
                        colorHex = selectedColor,
                        createdAt = createdAtMillis,
                        notificationEnabled = notificationEnabled,
                        notificationHour = if (notificationEnabled) notificationHour else null,
                        notificationMinute = if (notificationEnabled) notificationMinute else null,
                        onSuccess = {
                            onSaveSuccess()
                        }
                    )
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hedefi Kaydet", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
        
        // DatePicker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli(),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = Instant.ofEpochMilli(utcTimeMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        // Sadece bugün ve gelecek tarihler seçilebilir
                        return !date.isBefore(today)
                    }
                }
            )
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Tamam")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDatePicker = false }) {
                        Text("İptal")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        // TimePicker Dialog
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = notificationHour,
                initialMinute = notificationMinute,
                is24Hour = true
            )
            
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("Hatırlatıcı Saati") },
                text = {
                    TimePicker(state = timePickerState)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            notificationHour = timePickerState.hour
                            notificationMinute = timePickerState.minute
                            showTimePicker = false
                        }
                    ) {
                        Text("Tamam")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showTimePicker = false }) {
                        Text("İptal")
                    }
                }
            )
        }
    }
}
