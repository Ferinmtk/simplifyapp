package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Engineering
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
import com.simplifybiz.mobile.data.LeadershipSystemItem
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.clickable
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

private val O_Hero    = Color(0xFFE65100)
private val O_HeroEnd = Color(0xFFF57C00)
private val O_Tint    = Color(0xFFFBE9E7)
private val O_Neutral = Color(0xFF607D8B)
private val O_Card    = Color(0xFFFFFFFF)
private val O_Bg      = Color(0xFFF4F6F8)
private val O_Tp      = Color(0xFF1C1C1E)

class OperationsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<OperationsViewModel>()
        val state by viewModel.uiState.collectAsState()
        val snackbar = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        var editing by remember { mutableStateOf<LeadershipSystemItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }
        LaunchedEffect(Unit) { viewModel.saveSuccess.collect { if (it) navigator?.pop() } }
        LaunchedEffect(Unit) { viewModel.validationMessage.collect { snackbar.showSnackbar(it) } }

        val filled = listOf(state.workflowDescriptions, state.keyActivitiesDependencies, state.requiredResources,
            state.inventoryManagementProtocols, state.qualityStandards, state.qualityControlMethods,
            state.standardOperatingProcedures, state.troubleshootingGuide, state.performanceMetrics,
            state.monitoringTools, state.scalabilitySteps, state.automationOutsourcingPlan,
            state.operationalRisk, state.contingencyPlans).count { it.isNotBlank() }
        val total = 14

        if (showDialog) {
            LSystemDialog(existing = editing, accentColor = O_Hero, tint = O_Tint,
                onDismiss = { showDialog = false; editing = null },
                onSave = { if (editing != null) viewModel.updateSystem(it) else viewModel.addSystem(it)
                    showDialog = false; editing = null })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Operations", style = MaterialTheme.typography.titleMedium.copy(color = O_Tp, fontWeight = FontWeight.SemiBold)) },
                    navigationIcon = { IconButton(onClick = { navigator?.pop() }) { Icon(Icons.Default.ArrowBack, "Back", tint = O_Tp) } },
                    actions = { IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh", tint = O_Tp) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = O_Card))
            },
            snackbarHost = { SnackbarHost(snackbar) }, containerColor = O_Bg
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ModuleHero(O_Hero, O_HeroEnd, "Operations Process", filled, total, filled / total.toFloat(), isRefreshing,
                    icon = { Icon(Icons.Outlined.Engineering, null, tint = Color.White, modifier = it) })
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    GroupHeader("Workflow")
                    SectionCard(1, "Workflow Descriptions", state.workflowDescriptions.isNotBlank()) {
                        SectionHint("Describe your core business workflows and how work flows through the business.")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.workflowDescriptions, viewModel::onWorkflowDescriptionsChange, "Workflow Descriptions", "Describe your core business workflows and how work flows through the business.", O_Tint) }
                    SectionCard(2, "Key Activities & Dependencies", state.keyActivitiesDependencies.isNotBlank()) {
                        SectionHint("What are the critical activities and what do they depend on?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.keyActivitiesDependencies, viewModel::onKeyActivitiesChange, "Key Activities & Dependencies", "What are the critical activities and what do they depend on?", O_Tint) }
                    SectionCard(3, "Required Resources", state.requiredResources.isNotBlank()) {
                        SectionHint("What people, tools, and resources are required to run operations?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.requiredResources, viewModel::onRequiredResourcesChange, "Required Resources", "What people, tools, and resources are required to run operations?", O_Tint) }
                    SectionCard(4, "Inventory Management", state.inventoryManagementProtocols.isNotBlank()) {
                        SectionHint("How do you manage stock, supplies, or assets?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.inventoryManagementProtocols, viewModel::onInventoryManagementChange, "Inventory Management", "How do you manage stock, supplies, or assets?", O_Tint) }

                    GroupHeader("Quality")
                    SectionCard(5, "Quality Standards", state.qualityStandards.isNotBlank()) {
                        SectionHint("What standards define quality in your products or services?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.qualityStandards, viewModel::onQualityStandardsChange, "Quality Standards", "What standards define quality in your products or services?", O_Tint) }
                    SectionCard(6, "Quality Control Methods", state.qualityControlMethods.isNotBlank()) {
                        SectionHint("How do you check and ensure quality is maintained?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.qualityControlMethods, viewModel::onQualityControlMethodsChange, "Quality Control Methods", "How do you check and ensure quality is maintained?", O_Tint) }
                    SectionCard(7, "Standard Operating Procedures", state.standardOperatingProcedures.isNotBlank()) {
                        SectionHint("What documented SOPs guide day-to-day operations?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.standardOperatingProcedures, viewModel::onStandardOperatingProceduresChange, "Standard Operating Procedures", "What documented SOPs guide day-to-day operations?", O_Tint) }
                    SectionCard(8, "Troubleshooting Guide", state.troubleshootingGuide.isNotBlank()) {
                        SectionHint("How do you handle operational issues when they arise?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.troubleshootingGuide, viewModel::onTroubleshootingGuideChange, "Troubleshooting Guide", "How do you handle operational issues when they arise?", O_Tint) }

                    GroupHeader("Performance")
                    SectionCard(9, "Performance Metrics", state.performanceMetrics.isNotBlank()) {
                        SectionHint("What KPIs measure operational efficiency and effectiveness?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.performanceMetrics, viewModel::onPerformanceMetricsChange, "Performance Metrics", "What KPIs measure operational efficiency and effectiveness?", O_Tint) }
                    SectionCard(10, "Monitoring Tools", state.monitoringTools.isNotBlank()) {
                        SectionHint("What tools or dashboards do you use to monitor operations?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.monitoringTools, viewModel::onMonitoringToolsChange, "Monitoring Tools", "What tools or dashboards do you use to monitor operations?", O_Tint) }

                    GroupHeader("Growth")
                    SectionCard(11, "Scalability Steps", state.scalabilitySteps.isNotBlank()) {
                        SectionHint("How will operations scale as the business grows?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.scalabilitySteps, viewModel::onScalabilityStepsChange, "Scalability Steps", "How will operations scale as the business grows?", O_Tint) }
                    SectionCard(12, "Automation & Outsourcing Plan", state.automationOutsourcingPlan.isNotBlank()) {
                        SectionHint("What will you automate or outsource to improve efficiency?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.automationOutsourcingPlan, viewModel::onAutomationOutsourcingPlanChange, "Automation & Outsourcing", "What will you automate or outsource to improve efficiency?", O_Tint) }

                    GroupHeader("Risk")
                    SectionCard(13, "Operational Risk", state.operationalRisk.isNotBlank()) {
                        SectionHint("What are the main operational risks to the business?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.operationalRisk, viewModel::onOperationalRiskChange, "Operational Risk", "What are the main operational risks to the business?", O_Tint) }
                    SectionCard(14, "Contingency Plans", state.contingencyPlans.isNotBlank()) {
                        SectionHint("What plans exist for when things go wrong?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.contingencyPlans, viewModel::onContingencyPlansChange, "Contingency Plans", "What plans exist for when things go wrong?", O_Tint) }

                    GroupHeader("Systems")
                    GenericSystemsList(items = state.systems.map { Triple(it.id, it.systemName, it.purpose) },
                        tint = O_Tint, accentColor = O_Hero,
                        onAdd = { showDialog = true; editing = null },
                        onEdit = { id -> editing = state.systems.find { it.id == id }; showDialog = true },
                        onRemove = { viewModel.removeSystem(it) })

                    GroupHeader("Implementation Status")
                    ModuleStatusSection(state.statusQuoOfImplementation, viewModel::onStatusChange, O_Tint, O_Neutral)

                    Button(onClick = { viewModel.submitOperations() },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = O_Neutral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp)); Text("Submit Operations", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp) }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}