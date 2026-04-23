package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Diversity3
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
import com.simplifybiz.mobile.data.PeopleSystemItem
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.clickable
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val P_Hero    = Color(0xFF827717)
private val P_HeroEnd = Color(0xFFAFB42B)
private val P_Tint    = Color(0xFFF9FBE7)
private val P_Neutral = Color(0xFF607D8B)
private val P_Card    = Color(0xFFFFFFFF)
private val P_Bg      = Color(0xFFF4F6F8)
private val P_Tp      = Color(0xFF1C1C1E)
private val P_Ts      = Color(0xFF6B7280)

class PeopleScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<PeopleViewModel>()
        val state by viewModel.uiState.collectAsState()
        val snackbar = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        var editing by remember { mutableStateOf<PeopleSystemItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }
        LaunchedEffect(Unit) { viewModel.saveSuccess.collect { if (it) navigator?.pop() } }
        LaunchedEffect(Unit) { viewModel.validationMessage.collect { snackbar.showSnackbar(it) } }

        val filled = listOf(state.jobRolesAndSkills, state.recruitmentChannels, state.hiringProcess,
            state.onboardingPlan, state.onboardingTraining, state.trainingPrograms, state.mentorshipPrograms,
            state.performanceGoals, state.performanceReviewSchedule, state.compensationPackages,
            state.retentionInitiatives, state.successionPlan, state.leadershipDevelopmentPlans,
            state.employeeEngagementMethods, state.feedbackActionPlan).count { it.isNotBlank() }
        val total = 15

        if (showDialog) {
            PSystemDialog(existing = editing, onDismiss = { showDialog = false; editing = null },
                onSave = { if (editing != null) viewModel.updateSystem(it) else viewModel.addSystem(it)
                    showDialog = false; editing = null })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("People", style = MaterialTheme.typography.titleMedium.copy(color = P_Tp, fontWeight = FontWeight.SemiBold)) },
                    navigationIcon = { IconButton(onClick = { navigator?.pop() }) { Icon(Icons.Default.ArrowBack, "Back", tint = P_Tp) } },
                    actions = { IconButton(onClick = { if (!isRefreshing) viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh", tint = P_Tp) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = P_Card))
            },
            snackbarHost = { SnackbarHost(snackbar) }, containerColor = P_Bg
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ModuleHero(P_Hero, P_HeroEnd, "People & HR", filled, total, filled / total.toFloat(), isRefreshing,
                    icon = { Icon(Icons.Outlined.Diversity3, null, tint = Color.White, modifier = it) })
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    GroupHeader("Hiring")
                    SectionCard(1, "Job Roles & Skills", state.jobRolesAndSkills.isNotBlank()) {
                        SectionHint("What roles exist in your business and what skills do they require?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.jobRolesAndSkills, viewModel::onJobRolesChange, "Job Roles & Skills", "What roles exist in your business and what skills do they require?", P_Tint) }
                    SectionCard(2, "Recruitment Channels", state.recruitmentChannels.isNotBlank()) {
                        SectionHint("Where do you find and attract candidates?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.recruitmentChannels, viewModel::onRecruitmentChannelsChange, "Recruitment Channels", "Where do you find and attract candidates?", P_Tint) }
                    SectionCard(3, "Hiring Process", state.hiringProcess.isNotBlank()) {
                        SectionHint("What steps do you follow from application to offer?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.hiringProcess, viewModel::onHiringProcessChange, "Hiring Process", "What steps do you follow from application to offer?", P_Tint) }

                    GroupHeader("Onboarding")
                    SectionCard(4, "Onboarding Plan", state.onboardingPlan.isNotBlank()) {
                        SectionHint("What is your plan for bringing new team members up to speed?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.onboardingPlan, viewModel::onOnboardingPlanChange, "Onboarding Plan", "What is your plan for bringing new team members up to speed?", P_Tint) }
                    SectionCard(5, "Onboarding Training", state.onboardingTraining.isNotBlank()) {
                        SectionHint("What training do new hires receive in their first weeks?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.onboardingTraining, viewModel::onOnboardingTrainingChange, "Onboarding Training", "What training do new hires receive in their first weeks?", P_Tint) }

                    GroupHeader("Development")
                    SectionCard(6, "Training Programs", state.trainingPrograms.isNotBlank()) {
                        SectionHint("What ongoing training is available for your team?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.trainingPrograms, viewModel::onTrainingProgramsChange, "Training Programs", "What ongoing training is available for your team?", P_Tint) }
                    SectionCard(7, "Mentorship Programs", state.mentorshipPrograms.isNotBlank()) {
                        SectionHint("How do you pair and develop people through mentorship?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.mentorshipPrograms, viewModel::onMentorshipProgramsChange, "Mentorship Programs", "How do you pair and develop people through mentorship?", P_Tint) }
                    SectionCard(8, "Leadership Development", state.leadershipDevelopmentPlans.isNotBlank()) {
                        SectionHint("How do you develop future leaders within the business?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.leadershipDevelopmentPlans, viewModel::onLeadershipDevelopmentPlansChange, "Leadership Development", "How do you develop future leaders within the business?", P_Tint) }

                    GroupHeader("Performance")
                    SectionCard(9, "Performance Goals", state.performanceGoals.isNotBlank()) {
                        SectionHint("What goals are set for individuals and how are they tracked?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.performanceGoals, viewModel::onPerformanceGoalsChange, "Performance Goals", "What goals are set for individuals and how are they tracked?", P_Tint) }
                    SectionCard(10, "Performance Review Schedule", state.performanceReviewSchedule.isNotBlank()) {
                        SectionHint("How often do performance reviews happen and what do they cover?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.performanceReviewSchedule, viewModel::onPerformanceReviewScheduleChange, "Performance Review Schedule", "How often do performance reviews happen and what do they cover?", P_Tint) }

                    GroupHeader("Retention")
                    SectionCard(11, "Compensation Packages", state.compensationPackages.isNotBlank()) {
                        SectionHint("What salary, benefits, and incentives do you offer?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.compensationPackages, viewModel::onCompensationPackagesChange, "Compensation Packages", "What salary, benefits, and incentives do you offer?", P_Tint) }
                    SectionCard(12, "Retention Initiatives", state.retentionInitiatives.isNotBlank()) {
                        SectionHint("What do you do to keep your best people?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.retentionInitiatives, viewModel::onRetentionInitiativesChange, "Retention Initiatives", "What do you do to keep your best people?", P_Tint) }
                    SectionCard(13, "Succession Plan", state.successionPlan.isNotBlank()) {
                        SectionHint("Who steps up if a key person leaves?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.successionPlan, viewModel::onSuccessionPlanChange, "Succession Plan", "Who steps up if a key person leaves?", P_Tint) }

                    GroupHeader("Engagement")
                    SectionCard(14, "Employee Engagement Methods", state.employeeEngagementMethods.isNotBlank()) {
                        SectionHint("How do you keep your team motivated and connected?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.employeeEngagementMethods, viewModel::onEmployeeEngagementMethodsChange, "Employee Engagement", "How do you keep your team motivated and connected?", P_Tint) }
                    SectionCard(15, "Feedback Action Plan", state.feedbackActionPlan.isNotBlank()) {
                        SectionHint("How do you collect and act on employee feedback?")
                        Spacer(Modifier.height(10.dp)); ExpandingField(state.feedbackActionPlan, viewModel::onFeedbackActionPlanChange, "Feedback Action Plan", "How do you collect and act on employee feedback?", P_Tint) }

                    GroupHeader("Systems")
                    GenericSystemsList(items = state.systems.map { Triple(it.id, it.systemOrApplication, it.purpose) },
                        tint = P_Tint, accentColor = P_Hero,
                        onAdd = { showDialog = true; editing = null },
                        onEdit = { id -> editing = state.systems.find { s -> s.id == id }; showDialog = true },
                        onRemove = { viewModel.removeSystem(it) })

                    GroupHeader("Implementation Status")
                    ModuleStatusSection(state.statusQuoOfImplementation, viewModel::onStatusChange, P_Tint, P_Neutral)

                    Button(onClick = { viewModel.submitPeople() },
                        modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = P_Neutral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp)); Text("Submit People Plan", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp) }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun PSystemDialog(existing: PeopleSystemItem?, onDismiss: () -> Unit, onSave: (PeopleSystemItem) -> Unit) {
    var name by remember { mutableStateOf(existing?.systemOrApplication ?: "") }
    var purpose by remember { mutableStateOf(existing?.purpose ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: "Not Started") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (existing == null) "Add System" else "Edit System", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = P_Ts, modifier = Modifier.size(18.dp)) } }
                DialogField("System or Application", name, { name = it }, P_Hero)
                DialogField("Purpose", purpose, { purpose = it }, P_Hero, minLines = 2)
                ModuleStatusSection(status, { status = it }, P_Tint, P_Neutral)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Cancel", color = P_Ts) }
                    Button(onClick = { if (name.isNotBlank()) onSave(PeopleSystemItem(id = existing?.id ?: getTimeMillis().toString(), systemOrApplication = name.trim(), purpose = purpose.trim(), status = status)) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = P_Neutral)) { Text("Save") } }
            }
        }
    }
}