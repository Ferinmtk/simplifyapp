package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Campaign
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
import com.simplifybiz.mobile.data.MarketingSystemItem
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

private val M_Hero    = Color(0xFFAD1457)
private val M_HeroEnd = Color(0xFFD81B60)
private val M_Tint    = Color(0xFFFCE4EC)
private val M_Neutral = Color(0xFF607D8B)
private val M_Card    = Color(0xFFFFFFFF)
private val M_Bg      = Color(0xFFF4F6F8)
private val M_Tp      = Color(0xFF1C1C1E)
private val M_Ts      = Color(0xFF6B7280)

class MarketingScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<MarketingViewModel>()
        val state by viewModel.uiState.collectAsState()
        val snackbar = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        var editing by remember { mutableStateOf<MarketingSystemItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }
        LaunchedEffect(Unit) { viewModel.saveSuccess.collect { if (it) navigator?.pop() } }
        LaunchedEffect(Unit) { viewModel.validationMessage.collect { snackbar.showSnackbar(it) } }

        val filled = listOf(state.marketingObjectives, state.marketingChannelsRationale, state.marketingBudget,
            state.brandCoreMessage, state.brandTone, state.contentTypes, state.successMetricsKeyMetrics,
            state.ownersTimeCommitment, state.ownersSkills, state.ownersOutsourcingNeeds).count { it.isNotBlank() }
        val total = 10

        if (showDialog) {
            MSystemDialog(existing = editing, onDismiss = { showDialog = false; editing = null },
                onSave = { if (editing != null) viewModel.updateSystem(it) else viewModel.addSystem(it.systemOrApplication, it.purpose, it.status)
                    showDialog = false; editing = null })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Marketing", style = MaterialTheme.typography.titleMedium.copy(color = M_Tp, fontWeight = FontWeight.SemiBold)) },
                    navigationIcon = { IconButton(onClick = { navigator?.pop() }) { Icon(Icons.Default.ArrowBack, "Back", tint = M_Tp) } },
                    actions = { IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh", tint = M_Tp) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = M_Card))
            },
            snackbarHost = { SnackbarHost(snackbar) }, containerColor = M_Bg
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ModuleHero(M_Hero, M_HeroEnd, "Marketing Process", filled, total, filled / total.toFloat(), isRefreshing,
                    icon = { Icon(Icons.Outlined.Campaign, null, tint = Color.White, modifier = it) })
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GroupHeader("Objectives")
                    SectionCard(1, "Marketing Objectives", state.marketingObjectives.isNotBlank()) {
                        SectionHint("What specific goals do you want your marketing to achieve?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.marketingObjectives, viewModel::onMarketingObjectivesChange, "Marketing Objectives", "What specific goals do you want your marketing to achieve?", M_Tint) }
                    SectionCard(2, "Channels & Rationale", state.marketingChannelsRationale.isNotBlank()) {
                        SectionHint("Which platforms will you use and why are they right for your audience?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.marketingChannelsRationale, viewModel::onMarketingChannelsRationaleChange, "Channels & Rationale", "Which platforms will you use and why are they right for your audience?", M_Tint) }

                    GroupHeader("Budget")
                    SectionCard(3, "Budget", state.marketingBudget.isNotBlank()) {
                        SectionHint("Set your total marketing budget and break it down by cost type.")
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = state.marketingBudget, onValueChange = viewModel::onMarketingBudgetChange,
                            label = { Text("Total Budget", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                            shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = M_Tint, unfocusedBorderColor = if (state.marketingBudget.isNotBlank()) Color(0xFF1C1C1E) else Color(0xFFE0E0E0),
                                focusedContainerColor = M_Card, focusedBorderColor = Color(0xFF1C1C1E), cursorColor = M_Neutral))
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = state.oneOffCost, onValueChange = viewModel::onOneOffCostChange,
                                label = { Text("One-off Cost", fontSize = 11.sp) }, modifier = Modifier.weight(1f), singleLine = true,
                                shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = M_Tint, unfocusedBorderColor = if (state.oneOffCost.isNotBlank()) Color(0xFF1C1C1E) else Color(0xFFE0E0E0),
                                    focusedContainerColor = M_Card, focusedBorderColor = Color(0xFF1C1C1E), cursorColor = M_Neutral))
                            OutlinedTextField(value = state.recurringCost, onValueChange = viewModel::onRecurringCostChange,
                                label = { Text("Recurring Cost", fontSize = 11.sp) }, modifier = Modifier.weight(1f), singleLine = true,
                                shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = M_Tint, unfocusedBorderColor = if (state.recurringCost.isNotBlank()) Color(0xFF1C1C1E) else Color(0xFFE0E0E0),
                                    focusedContainerColor = M_Card, focusedBorderColor = Color(0xFF1C1C1E), cursorColor = M_Neutral)) }
                        Spacer(Modifier.height(10.dp))
                        Text("FREQUENCY", style = MaterialTheme.typography.labelSmall.copy(color = M_Hero, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Daily","Weekly","Monthly").forEach { opt ->
                                val isSel = state.budgetFrequency == opt
                                Surface(shape = RoundedCornerShape(20.dp), color = if (isSel) M_Hero else M_Card,
                                    modifier = Modifier.weight(1f)) {
                                    TextButton(onClick = { viewModel.onBudgetFrequencyChange(opt) }, modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.textButtonColors(contentColor = if (isSel) Color.White else M_Ts)) {
                                        Text(opt, style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp)) } } } } }

                    GroupHeader("Brand")
                    SectionCard(4, "Core Message", state.brandCoreMessage.isNotBlank()) {
                        SectionHint("What is the single clearest thing you want your audience to understand?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.brandCoreMessage, viewModel::onBrandCoreMessageChange, "Core Message", "What is the single clearest thing you want your audience to understand?", M_Tint) }
                    SectionCard(5, "Brand Tone", state.brandTone.isNotBlank()) {
                        SectionHint("How does your brand communicate? Formal, friendly, bold, calm?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.brandTone, viewModel::onBrandToneChange, "Brand Tone", "How does your brand communicate? Formal, friendly, bold, calm?", M_Tint) }
                    SectionCard(6, "Content Plan", state.contentTypes.isNotBlank()) {
                        SectionHint("What types of content will you create — blogs, videos, social posts?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.contentTypes, viewModel::onContentTypesChange, "Content Plan", "What types of content will you create — blogs, videos, social posts?", M_Tint) }

                    GroupHeader("Performance")
                    SectionCard(7, "Success Metrics", state.successMetricsKeyMetrics.isNotBlank()) {
                        SectionHint("How will you know if your marketing is working? What will you track?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.successMetricsKeyMetrics, viewModel::onSuccessMetricsChange, "Success Metrics", "How will you know if your marketing is working? What will you track?", M_Tint) }

                    GroupHeader("Resources")
                    SectionCard(8, "Owner's Time Commitment", state.ownersTimeCommitment.isNotBlank()) {
                        SectionHint("How many hours per week will you dedicate to marketing?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.ownersTimeCommitment, viewModel::onOwnersTimeCommitmentChange, "Owner's Time Commitment", "How many hours per week will you dedicate to marketing?", M_Tint) }
                    SectionCard(9, "Owner's Skills", state.ownersSkills.isNotBlank()) {
                        SectionHint("What marketing skills do you already have?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.ownersSkills, viewModel::onOwnersSkillsChange, "Owner's Skills", "What marketing skills do you already have?", M_Tint) }
                    SectionCard(10, "Outsourcing Needs", state.ownersOutsourcingNeeds.isNotBlank()) {
                        SectionHint("What marketing activities will you outsource and to whom?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.ownersOutsourcingNeeds, viewModel::onOwnersOutsourcingNeedsChange, "Outsourcing Needs", "What marketing activities will you outsource and to whom?", M_Tint) }

                    GroupHeader("Systems")
                    GenericSystemsList(items = state.systems.map { Triple(it.id, it.systemOrApplication, it.purpose) },
                        tint = M_Tint, accentColor = M_Hero,
                        onAdd = { showDialog = true; editing = null },
                        onEdit = { id -> editing = state.systems.find { s -> s.id == id }; showDialog = true },
                        onRemove = { viewModel.removeSystem(it) })

                    GroupHeader("Implementation Status")
                    ModuleStatusSection(state.processStatus, viewModel::onProcessStatusChange, M_Tint, M_Neutral)

                    Button(onClick = { viewModel.submitMarketing() },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = M_Neutral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp)); Text("Submit Marketing", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp) }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun MSystemDialog(existing: MarketingSystemItem?, onDismiss: () -> Unit, onSave: (MarketingSystemItem) -> Unit) {
    var name by remember { mutableStateOf(existing?.systemOrApplication ?: "") }
    var purpose by remember { mutableStateOf(existing?.purpose ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: "Not Started") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (existing == null) "Add System" else "Edit System", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp)) } }
                DialogField("System or Application", name, { name = it }, M_Hero)
                DialogField("Purpose", purpose, { purpose = it }, M_Hero, minLines = 2)
                ModuleStatusSection(status, { status = it }, M_Tint, M_Neutral)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Cancel", color = Color(0xFF6B7280)) }
                    Button(onClick = { if (name.isNotBlank()) onSave(MarketingSystemItem(id = existing?.id ?: getTimeMillis().toString(), systemOrApplication = name.trim(), purpose = purpose.trim(), status = status)) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = M_Neutral)) { Text("Save") } }
            }
        }
    }
}