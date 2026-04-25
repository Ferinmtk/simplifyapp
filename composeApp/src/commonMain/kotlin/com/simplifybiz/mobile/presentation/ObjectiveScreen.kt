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
import com.simplifybiz.mobile.presentation.components.AppDatePickerDialog
import com.simplifybiz.mobile.presentation.components.AppTimePickerDialog
import com.simplifybiz.mobile.presentation.components.LuxuryTextField
import com.simplifybiz.mobile.presentation.components.SectionWithHelp
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Edits ONE objective. Pass uuid="" for a new objective; the VM mints one.
 */
data class ObjectiveScreen(val uuid: String = "") : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<ObjectivesViewModel> { parametersOf(uuid) }
        val state by viewModel.currentObjective.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()

        var showActionStepDialog by remember { mutableStateOf<ActionStepItem?>(null) }
        var showDatePickerForObjective by remember { mutableStateOf(false) }
        var showTimePickerForObjective by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.saveSuccess.collect { navigator?.pop() }
        }

        LaunchedEffect(Unit) {
            viewModel.validationMessage.collect { snackbarHostState.showSnackbar(it) }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (uuid.isBlank()) "New Objective" else "Strategic Objective",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
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
                        title = "2. Expected Outcomes",
                        helpTitle = "Expected Outcomes",
                        helpText = "List the concrete results you expect once this objective is met. Add as many as you need."
                    ) {
                        ExpectedOutcomesEditor(
                            outcomes = state.expectedOutcomes,
                            onChange = viewModel::updateExpectedOutcome,
                            onAdd = viewModel::addExpectedOutcome,
                            onRemove = viewModel::removeExpectedOutcome
                        )
                    }

                    SectionWithHelp(
                        title = "3. Point Person",
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
                                onClick = { showDatePickerForObjective = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(state.dueDate.ifBlank { "Select Date" }, modifier = Modifier.padding(16.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Due Time", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedCard(
                                onClick = { showTimePickerForObjective = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(state.dueTime.ifBlank { "Select Time" }, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }

                    SectionWithHelp(
                        title = "4. Progress Status",
                        helpTitle = "Completion Status",
                        helpText = "Mark the current stage of this objective."
                    ) {
                        ObjectiveStatusSelector(
                            selected = state.completionStatus,
                            onSelect = viewModel::onStatusChange
                        )
                    }

                    SectionWithHelp(
                        title = "5. Action Steps",
                        helpTitle = "Action Steps",
                        helpText = "Break down the objective into specific action steps. Each step can have its own tasks."
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.actionSteps.forEach { step ->
                                ActionStepCard(
                                    item = step,
                                    onEdit = { showActionStepDialog = it },
                                    onDelete = { viewModel.deleteActionStep(it.id) },
                                    onManageTasks = {
                                        navigator?.push(
                                            TaskManagerScreen(
                                                objectiveUuid = state.uuid,
                                                actionStepId = it.id
                                            )
                                        )
                                    }
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

        // --- Pickers (objective level) ---

        if (showDatePickerForObjective) {
            AppDatePickerDialog(
                initialDateIso = state.dueDate,
                onDismiss = { showDatePickerForObjective = false },
                onConfirm = { iso ->
                    viewModel.onDueDateChange(iso)
                    showDatePickerForObjective = false
                }
            )
        }
        if (showTimePickerForObjective) {
            AppTimePickerDialog(
                initialTime = state.dueTime,
                onDismiss = { showTimePickerForObjective = false },
                onConfirm = { time ->
                    viewModel.onDueTimeChange(time)
                    showTimePickerForObjective = false
                }
            )
        }

        // --- Action Step dialog ---

        if (showActionStepDialog != null) {
            ActionStepDialog(
                item = showActionStepDialog!!,
                onDismiss = { showActionStepDialog = null },
                onSave = { saved ->
                    if (state.actionSteps.any { s -> s.id == saved.id }) viewModel.updateActionStep(saved)
                    else viewModel.addActionStep(saved)
                    showActionStepDialog = null
                }
            )
        }
    }
}

// =============================================================================
// Sub-composables
// =============================================================================

@Composable
private fun ExpectedOutcomesEditor(
    outcomes: List<String>,
    onChange: (Int, String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        outcomes.forEachIndexed { index, value ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { onChange(index, it) },
                    placeholder = { Text("Outcome ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = { onRemove(index) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Remove outcome", tint = Color.Gray)
                }
            }
        }
        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text(if (outcomes.isEmpty()) "ADD AN OUTCOME" else "ADD ANOTHER OUTCOME")
        }
    }
}

@Composable
private fun ActionStepCard(
    item: ActionStepItem,
    onEdit: (ActionStepItem) -> Unit,
    onDelete: (ActionStepItem) -> Unit,
    onManageTasks: (ActionStepItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (item.name.isBlank()) "(unnamed step)" else item.name,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        buildString {
                            append("Status: ${item.status}")
                            if (item.dueDate.isNotBlank()) append("  •  Due: ${item.dueDate}")
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = { onEdit(item) }) {
                    Icon(Icons.Default.Edit, null, tint = Color.Gray)
                }
                IconButton(onClick = { onDelete(item) }) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { onManageTasks(item) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Checklist, null, tint = Color(0xFF4A148C))
                Spacer(Modifier.width(8.dp))
                Text("Manage Tasks (${item.tasks.size})", color = Color(0xFF4A148C))
            }
        }
    }
}

@Composable
private fun ObjectiveStatusSelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        StatusCircle(Icons.Outlined.Circle, selected == "Not Started", "Not Started") { onSelect("Not Started") }
        StatusCircle(Icons.Default.RadioButtonChecked, selected == "In Progress", "Started") { onSelect("In Progress") }
        StatusCircle(Icons.Default.CheckCircle, selected == "Fully Implemented", "Completed") { onSelect("Fully Implemented") }
    }
}

