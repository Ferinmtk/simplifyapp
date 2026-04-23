package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Handshake
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val S_Hero    = Color(0xFFB71C1C)
private val S_HeroEnd = Color(0xFFD32F2F)
private val S_Tint    = Color(0xFFFFEBEE)
private val S_Neutral = Color(0xFF607D8B)
private val S_Card    = Color(0xFFFFFFFF)
private val S_Bg      = Color(0xFFF4F6F8)
private val S_Tp      = Color(0xFF1C1C1E)

class SalesScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<SalesViewModel>()
        val state by viewModel.uiState.collectAsState()
        val snackbar = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        var editing by remember { mutableStateOf<LeadershipSystemItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }
        LaunchedEffect(Unit) { viewModel.saveSuccess.collect { if (it) navigator?.pop() } }
        LaunchedEffect(Unit) { viewModel.validationMessage.collect { snackbar.showSnackbar(it) } }

        val filled = listOf(state.salesFunnelStages, state.leadQualificationCriteria, state.salesChannels,
            state.elevatorPitch, state.businessPurpose, state.salesTeamStructure, state.salesGoals,
            state.salesTrainingPlan, state.salesMetrics, state.priceList, state.objectionProcess).count { it.isNotBlank() }
        val total = 11

        if (showDialog) {
            LSystemDialog(existing = editing, accentColor = S_Hero, tint = S_Tint,
                onDismiss = { showDialog = false; editing = null },
                onSave = { if (editing != null) viewModel.updateSystem(it) else viewModel.addSystem(it)
                    showDialog = false; editing = null })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Sales Process", style = MaterialTheme.typography.titleMedium.copy(color = S_Tp, fontWeight = FontWeight.SemiBold)) },
                    navigationIcon = { IconButton(onClick = { navigator?.pop() }) { Icon(Icons.Default.ArrowBack, "Back", tint = S_Tp) } },
                    actions = { IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh", tint = S_Tp) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = S_Card))
            },
            snackbarHost = { SnackbarHost(snackbar) }, containerColor = S_Bg
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ModuleHero(S_Hero, S_HeroEnd, "Sales Process", filled, total, filled / total.toFloat(), isRefreshing,
                    icon = { Icon(Icons.Outlined.Handshake, null, tint = Color.White, modifier = it) })
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GroupHeader("Funnel")
                    SectionCard(1, "Sales Funnel", state.salesFunnelStages.isNotBlank()) {
                        SectionHint("Describe each stage from first contact to paid customer.")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.salesFunnelStages, viewModel::onFunnelChange, "Sales Funnel", "Describe each stage from first contact to paid customer.", S_Tint) }
                    SectionCard(2, "Lead Qualification", state.leadQualificationCriteria.isNotBlank()) {
                        SectionHint("How do you decide a lead is worth pursuing?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.leadQualificationCriteria, viewModel::onQualificationChange, "Lead Qualification", "How do you decide a lead is worth pursuing?", S_Tint) }
                    SectionCard(3, "Sales Channels", state.salesChannels.isNotBlank()) {
                        SectionHint("Where do leads come from? Online, referrals, cold outreach?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.salesChannels, viewModel::onChannelsChange, "Sales Channels", "Where do leads come from? Online, referrals, cold outreach?", S_Tint) }

                    GroupHeader("Messaging")
                    SectionCard(4, "Elevator Pitch", state.elevatorPitch.isNotBlank()) {
                        SectionHint("A short clear explanation of what you sell and why it matters.")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.elevatorPitch, viewModel::onPitchChange, "Elevator Pitch", "A short clear explanation of what you sell and why it matters.", S_Tint) }
                    SectionCard(5, "Business Purpose", state.businessPurpose.isNotBlank()) {
                        SectionHint("How does your sales approach connect to your business purpose?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.businessPurpose, viewModel::onBusinessPurposeChange, "Business Purpose", "How does your sales approach connect to your business purpose?", S_Tint) }

                    GroupHeader("Team")
                    SectionCard(6, "Sales Team Structure", state.salesTeamStructure.isNotBlank()) {
                        SectionHint("Who sells, who follows up, who closes?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.salesTeamStructure, viewModel::onTeamStructureChange, "Sales Team Structure", "Who sells, who follows up, who closes?", S_Tint) }
                    SectionCard(7, "Sales Goals", state.salesGoals.isNotBlank()) {
                        SectionHint("What are your targets — revenue, deals, conversion rate?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.salesGoals, viewModel::onGoalsChange, "Sales Goals", "What are your targets — revenue, deals, conversion rate?", S_Tint) }
                    SectionCard(8, "Sales Training Plan", state.salesTrainingPlan.isNotBlank()) {
                        SectionHint("How do you train yourself or the sales team?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.salesTrainingPlan, viewModel::onTrainingPlanChange, "Sales Training Plan", "How do you train yourself or the sales team?", S_Tint) }

                    GroupHeader("Performance")
                    SectionCard(9, "Sales Metrics", state.salesMetrics.isNotBlank()) {
                        SectionHint("What numbers do you track weekly to measure sales performance?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.salesMetrics, viewModel::onMetricsChange, "Sales Metrics", "What numbers do you track weekly to measure sales performance?", S_Tint) }
                    SectionCard(10, "Price List", state.priceList.isNotBlank()) {
                        SectionHint("What do you charge and what is included at each price point?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.priceList, viewModel::onPriceListChange, "Price List", "What do you charge and what is included at each price point?", S_Tint) }
                    SectionCard(11, "Objection Handling", state.objectionProcess.isNotBlank()) {
                        SectionHint("How do you respond to common objections from prospects?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.objectionProcess, viewModel::onObjectionProcessChange, "Objection Handling", "How do you respond to common objections from prospects?", S_Tint) }

                    GroupHeader("Systems")
                    GenericSystemsList(items = state.systemsUsed.map { Triple(it.id, it.systemName, it.purpose) },
                        tint = S_Tint, accentColor = S_Hero,
                        onAdd = { showDialog = true; editing = null },
                        onEdit = { id -> editing = state.systemsUsed.find { it.id == id }; showDialog = true },
                        onRemove = { viewModel.removeSystem(it) })

                    GroupHeader("Implementation Status")
                    ModuleStatusSection(state.implementationStatus, viewModel::onImplementationStatusChange, S_Tint, S_Neutral)

                    Button(onClick = { viewModel.submitSales() },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = S_Neutral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp)); Text("Submit Sales Process", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp) }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}