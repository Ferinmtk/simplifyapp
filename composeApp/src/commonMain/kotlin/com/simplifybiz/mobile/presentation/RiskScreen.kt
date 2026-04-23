package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Security
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
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.clickable
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val RK_Hero    = Color(0xFF1565C0)
private val RK_HeroEnd = Color(0xFF1976D2)
private val RK_Tint    = Color(0xFFE3F2FD)
private val RK_Neutral = Color(0xFF607D8B)
private val RK_Card    = Color(0xFFFFFFFF)
private val RK_Bg      = Color(0xFFF4F6F8)
private val RK_Tp      = Color(0xFF1C1C1E)
private val RK_Ts      = Color(0xFF6B7280)

class RiskScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<RiskViewModel>()
        val state by viewModel.uiState.collectAsState()
        val snackbar = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        var editing by remember { mutableStateOf<LeadershipSystemItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }
        LaunchedEffect(Unit) { viewModel.saveSuccess.collect { if (it) navigator?.pop() } }
        LaunchedEffect(Unit) { viewModel.validationMessage.collect { snackbar.showSnackbar(it) } }

        val filled = listOf(state.businessStructure, state.businessRegistration, state.complianceRequirements,
            state.complianceMonitoring, state.contractTemplates, state.contractReviewProcess,
            state.legalCounsel, state.intellectualPropertyStrategy, state.ipRegistrationPlan,
            state.dataPrivacyPolicies, state.employeeLegalAgreements, state.riskAssessment,
            state.riskMitigationPlan, state.disputeResolutionProcess).count { it.isNotBlank() }
        val total = 14

        if (showDialog) {
            RKSystemDialog(existing = editing, onDismiss = { showDialog = false; editing = null },
                onSave = { if (editing != null) viewModel.updateSystem(it) else viewModel.addSystem(it)
                    showDialog = false; editing = null })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Risk & Legal", style = MaterialTheme.typography.titleMedium.copy(color = RK_Tp, fontWeight = FontWeight.SemiBold)) },
                    navigationIcon = { IconButton(onClick = { navigator?.pop() }) { Icon(Icons.Default.ArrowBack, "Back", tint = RK_Tp) } },
                    actions = { IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh", tint = RK_Tp) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = RK_Card))
            },
            snackbarHost = { SnackbarHost(snackbar) }, containerColor = RK_Bg
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ModuleHero(RK_Hero, RK_HeroEnd, "Risk & Legal", filled, total, filled / total.toFloat(), isRefreshing,
                    icon = { Icon(Icons.Outlined.Security, null, tint = Color.White, modifier = it) })
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    GroupHeader("Legal Structure")
                    SectionCard(1, "Business Structure", state.businessStructure.isNotBlank()) {
                        SectionHint("What legal structure does your business operate under?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.businessStructure, viewModel::onBusinessStructureChange, "Business Structure", "What legal structure does your business operate under?", RK_Tint) }
                    SectionCard(2, "Business Registration", state.businessRegistration.isNotBlank()) {
                        SectionHint("Where and how is your business registered?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.businessRegistration, viewModel::onBusinessRegistrationChange, "Business Registration", "Where and how is your business registered?", RK_Tint) }

                    GroupHeader("Compliance")
                    SectionCard(3, "Compliance Requirements", state.complianceRequirements.isNotBlank()) {
                        SectionHint("What legal and regulatory requirements apply to your business?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.complianceRequirements, viewModel::onComplianceRequirementsChange, "Compliance Requirements", "What legal and regulatory requirements apply to your business?", RK_Tint) }
                    SectionCard(4, "Compliance Monitoring", state.complianceMonitoring.isNotBlank()) {
                        SectionHint("How do you ensure ongoing compliance with regulations?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.complianceMonitoring, viewModel::onComplianceMonitoringChange, "Compliance Monitoring", "How do you ensure ongoing compliance with regulations?", RK_Tint) }

                    GroupHeader("Contracts")
                    SectionCard(5, "Contract Templates", state.contractTemplates.isNotBlank()) {
                        SectionHint("What standard contracts or agreements does your business use?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.contractTemplates, viewModel::onContractTemplatesChange, "Contract Templates", "What standard contracts or agreements does your business use?", RK_Tint) }
                    SectionCard(6, "Contract Review Process", state.contractReviewProcess.isNotBlank()) {
                        SectionHint("How do you review and approve contracts before signing?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.contractReviewProcess, viewModel::onContractReviewProcessChange, "Contract Review Process", "How do you review and approve contracts before signing?", RK_Tint) }
                    SectionCard(7, "Legal Counsel", state.legalCounsel.isNotBlank()) {
                        SectionHint("Who provides legal advice and support to your business?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.legalCounsel, viewModel::onLegalCounselChange, "Legal Counsel", "Who provides legal advice and support to your business?", RK_Tint) }

                    GroupHeader("Intellectual Property")
                    SectionCard(8, "IP Strategy", state.intellectualPropertyStrategy.isNotBlank()) {
                        SectionHint("How do you protect your intellectual property and innovations?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.intellectualPropertyStrategy, viewModel::onIntellectualPropertyStrategyChange, "IP Strategy", "How do you protect your intellectual property and innovations?", RK_Tint) }
                    SectionCard(9, "IP Registration Plan", state.ipRegistrationPlan.isNotBlank()) {
                        SectionHint("What IP assets will you register and how?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.ipRegistrationPlan, viewModel::onIpRegistrationPlanChange, "IP Registration Plan", "What IP assets will you register and how?", RK_Tint) }

                    GroupHeader("Data & People")
                    SectionCard(10, "Data Privacy Policies", state.dataPrivacyPolicies.isNotBlank()) {
                        SectionHint("How do you handle and protect customer and employee data?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.dataPrivacyPolicies, viewModel::onDataPrivacyPoliciesChange, "Data Privacy Policies", "How do you handle and protect customer and employee data?", RK_Tint) }
                    SectionCard(11, "Employee Legal Agreements", state.employeeLegalAgreements.isNotBlank()) {
                        SectionHint("What legal agreements do employees sign? NDAs, contracts, IP assignments?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.employeeLegalAgreements, viewModel::onEmployeeLegalAgreementsChange, "Employee Legal Agreements", "What legal agreements do employees sign?", RK_Tint) }

                    GroupHeader("Risk Management")
                    SectionCard(12, "Risk Assessment", state.riskAssessment.isNotBlank()) {
                        SectionHint("What are the key risks facing your business and their likelihood?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.riskAssessment, viewModel::onRiskAssessmentChange, "Risk Assessment", "What are the key risks facing your business and their likelihood?", RK_Tint) }
                    SectionCard(13, "Risk Mitigation Plan", state.riskMitigationPlan.isNotBlank()) {
                        SectionHint("How do you reduce or manage each identified risk?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.riskMitigationPlan, viewModel::onRiskMitigationPlanChange, "Risk Mitigation Plan", "How do you reduce or manage each identified risk?", RK_Tint) }
                    SectionCard(14, "Dispute Resolution Process", state.disputeResolutionProcess.isNotBlank()) {
                        SectionHint("How do you handle disputes with customers, suppliers, or employees?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.disputeResolutionProcess, viewModel::onDisputeResolutionProcessChange, "Dispute Resolution", "How do you handle disputes with customers, suppliers, or employees?", RK_Tint) }

                    GroupHeader("Systems")
                    RDSystemsList(items = state.systemsUsed, tint = RK_Tint,
                        onAdd = { showDialog = true; editing = null },
                        onEdit = { editing = it; showDialog = true },
                        onRemove = { viewModel.removeSystem(it) })

                    GroupHeader("Implementation Status")
                    ModuleStatusSection(state.statusQuoOfImplementation, viewModel::onStatusChange, RK_Tint, RK_Neutral)

                    Button(onClick = { viewModel.submitRisk() },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RK_Neutral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp)); Text("Submit Risk & Legal", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp) }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun RKSystemDialog(existing: LeadershipSystemItem?, onDismiss: () -> Unit, onSave: (LeadershipSystemItem) -> Unit) {
    var name by remember { mutableStateOf(existing?.systemName ?: "") }
    var description by remember { mutableStateOf(existing?.purpose ?: "") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (existing == null) "Add System" else "Edit System", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = RK_Ts, modifier = Modifier.size(18.dp)) } }
                DialogField("System Name", name, { name = it }, RK_Hero)
                DialogField("Purpose", description, { description = it }, RK_Hero, minLines = 2)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Cancel", color = RK_Ts) }
                    Button(onClick = { if (name.isNotBlank()) onSave(LeadershipSystemItem(id = existing?.id ?: getTimeMillis().toString(), remoteId = existing?.remoteId, systemName = name.trim(), purpose = description.trim(), status = existing?.status ?: "Not Started")) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = RK_Neutral)) { Text("Save") } }
            }
        }
    }
}