package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.simplifybiz.mobile.data.MoneySystemItem
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Palette ───────────────────────────────────────────────────────────────────
private val HeroColor     = Color(0xFF2E7D32)
private val HeroColorEnd  = Color(0xFF388E3C)
private val FieldTint     = Color(0xFFE8F5E9)
private val Neutral       = Color(0xFF607D8B)
private val CardWhite     = Color(0xFFFFFFFF)
private val PageBg        = Color(0xFFF4F6F8)
private val TextPrimary   = Color(0xFF1C1C1E)
private val TextSecondary = Color(0xFF6B7280)
private val TextMuted     = Color(0xFFBDBDBD)
private val DestructiveRed = Color(0xFFEF5350)

class MoneyScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<MoneyViewModel>()
        val state by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val uriHandler = LocalUriHandler.current
        val scope = rememberCoroutineScope()

        var showSystemDialog by remember { mutableStateOf(false) }
        var editingSystem by remember { mutableStateOf<MoneySystemItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }
        LaunchedEffect(Unit) { viewModel.saveSuccess.collect { if (it) navigator?.pop() } }
        LaunchedEffect(Unit) { viewModel.validationMessage.collect { snackbarHostState.showSnackbar(it) } }

        // Progress now counts 12 text sections (added Financial Reports).
        // File upload fields are NOT counted — the mobile user can't control
        // them, so it would be unfair to factor them into a completion score.
        val filled = listOf(
            state.revenueTrackingSystem, state.revenueMonitoringTools,
            state.expenseCategories, state.expenseApprovalWorkflow, state.costSavingStrategies,
            state.cashReservePlan, state.financialReports,
            state.stakeholderReporting, state.complianceRequirements, state.auditSchedule,
            state.reinvestmentCriteria, state.fundingOptions
        ).count { it.isNotBlank() }
        val total = 12
        val progress = filled / total.toFloat()

        if (showSystemDialog) {
            MoneySystemDialog(
                existingItem = editingSystem,
                onDismiss = { showSystemDialog = false; editingSystem = null },
                onSave = { item ->
                    if (editingSystem != null) viewModel.updateSystem(item)
                    else viewModel.addSystem(item)
                    showSystemDialog = false; editingSystem = null
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Money", style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary, fontWeight = FontWeight.SemiBold))
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = PageBg
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                ModuleHero(
                    color = HeroColor, colorEnd = HeroColorEnd,
                    title = "Financial Management",
                    sectionsDone = filled, total = total, progress = progress,
                    isRefreshing = isRefreshing,
                    icon = { Icon(Icons.Outlined.AttachMoney, null, tint = Color.White, modifier = it) }
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── DOCUMENTS ─────────────────────────────────────────────
                    // Read-only file references. Uploaded via the web form;
                    // tap to view in the system browser / PDF viewer.
                    GroupHeader("Documents")

                    DocumentsHint()

                    FileCard(
                        title = "Annual Budget",
                        url = state.annualBudgetUrl,
                        uriHandler = uriHandler,
                        onOpenFailed = { scope.launch { snackbarHostState.showSnackbar("Couldn't open file") } }
                    )
                    FileCard(
                        title = "Departmental Budgets",
                        url = state.departmentalBudgetUrl,
                        uriHandler = uriHandler,
                        onOpenFailed = { scope.launch { snackbarHostState.showSnackbar("Couldn't open file") } }
                    )
                    FileCard(
                        title = "Cash Flow Forecast",
                        url = state.cashFlowForecastUrl,
                        uriHandler = uriHandler,
                        onOpenFailed = { scope.launch { snackbarHostState.showSnackbar("Couldn't open file") } }
                    )

                    // ── REVENUE ───────────────────────────────────────────────
                    GroupHeader("Revenue")

                    SectionCard(1, "Revenue Tracking System", state.revenueTrackingSystem.isNotBlank()) {
                        SectionHint("How do you track incoming revenue? What system or process do you use?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.revenueTrackingSystem, viewModel::onRevenueTrackingSystemChange, "Revenue Tracking System", "How do you track incoming revenue? What system or process do you use?", FieldTint)
                    }
                    SectionCard(2, "Revenue Monitoring Tools", state.revenueMonitoringTools.isNotBlank()) {
                        SectionHint("What tools or dashboards do you use to monitor revenue performance?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.revenueMonitoringTools, viewModel::onRevenueMonitoringToolsChange, "Revenue Monitoring Tools", "What tools or dashboards do you use to monitor revenue performance?", FieldTint)
                    }

                    // ── EXPENSES ──────────────────────────────────────────────
                    GroupHeader("Expenses")

                    SectionCard(3, "Expense Categories", state.expenseCategories.isNotBlank()) {
                        SectionHint("What are the main categories of business expenditure?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.expenseCategories, viewModel::onExpenseCategoriesChange, "Expense Categories", "What are the main categories of business expenditure?", FieldTint)
                    }
                    SectionCard(4, "Expense Approval Workflow", state.expenseApprovalWorkflow.isNotBlank()) {
                        SectionHint("How are expenses approved? Who has authority at what thresholds?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.expenseApprovalWorkflow, viewModel::onExpenseApprovalWorkflowChange, "Expense Approval Workflow", "How are expenses approved? Who has authority at what thresholds?", FieldTint)
                    }
                    SectionCard(5, "Cost Saving Strategies", state.costSavingStrategies.isNotBlank()) {
                        SectionHint("What strategies do you use to reduce or control costs?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.costSavingStrategies, viewModel::onCostSavingStrategiesChange, "Cost Saving Strategies", "What strategies do you use to reduce or control costs?", FieldTint)
                    }

                    // ── CASH MANAGEMENT ───────────────────────────────────────
                    GroupHeader("Cash Management")

                    SectionCard(6, "Cash Reserve Plan", state.cashReservePlan.isNotBlank()) {
                        SectionHint("How much cash reserve do you maintain and how is it managed?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.cashReservePlan, viewModel::onCashReservePlanChange, "Cash Reserve Plan", "How much cash reserve do you maintain and how is it managed?", FieldTint)
                    }

                    // ── REPORTING & COMPLIANCE ────────────────────────────────
                    GroupHeader("Reporting & Compliance")

                    // Financial Reports is NEW — exists on the web form but was
                    // missing from mobile until this port. Placed first in this
                    // group because it's the most general reporting artifact.
                    SectionCard(7, "Financial Reports", state.financialReports.isNotBlank()) {
                        SectionHint("What financial reports do you produce internally to track performance?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.financialReports, viewModel::onFinancialReportsChange, "Financial Reports", "What financial reports do you produce internally to track performance?", FieldTint)
                    }
                    SectionCard(8, "Stakeholder Reporting", state.stakeholderReporting.isNotBlank()) {
                        SectionHint("What reports do you share with stakeholders and for whom?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.stakeholderReporting, viewModel::onStakeholderReportingChange, "Stakeholder Reporting", "What reports do you share with stakeholders and for whom?", FieldTint)
                    }
                    SectionCard(9, "Compliance Requirements", state.complianceRequirements.isNotBlank()) {
                        SectionHint("What financial regulations or compliance obligations apply to your business?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.complianceRequirements, viewModel::onComplianceRequirementsChange, "Compliance Requirements", "What financial regulations or compliance obligations apply to your business?", FieldTint)
                    }
                    SectionCard(10, "Audit Schedule", state.auditSchedule.isNotBlank()) {
                        SectionHint("How often do you conduct financial audits and who conducts them?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.auditSchedule, viewModel::onAuditScheduleChange, "Audit Schedule", "How often do you conduct financial audits and who conducts them?", FieldTint)
                    }

                    // ── GROWTH ────────────────────────────────────────────────
                    GroupHeader("Growth")

                    SectionCard(11, "Reinvestment Criteria", state.reinvestmentCriteria.isNotBlank()) {
                        SectionHint("What criteria determine when and how profits are reinvested?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.reinvestmentCriteria, viewModel::onReinvestmentCriteriaChange, "Reinvestment Criteria", "What criteria determine when and how profits are reinvested?", FieldTint)
                    }
                    SectionCard(12, "Funding Options", state.fundingOptions.isNotBlank()) {
                        SectionHint("What funding sources are available or being explored for growth?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.fundingOptions, viewModel::onFundingOptionsChange, "Funding Options", "What funding sources are available or being explored for growth?", FieldTint)
                    }

                    // ── SYSTEMS ───────────────────────────────────────────────
                    GroupHeader("Systems")

                    MoneySystemsSection(
                        items = state.systems,
                        onAdd = { showSystemDialog = true; editingSystem = null },
                        onEdit = { editingSystem = it; showSystemDialog = true },
                        onRemove = { viewModel.removeSystem(it) }
                    )

                    // ── STATUS ────────────────────────────────────────────────
                    GroupHeader("Implementation Status")

                    MoneyStatusSection(
                        selected = state.statusQuoOfImplementation,
                        onSelected = viewModel::onStatusChange
                    )

                    // ── SUBMIT ────────────────────────────────────────────────
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.submitMoney() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Neutral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Submit Money Plan", fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

// ── Documents hint ────────────────────────────────────────────────────────────
@Composable
private fun DocumentsHint() {
    Text(
        "Files uploaded via the web. Tap to view.",
        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
        modifier = Modifier.padding(start = 4.dp)
    )
}

// ── File Card ─────────────────────────────────────────────────────────────────
@Composable
private fun FileCard(
    title: String,
    url: String,
    uriHandler: UriHandler,
    onOpenFailed: () -> Unit
) {
    val hasFile = url.isNotBlank()
    val filename = if (hasFile) url.substringAfterLast('/').substringBefore('?').ifBlank { "Document" } else ""

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (hasFile) it.clickable {
                    runCatching { uriHandler.openUri(url) }.onFailure { _ -> onOpenFailed() }
                } else it
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon badge — filled green when a file exists, muted cloud-off when not
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (hasFile) FieldTint else Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (hasFile) Icons.Outlined.Description else Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = if (hasFile) HeroColor else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (hasFile) filename else "No file — upload from web",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (hasFile) TextSecondary else TextMuted
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (hasFile) {
                Icon(
                    Icons.Outlined.OpenInNew,
                    contentDescription = "Open",
                    tint = HeroColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Systems Section ───────────────────────────────────────────────────────────
@Composable
private fun MoneySystemsSection(
    items: List<MoneySystemItem>,
    onAdd: () -> Unit,
    onEdit: (MoneySystemItem) -> Unit,
    onRemove: (String) -> Unit
) {
    Column {
        if (items.isNotEmpty()) {
            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    items.forEachIndexed { index, item ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.systemOrApplication, style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold, color = TextPrimary))
                                if (item.purpose.isNotBlank()) {
                                    Text(item.purpose, style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                if (item.status.isNotBlank() && item.status != "Not Started") {
                                    Spacer(Modifier.height(4.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = FieldTint) {
                                        Text(item.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = HeroColor, fontWeight = FontWeight.Medium))
                                    }
                                }
                            }
                            IconButton(onClick = { onEdit(item) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, null, tint = Neutral, modifier = Modifier.size(15.dp))
                            }
                            IconButton(onClick = { onRemove(item.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = DestructiveRed, modifier = Modifier.size(15.dp))
                            }
                        }
                        if (index != items.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Neutral)) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add System", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ── Status Section ────────────────────────────────────────────────────────────
@Composable
private fun MoneyStatusSection(selected: String, onSelected: (String) -> Unit) {
    val options = listOf("Not Started", "In Progress", "Fully Implemented")
    Column {
        options.forEach { option ->
            val isSelected = selected == option
            Surface(shape = RoundedCornerShape(8.dp),
                color = if (isSelected) FieldTint else Color.Transparent,
                modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isSelected, onClick = { onSelected(option) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Neutral, unselectedColor = TextMuted))
                    Text(option, style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isSelected) Neutral else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal))
                }
            }
        }
    }
}

// ── System Dialog ─────────────────────────────────────────────────────────────
@Composable
private fun MoneySystemDialog(
    existingItem: MoneySystemItem?,
    onDismiss: () -> Unit,
    onSave: (MoneySystemItem) -> Unit
) {
    var systemName by remember { mutableStateOf(existingItem?.systemOrApplication ?: "") }
    var purpose by remember { mutableStateOf(existingItem?.purpose ?: "") }
    var status by remember { mutableStateOf(existingItem?.status ?: "Not Started") }
    val statusOptions = listOf("Not Started", "In Progress", "Fully Implemented")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (existingItem == null) "Add System" else "Edit System",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
                DialogField("System or Application", systemName, { systemName = it }, HeroColor)
                DialogField("Purpose", purpose, { purpose = it }, HeroColor, minLines = 2)
                Text("STATUS", style = MaterialTheme.typography.labelSmall.copy(
                    color = HeroColor, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    statusOptions.forEach { option ->
                        val isSelected = status == option
                        Surface(shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FieldTint else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = isSelected, onClick = { status = option },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Neutral, unselectedColor = TextMuted))
                                Text(option, style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) Neutral else TextSecondary))
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)) { Text("Cancel", color = TextSecondary) }
                    Button(onClick = {
                        if (systemName.isNotBlank()) {
                            onSave(MoneySystemItem(
                                id = existingItem?.id ?: getTimeMillis().toString(),
                                remoteId = existingItem?.remoteId,
                                systemOrApplication = systemName.trim(),
                                purpose = purpose.trim(), status = status))
                        }
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Neutral)) { Text("Save") }
                }
            }
        }
    }
}