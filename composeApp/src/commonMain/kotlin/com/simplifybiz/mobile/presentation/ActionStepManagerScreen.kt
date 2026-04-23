package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.simplifybiz.mobile.data.ActionStepItem
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel

class ActionStepManagerScreen : Screen {

    internal enum class DialogMode { NONE, VIEW, EDIT, CREATE }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<ObjectivesViewModel>()

        // REACTIVE: Observe the objective state from the ViewModel
        val currentObj by viewModel.currentObjective.collectAsState()

        var selectedItem by remember { mutableStateOf<ActionStepItem?>(null) }
        var dialogMode by remember { mutableStateOf(DialogMode.NONE) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Action Steps", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        selectedItem = ActionStepItem(id = getTimeMillis().toString())
                        dialogMode = DialogMode.CREATE
                    },
                    containerColor = Color(0xFF4A148C),
                    contentColor = Color.White
                ) { Icon(Icons.Default.Add, "Add Step") }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentObj.actionSteps, key = { it.id }) { step ->
                    ActionStepListItem(
                        item = step,
                        onEdit = {
                            selectedItem = it
                            dialogMode = DialogMode.EDIT
                        }
                    )
                }
            }

            // DIALOG LOGIC
            when (dialogMode) {
                DialogMode.CREATE -> {
                    ActionStepEditDialog(
                        item = selectedItem!!,
                        onDismiss = { dialogMode = DialogMode.NONE },
                        onSave = { updatedStep ->
                            viewModel.addActionStep(updatedStep)
                            dialogMode = DialogMode.NONE
                        }
                    )
                }
                DialogMode.EDIT -> {
                    ActionStepEditDialog(
                        item = selectedItem!!,
                        onDismiss = { dialogMode = DialogMode.NONE },
                        onSave = { updatedStep ->
                            viewModel.updateActionStep(updatedStep)
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
private fun ActionStepListItem(item: ActionStepItem, onEdit: (ActionStepItem) -> Unit) {
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
                Text(item.taskName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Person: ${item.pointPerson}", fontSize = 13.sp, color = Color.Gray)
            }
            IconButton(onClick = { onEdit(item) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF4A148C))
            }
        }
    }
}

@Composable
fun ActionStepEditDialog(
    item: ActionStepItem,
    onDismiss: () -> Unit,
    onSave: (ActionStepItem) -> Unit
) {
    var taskName by remember { mutableStateOf(item.taskName) }
    var pointPerson by remember { mutableStateOf(item.pointPerson) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(item.copy(taskName = taskName, pointPerson = pointPerson)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C))
            ) { Text("SAVE") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } },
        title = { Text("Edit Action Step") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = taskName, onValueChange = { taskName = it }, label = { Text("Task Name") })
                OutlinedTextField(value = pointPerson, onValueChange = { pointPerson = it }, label = { Text("Point Person") })
            }
        }
    )
}
