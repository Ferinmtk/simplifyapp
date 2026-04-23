package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Stars
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

private val L_Hero    = Color(0xFF1A237E)
private val L_HeroEnd = Color(0xFF283593)
private val L_Tint    = Color(0xFFE8EAF6)
private val L_Neutral = Color(0xFF607D8B)
private val L_Card    = Color(0xFFFFFFFF)
private val L_Bg      = Color(0xFFF4F6F8)
private val L_Tp      = Color(0xFF1C1C1E)
private val L_Ts      = Color(0xFF6B7280)
private val L_Tm      = Color(0xFFBDBDBD)
private val L_Be      = Color(0xFFE0E0E0)
private val L_Bf      = Color(0xFF1C1C1E)
private val L_Red     = Color(0xFFEF5350)

class LeadershipScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<LeadershipViewModel>()
        val state by viewModel.uiState.collectAsState()
        val snackbar = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        var editing by remember { mutableStateOf<LeadershipSystemItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }
        LaunchedEffect(Unit) { viewModel.saveSuccess.collect { if (it) navigator?.pop() } }
        LaunchedEffect(Unit) { viewModel.validationMessage.collect { snackbar.showSnackbar(it) } }

        val fields = listOf(state.strategyReviewSchedule, state.decisionFramework, state.decisionTools,
            state.delegationPlan, state.expectationSetting, state.communicationChannels,
            state.leadershipTraining, state.coachingPrograms, state.leadershipKpis,
            state.feedbackMechanism, state.changeManagementFramework)
        val filled = fields.count { it.isNotBlank() }
        val total = 11

        if (showDialog) {
            LSystemDialog(existing = editing, accentColor = L_Hero, tint = L_Tint,
                onDismiss = { showDialog = false; editing = null },
                onSave = { if (editing != null) viewModel.updateSystem(it) else viewModel.addSystem(it)
                    showDialog = false; editing = null })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Leadership", style = MaterialTheme.typography.titleMedium.copy(color = L_Tp, fontWeight = FontWeight.SemiBold)) },
                    navigationIcon = { IconButton(onClick = { navigator?.pop() }) { Icon(Icons.Default.ArrowBack, "Back", tint = L_Tp) } },
                    actions = { IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh", tint = L_Tp) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = L_Card))
            },
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = L_Bg
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ModuleHero(L_Hero, L_HeroEnd, "Leadership Process", filled, total, filled / total.toFloat(), isRefreshing,
                    icon = { Icon(Icons.Outlined.Stars, null, tint = Color.White, modifier = it) })
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GroupHeader("Strategy")
                    SectionCard(1, "Strategy Review Schedule", state.strategyReviewSchedule.isNotBlank()) {
                        SectionHint("How often do you review your business strategy?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.strategyReviewSchedule, viewModel::onStrategyReviewScheduleChange, "Strategy Review Schedule", "How often do you review your business strategy?", L_Tint) }
                    SectionCard(2, "Decision Framework", state.decisionFramework.isNotBlank()) {
                        SectionHint("How are key decisions made in your business?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.decisionFramework, viewModel::onDecisionFrameworkChange, "Decision Framework", "How are key decisions made in your business?", L_Tint) }
                    SectionCard(3, "Decision Tools", state.decisionTools.isNotBlank()) {
                        SectionHint("What tools or templates support your decision making?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.decisionTools, viewModel::onDecisionToolsChange, "Decision Tools", "What tools or templates support your decision making?", L_Tint) }

                    GroupHeader("Delegation")
                    SectionCard(4, "Delegation Plan", state.delegationPlan.isNotBlank()) {
                        SectionHint("Who is responsible for which decisions and areas?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.delegationPlan, viewModel::onDelegationPlanChange, "Delegation Plan", "Who is responsible for which decisions and areas?", L_Tint) }
                    SectionCard(5, "Expectations", state.expectationSetting.isNotBlank()) {
                        SectionHint("What are your standards for communication and delivery?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.expectationSetting, viewModel::onExpectationSettingChange, "Expectations", "What are your standards for communication and delivery?", L_Tint) }

                    GroupHeader("Communication")
                    SectionCard(6, "Channels", state.communicationChannels.isNotBlank()) {
                        SectionHint("What internal and external communication channels do you use?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.communicationChannels, viewModel::onCommunicationChannelsChange, "Channels", "What internal and external communication channels do you use?", L_Tint) }

                    GroupHeader("Development")
                    SectionCard(7, "Leadership Training", state.leadershipTraining.isNotBlank()) {
                        SectionHint("How do you build leadership capability in your team?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.leadershipTraining, viewModel::onLeadershipTrainingChange, "Leadership Training", "How do you build leadership capability in your team?", L_Tint) }
                    SectionCard(8, "Coaching Programs", state.coachingPrograms.isNotBlank()) {
                        SectionHint("What coaching or mentoring support exists for leaders?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.coachingPrograms, viewModel::onCoachingProgramsChange, "Coaching Programs", "What coaching or mentoring support exists for leaders?", L_Tint) }

                    GroupHeader("Performance")
                    SectionCard(9, "Leadership KPIs", state.leadershipKpis.isNotBlank()) {
                        SectionHint("How do you measure leadership effectiveness?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.leadershipKpis, viewModel::onLeadershipKpisChange, "Leadership KPIs", "How do you measure leadership effectiveness?", L_Tint) }
                    SectionCard(10, "Feedback Mechanism", state.feedbackMechanism.isNotBlank()) {
                        SectionHint("How do you gather and act on leadership feedback?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.feedbackMechanism, viewModel::onFeedbackMechanismChange, "Feedback Mechanism", "How do you gather and act on leadership feedback?", L_Tint) }

                    GroupHeader("Change Management")
                    SectionCard(11, "Change Management Framework", state.changeManagementFramework.isNotBlank()) {
                        SectionHint("How do you introduce and manage change in the business?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.changeManagementFramework, viewModel::onChangeManagementFrameworkChange, "Change Management", "How do you introduce and manage change in the business?", L_Tint) }

                    GroupHeader("Systems")
                    GenericSystemsList(items = state.systemsUsed.map { Triple(it.id, it.systemName, it.purpose) },
                        tint = L_Tint, accentColor = L_Hero,
                        onAdd = { showDialog = true; editing = null },
                        onEdit = { id -> editing = state.systemsUsed.find { it.id == id }; showDialog = true },
                        onRemove = { viewModel.removeSystem(it) })

                    GroupHeader("Implementation Status")
                    ModuleStatusSection(state.statusQuoOfImplementation, viewModel::onStatusChange, L_Tint, L_Neutral)

                    Button(onClick = { viewModel.submitLeadership() },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = L_Neutral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Submit Leadership", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp) }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

// ── Shared status section ─────────────────────────────────────────────────────
@Composable
fun ModuleStatusSection(selected: String, onSelected: (String) -> Unit, tint: Color, neutral: Color) {
    val options = listOf("Not Started", "In Progress", "Fully Implemented")
    Column {
        options.forEach { option ->
            val isSel = selected == option
            Surface(shape = RoundedCornerShape(8.dp), color = if (isSel) tint else Color.Transparent,
                modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isSel, onClick = { onSelected(option) },
                        colors = RadioButtonDefaults.colors(selectedColor = neutral, unselectedColor = Color(0xFFBDBDBD)))
                    Text(option, style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isSel) neutral else Color(0xFF6B7280),
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal))
                }
            }
        }
    }
}

// ── Generic systems list (for modules using LeadershipSystemItem) ──────────────
@Composable
fun GenericSystemsList(items: List<Triple<String, String, String>>, tint: Color, accentColor: Color,
                       onAdd: () -> Unit, onEdit: (String) -> Unit, onRemove: (String) -> Unit) {
    Column {
        if (items.isNotEmpty()) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    items.forEachIndexed { i, (id, name, purpose) ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1C1E)))
                                if (purpose.isNotBlank()) Text(purpose, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280)),
                                    maxLines = 2, overflow = TextOverflow.Ellipsis) }
                            IconButton(onClick = { onEdit(id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, null, tint = Color(0xFF607D8B), modifier = Modifier.size(15.dp)) }
                            IconButton(onClick = { onRemove(id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350), modifier = Modifier.size(15.dp)) }
                        }
                        if (i != items.lastIndex) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF607D8B))) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add System", style = MaterialTheme.typography.labelMedium) }
    }
}

// ── LeadershipSystemItem dialog ───────────────────────────────────────────────
@Composable
fun LSystemDialog(existing: LeadershipSystemItem?, accentColor: Color, tint: Color,
                  onDismiss: () -> Unit, onSave: (LeadershipSystemItem) -> Unit) {
    var name by remember { mutableStateOf(existing?.systemName ?: "") }
    var purpose by remember { mutableStateOf(existing?.purpose ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: "Not Started") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (existing == null) "Add System" else "Edit System", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp)) } }
                DialogField("System or Application", name, { name = it }, accentColor)
                DialogField("Purpose", purpose, { purpose = it }, accentColor, minLines = 2)
                Text("STATUS", style = MaterialTheme.typography.labelSmall.copy(color = accentColor, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf("Not Started", "In Progress", "Fully Implemented").forEach { opt ->
                        val isSel = status == opt
                        Surface(shape = RoundedCornerShape(8.dp), color = if (isSel) tint else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = isSel, onClick = { status = opt },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF607D8B), unselectedColor = Color(0xFFBDBDBD)))
                                Text(opt, style = MaterialTheme.typography.bodySmall.copy(color = if (isSel) Color(0xFF607D8B) else Color(0xFF6B7280))) } } } }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Cancel", color = Color(0xFF6B7280)) }
                    Button(onClick = { if (name.isNotBlank()) onSave(LeadershipSystemItem(id = existing?.id ?: getTimeMillis().toString(), systemName = name.trim(), purpose = purpose.trim(), status = status)) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B))) { Text("Save") } }
            }
        }
    }
}