@Composable
internal fun StepStatusSelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        StatusCircle(Icons.Outlined.Circle, selected == "Not Started", "Not Started") { onSelect("Not Started") }
        StatusCircle(Icons.Default.RadioButtonChecked, selected == "Started", "Started") { onSelect("Started") }
        StatusCircle(Icons.Default.CheckCircle, selected == "Completed", "Completed") { onSelect("Completed") }
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

// =============================================================================
// Action Step edit dialog — has its own date/time pickers
// =============================================================================

@Composable
private fun ActionStepDialog(
    item: ActionStepItem,
    onDismiss: () -> Unit,
    onSave: (ActionStepItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var dueDate by remember { mutableStateOf(item.dueDate) }
    var dueTime by remember { mutableStateOf(item.dueTime) }
    var status by remember { mutableStateOf(item.status) }
    var dateCompleted by remember { mutableStateOf(item.dateCompleted) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCompletedPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Action Step", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Action Step *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Due Date", fontSize = 12.sp, color = Color.Gray)
                            Text(dueDate.ifBlank { "Select Date" })
                        }
                    }
                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Time", fontSize = 12.sp, color = Color.Gray)
                            Text(dueTime.ifBlank { "Select Time" })
                        }
                    }
                }

                Text("Status", fontSize = 14.sp, color = Color.Gray)
                StepStatusSelector(selected = status, onSelect = { status = it })

                OutlinedCard(
                    onClick = { showCompletedPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Date Completed", fontSize = 12.sp, color = Color.Gray)
                        Text(dateCompleted.ifBlank { "Select Date" })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("CANCEL") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                item.copy(
                                    name = name,
                                    dueDate = dueDate,
                                    dueTime = dueTime,
                                    status = status,
                                    dateCompleted = dateCompleted
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C))
                    ) { Text("SAVE") }
                }
            }
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateIso = dueDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { iso ->
                dueDate = iso
                showDatePicker = false
            }
        )
    }
    if (showTimePicker) {
        AppTimePickerDialog(
            initialTime = dueTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { t ->
                dueTime = t
                showTimePicker = false
            }
        )
    }
    if (showCompletedPicker) {
        AppDatePickerDialog(
            initialDateIso = dateCompleted,
            onDismiss = { showCompletedPicker = false },
            onConfirm = { iso ->
                dateCompleted = iso
                showCompletedPicker = false
            }
        )
    }
}