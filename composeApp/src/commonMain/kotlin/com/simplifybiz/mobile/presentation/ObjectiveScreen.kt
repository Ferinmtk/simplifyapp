package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.simplifybiz.mobile.data.ActionStepItem
import com.simplifybiz.mobile.presentation.components.LuxuryTextField
import com.simplifybiz.mobile.presentation.components.SectionWithHelp
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel

class ObjectiveScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<ObjectivesViewModel>()
        // Reactive State: Observe the stream from the ViewModel
        val state by viewModel.currentObjective.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()

        var showActionStepDialog by remember { mutableStateOf<ActionStepItem?>(null) }

        DisposableEffect(Unit) {
            onDispose { viewModel.saveDraft() }
        }

        LaunchedEffect(Unit) {
            viewModel.saveSuccess.collect { navigator?.pop() }
        }

        LaunchedEffect(Unit) {
            viewModel.validationMessage.collect { snackbarHostState.showSnackbar(it) }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Strategic Objective", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, "Update", tint = Color(0xFF4A148C))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isRefreshing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF4A148C))
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    SectionWithHelp(
                        title = "1. The Objective *",
                        helpTitle = "Strategic Objective",
                        helpText = "Define what you want to achieve in this period."
                    ) {
                        LuxuryTextField(
                            value = state.objectiveText,
                            onValueChange = viewModel::onObjectiveChange,
                            label = "Define your objective",
                            minLines = 3
                        )
                    }

                    SectionWithHelp(
                        title = "2. Point Person",
                        helpTitle = "Point Person",
                        helpText = "Who is primarily responsible for this objective?"
                    ) {
                        LuxuryTextField(
                            value = state.pointPerson,
                            onValueChange = viewModel::onPointPersonChange,
                            label = "Assigned person"
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Due Date", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedCard(
                                onClick = {
                                    // Placeholder: In a real app, trigger a DatePickerDialog here
                                    // viewModel.onDueDateChange("Selected Date String")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(state.dueDate.ifBlank { "Select Date" }, modifier = Modifier.padding(16.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Due Time", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedCard(
                                onClick = {
                                    // Placeholder: In a real app, trigger a TimePickerDialog here
                                    // viewModel.onDueTimeChange("Selected Time String")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(state.dueTime.ifBlank { "Select Time" }, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }

                    SectionWithHelp(
                        title = "3. Progress Status",
                        helpTitle = "Completion Status",
                        helpText = "Mark the current stage of this objective."
                    ) {
                        ObjectiveStatusSelector(
                            selected = state.completionStatus,
                            onSelect = viewModel::onStatusChange
                        )
                    }

                    SectionWithHelp(
                        title = "4. Action Steps",
                        helpTitle = "Action Steps",
                        helpText = "Break down the objective into specific tasks."
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.actionSteps.forEach { step ->
                                ActionStepCard(
                                    item = step,
                                    onEdit = { showActionStepDialog = it },
                                    onDelete = { viewModel.deleteActionStep(it.id) }
                                )
                            }

                            OutlinedButton(
                                onClick = { showActionStepDialog = ActionStepItem(id = getTimeMillis().toString()) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("ADD ACTION STEP")
                            }
                        }
                    }

                    Button(
                        onClick = viewModel::submitObjective,
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 32.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C))
                    ) {
                        Text("SAVE OBJECTIVE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showActionStepDialog != null) {
            ActionStepDialog(
                item = showActionStepDialog!!,
                onDismiss = { showActionStepDialog = null },
                onSave = {
                    if (state.actionSteps.any { s -> s.id == it.id }) viewModel.updateActionStep(it)
                    else viewModel.addActionStep(it)
                    showActionStepDialog = null
                }
            )
        }
    }
}

@Composable
private fun ActionStepCard(item: ActionStepItem, onEdit: (ActionStepItem) -> Unit, onDelete: (ActionStepItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.taskName, fontWeight = FontWeight.Bold)
                Text("Person: ${item.pointPerson}", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = { onEdit(item) }) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
            IconButton(onClick = { onDelete(item) }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f)) }
        }
    }
}

@Composable
private fun ObjectiveStatusSelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        // Aligning status values with the standardized Backend/Operations logic
        StatusCircle(Icons.Outlined.Circle, selected == "Not Started", "Not Started") { onSelect("Not Started") }
        StatusCircle(Icons.Default.RadioButtonChecked, selected == "In Progress", "Started") { onSelect("In Progress") }
        StatusCircle(Icons.Default.CheckCircle, selected == "Fully Implemented", "Completed") { onSelect("Fully Implemented") }
    }
}

@Composable
private fun StatusCircle(icon: ImageVector, isSelected: Boolean, label: String, onClick: () -> Unit) {
    val brandPurple = Color(0xFF4A148C)
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape)
                .background(if (isSelected) brandPurple.copy(alpha = 0.2f) else Color(0xFFEEEEEE))
                .border(2.dp, if (isSelected) brandPurple else Color.Transparent, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (isSelected) brandPurple else Color.Gray)
        }
        Text(label, fontSize = 11.sp, color = if (isSelected) brandPurple else Color.Gray)
    }
}

@Composable
private fun ActionStepDialog(item: ActionStepItem, onDismiss: () -> Unit, onSave: (ActionStepItem) -> Unit) {
    var taskName by remember { mutableStateOf(item.taskName) }
    var pointPerson by remember { mutableStateOf(item.pointPerson) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Action Step") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = taskName, onValueChange = { taskName = it }, label = { Text("Task Name") })
                OutlinedTextField(value = pointPerson, onValueChange = { pointPerson = it }, label = { Text("Point Person") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(item.copy(taskName = taskName, pointPerson = pointPerson)) }) { Text("SAVE") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
