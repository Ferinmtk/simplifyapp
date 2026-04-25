package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.simplifybiz.mobile.data.ObjectiveEntity
import org.koin.compose.viewmodel.koinViewModel

class ObjectivesListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<ObjectivesListViewModel>()
        val objectives by viewModel.objectives.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        var pendingDelete by remember { mutableStateOf<ObjectiveEntity?>(null) }

        LaunchedEffect(Unit) {
            viewModel.validationMessage.collect { snackbarHostState.showSnackbar(it) }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Objectives", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = Color(0xFF4A148C))
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        // New objective: push edit screen with empty uuid.
                        navigator?.push(ObjectiveScreen(uuid = ""))
                    },
                    containerColor = Color(0xFF4A148C),
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("ADD OBJECTIVE") }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4A148C)
                    )
                }

                if (objectives.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No objectives yet.", color = Color.Gray, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap ADD OBJECTIVE to create your first one.",
                                color = Color.Gray, fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(objectives, key = { it.uuid }) { obj ->
                            ObjectiveListCard(
                                item = obj,
                                onClick = { navigator?.push(ObjectiveScreen(uuid = obj.uuid)) },
                                onDelete = { pendingDelete = obj }
                            )
                        }
                    }
                }
            }
        }

        pendingDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("Delete objective?") },
                text = {
                    Text(
                        "This removes \"${target.objectiveText.ifBlank { "(unnamed)" }}\" " +
                                "from this device only. To delete from the server, use the webapp."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteObjective(target.uuid)
                        pendingDelete = null
                    }) { Text("DELETE", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) { Text("CANCEL") }
                }
            )
        }
    }
}

@Composable
private fun ObjectiveListCard(
    item: ObjectiveEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIndicator(status = item.completionStatus)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (item.objectiveText.isBlank()) "(untitled objective)" else item.objectiveText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    buildString {
                        if (item.pointPerson.isNotBlank()) append(item.pointPerson)
                        if (item.dueDate.isNotBlank()) {
                            if (isNotEmpty()) append("  •  ")
                            append("Due ${item.dueDate}")
                        }
                        append("  •  ${item.actionSteps.size} step")
                        if (item.actionSteps.size != 1) append("s")
                    },
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: String) {
    val (icon: ImageVector, tint: Color) = when (status) {
        "Fully Implemented", "Completed" -> Icons.Default.CheckCircle to Color(0xFF388E3C)
        "In Progress", "Started" -> Icons.Default.RadioButtonChecked to Color(0xFFF57C00)
        else -> Icons.Outlined.Circle to Color.Gray
    }
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape)
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}