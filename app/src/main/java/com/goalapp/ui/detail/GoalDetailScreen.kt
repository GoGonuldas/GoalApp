package com.goalapp.ui.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goalapp.R
import com.goalapp.ui.theme.parseColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    onBack: () -> Unit,
    onSaveProgressSuccess: () -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel()
) {
    val goal by viewModel.goal.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    goal?.let { g ->
        val color = parseColor(g.colorHex)
        var sliderValue by remember { mutableFloatStateOf(g.currentValue) }

        val animatedProgress by animateFloatAsState(
            targetValue = (sliderValue / g.targetValue).coerceIn(0f, 1f),
            animationSpec = tween(600),
            label = "detail_progress"
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(g.title, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back))
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.detail_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Büyük dairesel progress göstergesi
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = color,
                        trackColor = color.copy(alpha = 0.12f),
                        strokeWidth = 14.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            text = if (g.isCompleted) 
                                stringResource(R.string.detail_completed) 
                            else 
                                stringResource(R.string.detail_in_progress),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                if (g.description.isNotBlank()) {
                    Text(
                        text = g.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider()

                // İlerleme güncelleme
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = color.copy(alpha = 0.06f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.detail_update_progress),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${sliderValue.toInt()} / ${g.targetValue.toInt()} ${g.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            valueRange = 0f..g.targetValue,
                            colors = SliderDefaults.colors(
                                thumbColor = color,
                                activeTrackColor = color,
                                inactiveTrackColor = color.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                // Notlar Bölümü
                var showNotesDialog by remember { mutableStateOf(false) }
                var notesText by remember { mutableStateOf(g.notes) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                            Text(
                                stringResource(R.string.detail_notes_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { showNotesDialog = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = stringResource(R.string.detail_notes_edit),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (g.notes.isBlank()) {
                                stringResource(R.string.detail_notes_empty)
                            } else {
                                g.notes
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (g.notes.isBlank()) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
                
                // Notlar Düzenleme Dialog
                if (showNotesDialog) {
                    AlertDialog(
                        onDismissRequest = { 
                            showNotesDialog = false
                            notesText = g.notes // Reset
                        },
                        title = { Text(stringResource(R.string.detail_notes_title)) },
                        text = {
                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                placeholder = { Text(stringResource(R.string.detail_notes_hint)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                maxLines = 10,
                                shape = RoundedCornerShape(12.dp)
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.updateNotes(notesText)
                                    showNotesDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.detail_notes_save))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { 
                                showNotesDialog = false
                                notesText = g.notes // Reset
                            }) {
                                Text(stringResource(R.string.detail_delete_cancel))
                            }
                        }
                    )
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { viewModel.updateProgress(sliderValue, onSaved = onSaveProgressSuccess) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    Text(stringResource(R.string.detail_save_progress), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.detail_delete_title)) },
                text = { Text(stringResource(R.string.detail_delete_message)) },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.deleteGoal(onBack) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text(stringResource(R.string.detail_delete_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { 
                        Text(stringResource(R.string.detail_delete_cancel)) 
                    }
                }
            )
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
