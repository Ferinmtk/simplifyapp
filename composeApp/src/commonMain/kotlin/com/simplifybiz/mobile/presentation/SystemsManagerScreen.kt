package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.simplifybiz.mobile.data.*
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel

// ── Palette ───────────────────────────────────────────────────────────────────
private val Neutral     = Color(0xFF607D8B)
private val CardWhite   = Color(0xFFFFFFFF)
private val PageBg      = Color(0xFFF4F6F8)
private val TextPrimary = Color(0xFF1C1C1E)
private val TextSec     = Color(0xFF6B7280)
private val TextMuted   = Color(0xFFBDBDBD)
private val BorderEmpty = Color(0xFFE0E0E0)
private val BorderFilled= Color(0xFF1C1C1E)
private val DestructiveRed = Color(0xFFEF5350)

// ── Module config ─────────────────────────────────────────────────────────────
private data class ModuleConfig(
    val label: String,
    val color: Color,
    val tint: Color
)

private val moduleConfigs = mapOf(
    "Leadership"  to ModuleConfig("Leadership",  Color(0xFF1A237E), Color(0xFFE8EAF6)),
    "Marketing"   to ModuleConfig("Marketing",   Color(0xFFAD1457), Color(0xFFFCE4EC)),
    "Sales"       to ModuleConfig("Sales",        Color(0xFFB71C1C), Color(0xFFFFEBEE)),
    "Operations"  to ModuleConfig("Operations",  Color(0xFFE65100), Color(0xFFFBE9E7)),
    "People"      to ModuleConfig("People",      Color(0xFF827717), Color(0xFFF9FBE7)),
    "Money"       to ModuleConfig("Money",       Color(0xFF2E7D32), Color(0xFFE8F5E9)),
    "R&D"         to ModuleConfig("R&D",         Color(0xFF00838F), Color(0xFFE0F7FA)),
    "Risk"        to ModuleConfig("Risk & Legal", Color(0xFF1565C0), Color(0xFFE3F2FD))
)

// ── Normalised entry ──────────────────────────────────────────────────────────
private data class UnifiedSystem(
    val id: String,
    val name: String,
    val purpose: String,
    val status: String,
    val module: String
)

class SystemsManagerScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        // ── Inject all module VMs ─────────────────────────────────────────────
        val leadershipVm  = koinViewModel<LeadershipViewModel>()
        val marketingVm   = koinViewModel<MarketingViewModel>()
        val salesVm       = koinViewModel<SalesViewModel>()
        val operationsVm  = koinViewModel<OperationsViewModel>()
        val peopleVm      = koinViewModel<PeopleViewModel>()
        val moneyVm       = koinViewModel<MoneyViewModel>()
        val rdVm          = koinViewModel<ResearchAndDevelopmentViewModel>()
        val riskVm        = koinViewModel<RiskViewModel>()

        val leadershipState  by leadershipVm.uiState.collectAsState()
        val marketingState   by marketingVm.uiState.collectAsState()
        val salesState       by salesVm.uiState.collectAsState()
        val operationsState  by operationsVm.uiState.collectAsState()
        val peopleState      by peopleVm.uiState.collectAsState()
        val moneyState       by moneyVm.uiState.collectAsState()
        val rdState          by rdVm.uiState.collectAsState()
        val riskState        by riskVm.uiState.collectAsState()

        // ── Normalise all systems ─────────────────────────────────────────────
        val grouped = remember(
            leadershipState.systemsUsed, marketingState.systems, salesState.systemsUsed,
            operationsState.systems, peopleState.systems, moneyState.systems,
            rdState.systemsUsed, riskState.systemsUsed
        ) {
            mapOf(
                "Leadership" to leadershipState.systemsUsed.map {
                    UnifiedSystem(it.id, it.systemName, it.purpose, it.status, "Leadership") },
                "Marketing" to marketingState.systems.map {
                    UnifiedSystem(it.id, it.systemOrApplication, it.purpose, it.status, "Marketing") },
                "Sales" to salesState.systemsUsed.map {
                    UnifiedSystem(it.id, it.systemName, it.purpose, it.status, "Sales") },
                "Operations" to operationsState.systems.map {
                    UnifiedSystem(it.id, it.systemName, it.purpose, it.status, "Operations") },
                "People" to peopleState.systems.map {
                    UnifiedSystem(it.id, it.systemOrApplication, it.purpose, it.status, "People") },
                "Money" to moneyState.systems.map {
                    UnifiedSystem(it.id, it.systemOrApplication, it.purpose, it.status, "Money") },
                "R&D" to rdState.systemsUsed.map {
                    UnifiedSystem(it.id, it.systemName, it.purpose, it.status, "R&D") },
                "Risk" to riskState.systemsUsed.map {
                    UnifiedSystem(it.id, it.systemName, it.purpose, it.status, "Risk") }
            ).filter { it.value.isNotEmpty() || true } // keep all groups for Add button
        }

        val totalCount = grouped.values.sumOf { it.size }

        // ── Dialog state ──────────────────────────────────────────────────────
        var showDialog by remember { mutableStateOf(false) }
        var editingSystem by remember { mutableStateOf<UnifiedSystem?>(null) }
        var dialogModule by remember { mutableStateOf("Leadership") }

        if (showDialog) {
            SystemDialog(
                existing = editingSystem,
                module = dialogModule,
                onDismiss = { showDialog = false; editingSystem = null },
                onSave = { name, purpose, status ->
                    val id = editingSystem?.id ?: getTimeMillis().toString()
                    when (dialogModule) {
                        "Leadership" -> {
                            val item = LeadershipSystemItem(id = id, systemName = name, purpose = purpose, status = status)
                            if (editingSystem != null) leadershipVm.updateSystem(item) else leadershipVm.addSystem(item)
                        }
                        "Marketing" -> {
                            if (editingSystem != null) {
                                marketingVm.updateSystem(MarketingSystemItem(id = id, systemOrApplication = name, purpose = purpose, status = status))
                            } else {
                                marketingVm.addSystem(name, purpose, status)
                            }
                        }
                        "Sales" -> {
                            val item = LeadershipSystemItem(id = id, systemName = name, purpose = purpose, status = status)
                            if (editingSystem != null) salesVm.updateSystem(item) else salesVm.addSystem(item)
                        }
                        "Operations" -> {
                            val item = LeadershipSystemItem(id = id, systemName = name, purpose = purpose, status = status)
                            if (editingSystem != null) operationsVm.updateSystem(item) else operationsVm.addSystem(item)
                        }
                        "People" -> {
                            val item = PeopleSystemItem(id = id, systemOrApplication = name, purpose = purpose, status = status)
                            if (editingSystem != null) peopleVm.updateSystem(item) else peopleVm.addSystem(item)
                        }
                        "Money" -> {
                            val item = MoneySystemItem(id = id, systemOrApplication = name, purpose = purpose, status = status)
                            if (editingSystem != null) moneyVm.updateSystem(item) else moneyVm.addSystem(item)
                        }
                        "R&D" -> {
                            val item = LeadershipSystemItem(id = id, systemName = name, purpose = purpose, status = status)
                            if (editingSystem != null) rdVm.updateSystem(item) else rdVm.addSystem(item)
                        }
                        "Risk" -> {
                            val item = LeadershipSystemItem(id = id, systemName = name, purpose = purpose, status = status)
                            if (editingSystem != null) riskVm.updateSystem(item) else riskVm.addSystem(item)
                        }
                    }
                    showDialog = false; editingSystem = null
                },
                onDelete = { id ->
                    when (dialogModule) {
                        "Leadership"  -> leadershipVm.removeSystem(id)
                        "Marketing"   -> marketingVm.removeSystem(id)
                        "Sales"       -> salesVm.removeSystem(id)
                        "Operations"  -> operationsVm.removeSystem(id)
                        "People"      -> peopleVm.removeSystem(id)
                        "Money"       -> moneyVm.removeSystem(id)
                        "R&D"         -> rdVm.removeSystem(id)
                        "Risk"        -> riskVm.removeSystem(id)
                    }
                    showDialog = false; editingSystem = null
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Systems", style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary, fontWeight = FontWeight.SemiBold))
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
                )
            },
            containerColor = PageBg
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // ── Summary hero ──────────────────────────────────────────────
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Neutral),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.ViewList, null, tint = Color.White,
                                    modifier = Modifier.size(26.dp))
                            }
                            Column {
                                Text("$totalCount systems across all modules",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White, fontWeight = FontWeight.SemiBold))
                                Text("Tap a module to add or manage systems",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.7f)))
                            }
                        }
                    }
                }

                // ── One group per module ──────────────────────────────────────
                grouped.forEach { (module, systems) ->
                    val config = moduleConfigs[module] ?: return@forEach

                    item(key = "header_$module") {
                        ModuleGroupHeader(
                            config = config,
                            count = systems.size,
                            onAdd = {
                                editingSystem = null
                                dialogModule = module
                                showDialog = true
                            }
                        )
                    }

                    if (systems.isEmpty()) {
                        item(key = "empty_$module") {
                            EmptyModuleRow(config = config)
                        }
                    } else {
                        items(systems, key = { "${module}_${it.id}" }) { system ->
                            SystemCard(
                                system = system,
                                config = config,
                                onEdit = {
                                    editingSystem = system
                                    dialogModule = module
                                    showDialog = true
                                }
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

// ── Module group header ───────────────────────────────────────────────────────
@Composable
private fun ModuleGroupHeader(config: ModuleConfig, count: Int, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).height(18.dp)
            .clip(RoundedCornerShape(2.dp)).background(config.color))
        Spacer(Modifier.width(8.dp))
        Text(config.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold, color = config.color, letterSpacing = 1.2.sp),
            modifier = Modifier.weight(1f))
        if (count > 0) {
            Surface(shape = RoundedCornerShape(12.dp), color = config.tint) {
                Text("$count", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = config.color, fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.width(8.dp))
        }
        TextButton(
            onClick = onAdd,
            colors = ButtonDefaults.textButtonColors(contentColor = config.color),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(3.dp))
            Text("Add", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

// ── Empty module row ──────────────────────────────────────────────────────────
@Composable
private fun EmptyModuleRow(config: ModuleConfig) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = config.tint.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("No systems added yet",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall.copy(color = config.color.copy(alpha = 0.6f)))
    }
}

// ── System card ───────────────────────────────────────────────────────────────
@Composable
private fun SystemCard(system: UnifiedSystem, config: ModuleConfig, onEdit: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Status indicator
            StatusDot(system.status, config.color)
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(system.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold, color = TextPrimary))
                if (system.purpose.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(system.purpose,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSec),
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(6.dp))
                StatusPill(system.status, config)
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, null, tint = config.color,
                    modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Status dot ────────────────────────────────────────────────────────────────
@Composable
private fun StatusDot(status: String, color: Color) {
    val dotColor = when (status) {
        "Fully Implemented" -> Color(0xFF2E7D32)
        "In Progress"       -> Color(0xFFF57C00)
        else                -> Color(0xFFBDBDBD)
    }
    Box(
        modifier = Modifier.padding(top = 4.dp).size(10.dp).clip(CircleShape).background(dotColor)
    )
}

// ── Status pill ───────────────────────────────────────────────────────────────
@Composable
private fun StatusPill(status: String, config: ModuleConfig) {
    val (bg, fg) = when (status) {
        "Fully Implemented" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "In Progress"       -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        else                -> Color(0xFFF5F5F5) to Color(0xFF9E9E9E)
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(status,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = fg, fontWeight = FontWeight.SemiBold))
    }
}

// ── System Dialog — Add / Edit ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SystemDialog(
    existing: UnifiedSystem?,
    module: String,
    onDismiss: () -> Unit,
    onSave: (name: String, purpose: String, status: String) -> Unit,
    onDelete: (id: String) -> Unit
) {
    val config = moduleConfigs[module] ?: moduleConfigs["Leadership"]!!
    var name    by remember { mutableStateOf(existing?.name ?: "") }
    var purpose by remember { mutableStateOf(existing?.purpose ?: "") }
    var status  by remember { mutableStateOf(existing?.status ?: "Not Started") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val statusOptions = listOf("Not Started", "In Progress", "Fully Implemented")

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove System?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("\"${existing?.name}\" will be permanently removed from ${config.label}.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSec)) },
            confirmButton = {
                Button(onClick = { existing?.id?.let { onDelete(it) } },
                    colors = ButtonDefaults.buttonColors(containerColor = DestructiveRed),
                    shape = RoundedCornerShape(8.dp)) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = TextSec) }
            },
            shape = RoundedCornerShape(16.dp)
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                // ── Coloured header ───────────────────────────────────────────
                Surface(
                    color = config.color,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (existing == null) "Add System" else "Edit System",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White, fontWeight = FontWeight.Bold))
                            Text(config.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.7f)))
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.White,
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // ── Fields ────────────────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // System name
                    Column {
                        Text("SYSTEM OR APPLICATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = config.color, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))
                        Spacer(Modifier.height(5.dp))
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("e.g. Notion, Slack, QuickBooks",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = config.tint,
                                unfocusedBorderColor = if (name.isNotBlank()) BorderFilled else BorderEmpty,
                                focusedContainerColor = CardWhite, focusedBorderColor = config.color,
                                cursorColor = config.color))
                    }

                    // Purpose
                    Column {
                        Text("PURPOSE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = config.color, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))
                        Spacer(Modifier.height(5.dp))
                        OutlinedTextField(
                            value = purpose, onValueChange = { purpose = it },
                            modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4,
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("What does this system do for the business?",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = config.tint,
                                unfocusedBorderColor = if (purpose.isNotBlank()) BorderFilled else BorderEmpty,
                                focusedContainerColor = CardWhite, focusedBorderColor = config.color,
                                cursorColor = config.color))
                    }

                    // Status
                    Text("STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = config.color, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        statusOptions.forEach { option ->
                            val isSelected = status == option
                            val (dotColor, _) = when (option) {
                                "Fully Implemented" -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
                                "In Progress"       -> Color(0xFFF57C00) to Color(0xFFFFF3E0)
                                else                -> Color(0xFFBDBDBD) to Color(0xFFF5F5F5)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) config.tint else Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = { status = option },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = config.color, unselectedColor = TextMuted))
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                                    Spacer(Modifier.width(8.dp))
                                    Text(option, style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) config.color else TextSec,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal))
                                }
                            }
                        }
                    }

                    // Buttons
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (existing != null) {
                            OutlinedButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DestructiveRed)
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete")
                            }
                        } else {
                            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)) {
                                Text("Cancel", color = TextSec) }
                        }
                        Button(
                            onClick = { if (name.isNotBlank()) onSave(name.trim(), purpose.trim(), status) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Neutral)
                        ) { Text("Save System") }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}