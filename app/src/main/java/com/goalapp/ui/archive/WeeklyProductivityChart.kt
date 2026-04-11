package com.goalapp.ui.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goalapp.R
import com.goalapp.data.GoalEntity
import com.goalapp.util.toEpochDay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Haftalık Üretkenlik Grafiği
 * Son 7 günü ve her gün tamamlanan hedef sayısını görselleştirir
 */
@Composable
fun WeeklyProductivityChart(
    allArchivedGoals: List<GoalEntity>,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    
    // Son 7 günü hazırla (bugünden 6 gün geriye)
    val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    
    // Her güne ait hedefleri grupla
    val goalsPerDay = last7Days.map { day ->
        val dayEpoch = day.atStartOfDay(zone).toInstant().toEpochMilli().toEpochDay()
        val dayGoals = allArchivedGoals.filter { 
            it.createdAt.toEpochDay() == dayEpoch 
        }
        DayProductivity(
            date = day,
            totalGoals = dayGoals.size,
            completedGoals = dayGoals.count { it.isCompleted }
        )
    }
    
    // Maksimum hedef sayısı (grafik ölçeklendirmesi için)
    val maxGoals = goalsPerDay.maxOfOrNull { it.totalGoals } ?: 1
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weekly_productivity_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.weekly_productivity_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grafik - Isı haritası tarzı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                goalsPerDay.forEach { dayData ->
                    DayProductivityBar(
                        dayData = dayData,
                        maxGoals = maxGoals,
                        isToday = dayData.date == today,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Bir günün üretkenlik verileri
 */
private data class DayProductivity(
    val date: LocalDate,
    val totalGoals: Int,
    val completedGoals: Int
) {
    val completionRate: Float = if (totalGoals > 0) {
        completedGoals.toFloat() / totalGoals.toFloat()
    } else 0f
}

/**
 * Tek bir günün bar grafiği
 */
@Composable
private fun DayProductivityBar(
    dayData: DayProductivity,
    maxGoals: Int,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val barHeight = if (maxGoals > 0) {
        ((dayData.totalGoals.toFloat() / maxGoals.toFloat()) * 60).coerceAtLeast(8f)
    } else 8f
    
    // Renk - tamamlanma oranına göre
    val barColor = when {
        dayData.totalGoals == 0 -> MaterialTheme.colorScheme.surfaceVariant
        dayData.completionRate >= 0.8f -> Color(0xFF4CAF50) // Yeşil - Çok iyi
        dayData.completionRate >= 0.5f -> Color(0xFFFF9800) // Turuncu - İyi
        else -> Color(0xFFF44336) // Kırmızı - Düşük
    }
    
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hedef sayısı
        if (dayData.totalGoals > 0) {
            Text(
                text = "${dayData.totalGoals}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Bar
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(barHeight.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(barColor)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Gün etiketi
        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        Text(
            text = if (isToday) {
                stringResource(R.string.today_label)
            } else {
                dayData.date.format(dayFormatter)
            },
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

