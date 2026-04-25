package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Dedicated Action Steps screen.
 *
 *   Top:    Objective picker dropdown (required selection).
 *   Body:   List of action steps for the picked objective.
 *   FAB:    Add a new action step under the picked objective.
 *
 * Mirrors the webapp's "View Action Steps" page where action steps are the
 * primary content and the objective is a filter at top.
 */
class ActionStepsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val listVm = koinViewModel<ObjectivesListViewModel>()
        val objectives by listVm.objectives.collectAsState()

        // Picked objective uuid persists across rotation. Initially null —
        // user must pick one before action steps appear.
        var selectedUuid by rememberSaveable { mutableStateOf<String?>(null) }

        // Auto-select the first objective when the list arrives so the user
        // isn't staring at a blank screen on entry. They can change it.
        LaunchedEffect(objectives) {
            if (selectedUuid == null && objectives.isNotEmpty()) {
                selectedUuid = objectives.first().uuid
            }
            // If the picked objective got deleted elsewhere, fall back to first.
            if (selectedUuid != null && objectives.none { it.uuid == selectedUuid }) {
                selectedUuid = objectives.firstOrNull()?.uuid
            }
        }

        val isRefreshing by listVm.isRefreshing.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Action Steps", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { if (!isRefreshing) listVm.refresh() }) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = Color(0xFF4A148C))
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4A148C)
                    )
                }

                // Objective picker
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        "OBJECTIVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF616161),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ObjectivePicker(
                        objectives = objectives,
                        selectedUuid = selectedUuid,
                        onSelect = { selectedUuid = it }
                    )
                }

                Divider(color = Color(0xFFEEEEEE))

                // Body — switches based on whether we have a selection
                when {
                    objectives.isEmpty() -> {
                        EmptyState(
                            title = "No objectives yet",
                            subtitle = "Create an objective first, then come back to add action steps."
                        )
                    }
                    selectedUuid == null -> {
                        EmptyState(
                            title = "Pick an objective",
                            subtitle = "Action steps belong to an objective."
                        )
                    }
                    else -> {
                        ActionStepsBody(
                            objectiveUuid = selectedUuid!!,
                            onManageTasks = { stepId ->
                                navigator?.push(
                                    TaskManagerScreen(
                                        objectiveUuid = selectedUuid!!,
                                        actionStepId = stepId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Picker
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ObjectivePicker(
    objectives: List<com.simplifybiz.mobile.data.ObjectiveEntity>,
    selectedUuid: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = objectives.firstOrNull { it.uuid == selectedUuid }
    val label = selected?.objectiveText?.ifBlank { "(untitled)" } ?: "Select an objective"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (objectives.isNotEmpty()) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4A148C),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            objectives.forEach { obj ->
                DropdownMenuItem(
                    text = {
                        Text(
                            obj.objectiveText.ifBlank { "(untitled)" },
                            maxLines = 2
                        )
                    },
                    onClick = {
                        onSelect(obj.uuid)
                        expanded = false
                    }
                )
            }
        }
    }
}

// =============================================================================
// Body — uses an ObjectivesViewModel keyed by the picked uuid so it
// always reflects the right objective's state and routes mutations there.
// =============================================================================

@Composable
private fun ActionStepsBody(
    objectiveUuid: String,
    onManageTasks: (String) -> Unit
) {
    // key = objectiveUuid forces a fresh VM whenever the picker changes.
    val viewModel = koinViewModel<ObjectivesViewModel>(
        key = objectiveUuid,
        parameters = { parametersOf(objectiveUuid) }
    )
    val obj by viewModel.currentObjective.collectAsState()

    var dialogStep by remember(objectiveUuid) { mutableStateOf<ActionStepItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (obj.actionSteps.isEmpty()) {
            EmptyState(
                title = "No action steps yet",
                subtitle = "Tap + ADD ACTION STEP to create one."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(obj.actionSteps, key = { it.id }) { step ->
                    ActionStepRow(
                        item = step,
                        onEdit = { dialogStep = it },
                        onDelete = { viewModel.deleteActionStep(it.id) },
                        onManageTasks = { onManageTasks(it.id) }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { dialogStep = ActionStepItem(id = getTimeMillis().toString()) },
            containerColor = Color(0xFF4A148C),
            contentColor = Color.White,
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("ADD ACTION STEP") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (dialogStep != null) {
        ActionStepEditDialog(
            item = dialogStep!!,
            onDismiss = { dialogStep = null },
            onSave = { saved ->
                if (obj.actionSteps.any { it.id == saved.id }) viewModel.updateActionStep(saved)
                else viewModel.addActionStep(saved)
                dialogStep = null
            }
        )
    }
}

// =============================================================================
// Reusable row card and empty state
// =============================================================================

@Composable
private fun ActionStepRow(
    item: ActionStepItem,
    onEdit: (ActionStepItem) -> Unit,
    onDelete: (ActionStepItem) -> Unit,
    onManageTasks: (ActionStepItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepStatusIndicator(status = item.status)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (item.name.isBlank()) "(unnamed step)" else item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
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
private fun StepStatusIndicator(status: String) {
    val (icon: ImageVector, tint: Color) = when (status) {
        "Completed" -> Icons.Default.CheckCircle to Color(0xFF388E3C)
        "Started" -> Icons.Default.RadioButtonChecked to Color(0xFFF57C00)
        else -> Icons.Outlined.Circle to Color.Gray
    }
    Box(
        modifier = Modifier.size(28.dp).clip(CircleShape)
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = Color.Gray, fontSize = 13.sp)
        }
    }
}

// =============================================================================
// Action Step edit dialog — same as the one in ObjectiveScreen, with real
// date/time pickers. Kept local so this screen stays self-contained.
// =============================================================================

@Composable
private fun ActionStepEditDialog(
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