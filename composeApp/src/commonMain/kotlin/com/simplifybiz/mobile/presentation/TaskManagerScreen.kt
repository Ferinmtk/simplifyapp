package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.simplifybiz.mobile.data.TaskItem
import com.simplifybiz.mobile.presentation.components.AppDatePickerDialog
import com.simplifybiz.mobile.presentation.components.AppTimePickerDialog
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Lists Tasks (Do items) for a specific Action Step under a specific Objective.
 *
 * Routes:
 *   ObjectivesListScreen -> ObjectiveScreen(uuid) -> TaskManagerScreen(uuid, stepId)
 *
 * Persistence model: every add / edit / delete fires the VM, which writes
 * locally and pushes immediately. No "save" button — actions ARE the save.
 */
data class TaskManagerScreen(
    val objectiveUuid: String,
    val actionStepId: String
) : Screen {

    private enum class DialogMode { NONE, EDIT, CREATE }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<ObjectivesViewModel> { parametersOf(objectiveUuid) }

        val currentObj by viewModel.currentObjective.collectAsState()
        val parentStep = currentObj.actionSteps.firstOrNull { it.id == actionStepId }

        var selectedItem by remember { mutableStateOf<TaskItem?>(null) }
        var dialogMode by remember { mutableStateOf(DialogMode.NONE) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Tasks", style = MaterialTheme.typography.titleMedium)
                            if (parentStep != null && parentStep.name.isNotBlank()) {
                                Text(
                                    parentStep.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                if (parentStep != null) {
                    FloatingActionButton(
                        onClick = {
                            selectedItem = TaskItem(id = getTimeMillis().toString())
                            dialogMode = DialogMode.CREATE
                        },
                        containerColor = Color(0xFF4A148C),
                        contentColor = Color.White
                    ) { Icon(Icons.Default.Add, "Add Task") }
                }
            }
        ) { padding ->
            if (parentStep == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Action step not found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (parentStep.tasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tasks yet. Tap + to add one.", color = Color.Gray)
                            }
                        }
                    }

                    items(parentStep.tasks, key = { it.id }) { task ->
                        TaskListRow(
                            item = task,
                            onEdit = {
                                selectedItem = it
                                dialogMode = DialogMode.EDIT
                            },
                            onDelete = { viewModel.deleteTask(actionStepId, it.id) }
                        )
                    }
                }
            }

            when (dialogMode) {
                DialogMode.CREATE -> {
                    TaskEditDialog(
                        item = selectedItem!!,
                        onDismiss = { dialogMode = DialogMode.NONE },
                        onSave = { updated ->
                            viewModel.addTask(actionStepId, updated)
                            dialogMode = DialogMode.NONE
                        }
                    )
                }
                DialogMode.EDIT -> {
                    TaskEditDialog(
                        item = selectedItem!!,
                        onDismiss = { dialogMode = DialogMode.NONE },
                        onSave = { updated ->
                            viewModel.updateTask(actionStepId, updated)
                            dialogMode = DialogMode.NONE
                        }
                    )
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun TaskListRow(
    item: TaskItem,
    onEdit: (TaskItem) -> Unit,
    onDelete: (TaskItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (item.taskText.isBlank()) "(unnamed task)" else item.taskText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (item.pointPerson.isNotBlank()) {
                    Text("Person: ${item.pointPerson}", fontSize = 13.sp, color = Color.Gray)
                }
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
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF4A148C))
            }
            IconButton(onClick = { onDelete(item) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun TaskEditDialog(
    item: TaskItem,
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit
) {
    var taskText by remember { mutableStateOf(item.taskText) }
    var pointPerson by remember { mutableStateOf(item.pointPerson) }
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
                Text("Task", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("Task *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pointPerson,
                    onValueChange = { pointPerson = it },
                    label = { Text("Point Person *") },
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
                                    taskText = taskText,
                                    pointPerson = pointPerson,
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