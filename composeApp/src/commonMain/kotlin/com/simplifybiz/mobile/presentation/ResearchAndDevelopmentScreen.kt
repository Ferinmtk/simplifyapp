package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Science
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

private val R_Hero    = Color(0xFF00838F)
private val R_HeroEnd = Color(0xFF0097A7)
private val R_Tint    = Color(0xFFE0F7FA)
private val R_Neutral = Color(0xFF607D8B)
private val R_Card    = Color(0xFFFFFFFF)
private val R_Bg      = Color(0xFFF4F6F8)
private val R_Tp      = Color(0xFF1C1C1E)
private val R_Ts      = Color(0xFF6B7280)

class ResearchAndDevelopmentScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<ResearchAndDevelopmentViewModel>()
        val state by viewModel.uiState.collectAsState()
        val snackbar = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        var editing by remember { mutableStateOf<LeadershipSystemItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }
        LaunchedEffect(Unit) { viewModel.saveSuccess.collect { if (it) navigator?.pop() } }
        LaunchedEffect(Unit) { viewModel.validationMessage.collect { snackbar.showSnackbar(it) } }

        val filled = listOf(state.ideaCollectionDetails, state.innovationWorkshopsDetails, state.ideaEvaluationDetails,
            state.swotAnalysisDetails, state.prototypeDevDetails, state.testingPlanDetails,
            state.teamBudgetDetails, state.projectManagementToolsDetails, state.feedbackIntegrationDetails,
            state.iterationPlanDetails, state.roadmapDetails, state.departmentCoordinationDetails,
            state.innovationsDetails, state.ipDocsDetails).count { it.isNotBlank() }
        val total = 14

        if (showDialog) {
            RDSystemDialog(existing = editing, onDismiss = { showDialog = false; editing = null },
                onSave = { if (editing != null) viewModel.updateSystem(it) else viewModel.addSystem(it)
                    showDialog = false; editing = null })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("R&D", style = MaterialTheme.typography.titleMedium.copy(color = R_Tp, fontWeight = FontWeight.SemiBold)) },
                    navigationIcon = { IconButton(onClick = { navigator?.pop() }) { Icon(Icons.Default.ArrowBack, "Back", tint = R_Tp) } },
                    actions = { IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh", tint = R_Tp) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = R_Card))
            },
            snackbarHost = { SnackbarHost(snackbar) }, containerColor = R_Bg
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ModuleHero(R_Hero, R_HeroEnd, "Research & Development", filled, total, filled / total.toFloat(), isRefreshing,
                    icon = { Icon(Icons.Outlined.Science, null, tint = Color.White, modifier = it) })
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    GroupHeader("Ideation")
                    SectionCard(1, "Idea Collection", state.ideaCollectionDetails.isNotBlank()) {
                        SectionHint("How do you collect and capture new ideas from your team and customers?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.ideaCollectionDetails, viewModel::onIdeaCollectionChange, "Idea Collection", "How do you collect and capture new ideas from your team and customers?", R_Tint) }
                    SectionCard(2, "Innovation Workshops", state.innovationWorkshopsDetails.isNotBlank()) {
                        SectionHint("How do you run structured sessions to generate and develop ideas?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.innovationWorkshopsDetails, viewModel::onInnovationWorkshopsChange, "Innovation Workshops", "How do you run structured sessions to generate and develop ideas?", R_Tint) }
                    SectionCard(3, "Idea Evaluation", state.ideaEvaluationDetails.isNotBlank()) {
                        SectionHint("How do you assess and prioritise which ideas to pursue?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.ideaEvaluationDetails, viewModel::onIdeaEvaluationChange, "Idea Evaluation", "How do you assess and prioritise which ideas to pursue?", R_Tint) }
                    SectionCard(4, "SWOT Analysis", state.swotAnalysisDetails.isNotBlank()) {
                        SectionHint("How do you use SWOT analysis to evaluate innovation opportunities?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.swotAnalysisDetails, viewModel::onSwotAnalysisChange, "SWOT Analysis", "How do you use SWOT analysis to evaluate innovation opportunities?", R_Tint) }

                    GroupHeader("Development")
                    SectionCard(5, "Prototype Development", state.prototypeDevDetails.isNotBlank()) {
                        SectionHint("How do you build and test early versions of new ideas?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.prototypeDevDetails, viewModel::onPrototypeDevChange, "Prototype Development", "How do you build and test early versions of new ideas?", R_Tint) }
                    SectionCard(6, "Testing Plan", state.testingPlanDetails.isNotBlank()) {
                        SectionHint("How do you test new products or features before launch?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.testingPlanDetails, viewModel::onTestingPlanChange, "Testing Plan", "How do you test new products or features before launch?", R_Tint) }
                    SectionCard(7, "Feedback Integration", state.feedbackIntegrationDetails.isNotBlank()) {
                        SectionHint("How do you collect and integrate feedback into the development process?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.feedbackIntegrationDetails, viewModel::onFeedbackIntegrationChange, "Feedback Integration", "How do you collect and integrate feedback into the development process?", R_Tint) }
                    SectionCard(8, "Iteration Plan", state.iterationPlanDetails.isNotBlank()) {
                        SectionHint("How do you improve products or services based on learnings?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.iterationPlanDetails, viewModel::onIterationPlanChange, "Iteration Plan", "How do you improve products or services based on learnings?", R_Tint) }

                    GroupHeader("Resources")
                    SectionCard(9, "Team & Budget", state.teamBudgetDetails.isNotBlank()) {
                        SectionHint("What team and budget are allocated to R&D activities?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.teamBudgetDetails, viewModel::onTeamBudgetChange, "Team & Budget", "What team and budget are allocated to R&D activities?", R_Tint) }
                    SectionCard(10, "Project Management Tools", state.projectManagementToolsDetails.isNotBlank()) {
                        SectionHint("What tools do you use to manage R&D projects?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.projectManagementToolsDetails, viewModel::onProjectManagementToolsChange, "Project Management Tools", "What tools do you use to manage R&D projects?", R_Tint) }

                    GroupHeader("Roadmap")
                    SectionCard(11, "Roadmap", state.roadmapDetails.isNotBlank()) {
                        SectionHint("What is your innovation roadmap for the next 12 months?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.roadmapDetails, viewModel::onRoadmapChange, "Roadmap", "What is your innovation roadmap for the next 12 months?", R_Tint) }
                    SectionCard(12, "Department Coordination", state.departmentCoordinationDetails.isNotBlank()) {
                        SectionHint("How does R&D coordinate with other departments?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.departmentCoordinationDetails, viewModel::onDepartmentCoordinationChange, "Department Coordination", "How does R&D coordinate with other departments?", R_Tint) }

                    GroupHeader("Intellectual Property")
                    SectionCard(13, "Innovations", state.innovationsDetails.isNotBlank()) {
                        SectionHint("What innovations has your business developed or is developing?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.innovationsDetails, viewModel::onInnovationsChange, "Innovations", "What innovations has your business developed or is developing?", R_Tint) }
                    SectionCard(14, "IP Documentation", state.ipDocsDetails.isNotBlank()) {
                        SectionHint("How do you document and protect your intellectual property?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.ipDocsDetails, viewModel::onIpDocsChange, "IP Documentation", "How do you document and protect your intellectual property?", R_Tint) }

                    GroupHeader("Systems")
                    RDSystemsList(items = state.systemsUsed, tint = R_Tint,
                        onAdd = { showDialog = true; editing = null },
                        onEdit = { editing = it; showDialog = true },
                        onRemove = { viewModel.removeSystem(it) })

                    GroupHeader("Implementation Status")
                    ModuleStatusSection(state.statusQuoOfImplementation, viewModel::onStatusChange, R_Tint, R_Neutral)

                    Button(onClick = { viewModel.submitRD() },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = R_Neutral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp)); Text("Submit R&D Plan", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp) }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun RDSystemsList(
    items: List<LeadershipSystemItem>,
    tint: Color,
    onAdd: () -> Unit,
    onEdit: (LeadershipSystemItem) -> Unit,
    onRemove: (String) -> Unit
) {
    Column {
        if (items.isNotEmpty()) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    items.forEachIndexed { i, item ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.systemName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1C1E)))
                                if (item.purpose.isNotBlank()) Text(item.purpose, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280)), maxLines = 2) }
                            IconButton(onClick = { onEdit(item) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, null, tint = Color(0xFF607D8B), modifier = Modifier.size(15.dp)) }
                            IconButton(onClick = { onRemove(item.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350), modifier = Modifier.size(15.dp)) } }
                        if (i != items.lastIndex) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF607D8B))) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp)); Text("Add System", style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
private fun RDSystemDialog(existing: LeadershipSystemItem?, onDismiss: () -> Unit, onSave: (LeadershipSystemItem) -> Unit) {
    var name by remember { mutableStateOf(existing?.systemName ?: "") }
    var description by remember { mutableStateOf(existing?.purpose ?: "") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (existing == null) "Add System" else "Edit System", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp)) } }
                DialogField("System Name", name, { name = it }, R_Hero)
                DialogField("Purpose", description, { description = it }, R_Hero, minLines = 2)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Cancel", color = Color(0xFF6B7280)) }
                    Button(onClick = { if (name.isNotBlank()) onSave(LeadershipSystemItem(id = existing?.id ?: getTimeMillis().toString(), remoteId = existing?.remoteId, systemName = name.trim(), purpose = description.trim(), status = existing?.status ?: "Not Started")) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = R_Neutral)) { Text("Save") } }
            }
        }
    }
}