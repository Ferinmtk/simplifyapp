package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.simplifybiz.mobile.data.TargetMarketItem
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel

// ── Palette ───────────────────────────────────────────────────────────────────
private val HeroColor     = Color(0xFF7B1FA2)
private val HeroColorEnd  = Color(0xFF9C27B0)
private val FieldTint     = Color(0xFFF3E5F5)
private val Neutral       = Color(0xFF607D8B)
private val CardWhite     = Color(0xFFFFFFFF)
private val PageBg        = Color(0xFFF4F6F8)
private val TextPrimary   = Color(0xFF1C1C1E)
private val TextSecondary = Color(0xFF6B7280)
private val TextMuted     = Color(0xFFBDBDBD)
private val BorderEmpty   = Color(0xFFE0E0E0)
private val BorderFilled  = Color(0xFF1C1C1E)
private val DestructiveRed = Color(0xFFEF5350)
private val ProfitGreen   = Color(0xFF2E7D32)
private val ProfitRed     = Color(0xFFC62828)

class StrategyScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<StrategyViewModel>()
        val state by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val isRefreshing by viewModel.isRefreshing.collectAsState()

        var showTargetMarketDialog by remember { mutableStateOf(false) }
        var editingTargetMarket by remember { mutableStateOf<TargetMarketItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }
        LaunchedEffect(Unit) { viewModel.saveSuccess.collect { if (it) navigator?.pop() } }
        LaunchedEffect(Unit) { viewModel.validationMessage.collect { snackbarHostState.showSnackbar(it) } }

        val sectionsDone = listOf(
            state.purpose.isNotBlank(), state.coreValues.isNotBlank(),
            state.targetMarkets.isNotEmpty(), state.solutions.isNotBlank(),
            (state.budgetMarketing + state.budgetSales + state.budgetOperations + state.budgetAdmin) > 0,
            state.leadershipFirst.isNotBlank()
        ).count { it }
        val total = 6
        val progress = sectionsDone / total.toFloat()

        if (showTargetMarketDialog) {
            TargetMarketDialog(
                existingItem = editingTargetMarket,
                onDismiss = { showTargetMarketDialog = false; editingTargetMarket = null },
                onSave = { item ->
                    if (editingTargetMarket != null) viewModel.updateTargetMarket(item)
                    else viewModel.addTargetMarket(item)
                    showTargetMarketDialog = false; editingTargetMarket = null
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Strategy Workshop", style = MaterialTheme.typography.titleMedium.copy(
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
                // ── Hero ─────────────────────────────────────────────────────
                ModuleHero(
                    color = HeroColor, colorEnd = HeroColorEnd,
                    title = "Strategic Planning",
                    sectionsDone = sectionsDone, total = total, progress = progress,
                    isRefreshing = isRefreshing,
                    icon = {
                        Icon(Icons.Outlined.Explore, null,
                            tint = Color.White, modifier = it)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── FOUNDATION ────────────────────────────────────────────
                    GroupHeader("Foundation")

                    SectionCard(1, "Purpose", state.purpose.isNotBlank()) {
                        SectionHint("Why does this business exist? What problem are you here to solve?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.purpose, viewModel::onPurposeChange, "Purpose", "Why does this business exist? What problem are you here to solve?", FieldTint)
                    }
                    SectionCard(2, "Core Values", state.coreValues.isNotBlank()) {
                        SectionHint("What principles guide every decision in your business?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.coreValues, viewModel::onValuesChange, "Core Values", "What principles guide every decision in your business?", FieldTint)
                    }
                    SectionCard(4, "Solutions", state.solutions.isNotBlank()) {
                        SectionHint("What products or services do you offer? How do you deliver results?")
                        Spacer(Modifier.height(10.dp))
                        ExpandingField(state.solutions, viewModel::onSolutionsChange, "Solutions", "What products or services do you offer? How do you deliver results?", FieldTint)
                    }

                    // ── MARKET ────────────────────────────────────────────────
                    GroupHeader("Market")

                    SectionCard(3, "Target Market", state.targetMarkets.isNotEmpty()) {
                        SectionHint("Define who you serve. Be specific — niche beats broad.")
                        Spacer(Modifier.height(12.dp))
                        if (state.targetMarkets.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.targetMarkets.forEach { item ->
                                    Surface(shape = RoundedCornerShape(8.dp), color = FieldTint,
                                        modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.name, style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold, color = TextPrimary))
                                                if (item.opportunities.isNotBlank()) {
                                                    Text(item.opportunities, style = MaterialTheme.typography.bodySmall.copy(
                                                        color = TextSecondary), maxLines = 1)
                                                }
                                            }
                                            IconButton(onClick = { editingTargetMarket = item; showTargetMarketDialog = true },
                                                modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, null, tint = Neutral, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { viewModel.removeTargetMarket(item.id) },
                                                modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, null, tint = DestructiveRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        OutlinedButton(onClick = { editingTargetMarket = null; showTargetMarketDialog = true },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Neutral)) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add Target Market", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // ── FINANCE ───────────────────────────────────────────────
                    GroupHeader("Finance")

                    val totalBudget = state.budgetMarketing + state.budgetSales + state.budgetOperations + state.budgetAdmin
                    SectionCard(5, "Budget Allocation", totalBudget > 0) {
                        SectionHint("Enter the percentage of your budget for each area.")
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            BudgetInput("Marketing %", state.budgetMarketing, Modifier.weight(1f)) {
                                viewModel.onBudgetChange(it, null, null, null)
                            }
                            BudgetInput("Sales %", state.budgetSales, Modifier.weight(1f)) {
                                viewModel.onBudgetChange(null, it, null, null)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            BudgetInput("Operations %", state.budgetOperations, Modifier.weight(1f)) {
                                viewModel.onBudgetChange(null, null, it, null)
                            }
                            BudgetInput("Admin %", state.budgetAdmin, Modifier.weight(1f)) {
                                viewModel.onBudgetChange(null, null, null, it)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        val profit = 100 - totalBudget
                        Surface(shape = RoundedCornerShape(8.dp),
                            color = if (profit >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Projected Profit", style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (profit >= 0) ProfitGreen else ProfitRed))
                                    Text(if (profit < 0) "Over budget — reduce allocations" else "of revenue retained",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (profit >= 0) ProfitGreen.copy(alpha = 0.7f) else ProfitRed.copy(alpha = 0.7f)))
                                }
                                Text("$profit%", style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (profit >= 0) ProfitGreen else ProfitRed))
                            }
                        }
                    }

                    // ── TEAM ──────────────────────────────────────────────────
                    GroupHeader("Team")

                    SectionCard(6, "Accountability", state.leadershipFirst.isNotBlank()) {
                        SectionHint("Assign who leads each business area.")
                        Spacer(Modifier.height(14.dp))
                        Text("Vision Lead", style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold, color = TextPrimary))
                        Text("That's you — the business owner", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LeadNameField(state.leadershipFirst, "First Name",
                                { viewModel.onLeadNameChange("vision", firstName = it) }, Modifier.weight(1f), FieldTint)
                            LeadNameField(state.leadershipLast, "Last Name",
                                { viewModel.onLeadNameChange("vision", lastName = it) }, Modifier.weight(1f), FieldTint)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))
                        LeadToggleRow("Marketing Lead", state.hasMarketingLead, state.marketingFirst, state.marketingLast,
                            { viewModel.toggleLeadSection("marketing", it) },
                            { viewModel.onLeadNameChange("marketing", firstName = it) },
                            { viewModel.onLeadNameChange("marketing", lastName = it) }, FieldTint)
                        Spacer(Modifier.height(12.dp))
                        LeadToggleRow("Sales Lead", state.hasSalesLead, state.salesFirst, state.salesLast,
                            { viewModel.toggleLeadSection("sales", it) },
                            { viewModel.onLeadNameChange("sales", firstName = it) },
                            { viewModel.onLeadNameChange("sales", lastName = it) }, FieldTint)
                        Spacer(Modifier.height(12.dp))
                        LeadToggleRow("Operations Lead", state.hasOperationsLead, state.operationsFirst, state.operationsLast,
                            { viewModel.toggleLeadSection("operations", it) },
                            { viewModel.onLeadNameChange("operations", firstName = it) },
                            { viewModel.onLeadNameChange("operations", lastName = it) }, FieldTint)
                        Spacer(Modifier.height(12.dp))
                        LeadToggleRow("Systems Lead", state.hasSystemsLead, state.systemsFirst, state.systemsLast,
                            { viewModel.toggleLeadSection("systems", it) },
                            { viewModel.onLeadNameChange("systems", firstName = it) },
                            { viewModel.onLeadNameChange("systems", lastName = it) }, FieldTint)
                    }

                    // ── SUBMIT ────────────────────────────────────────────────
                    Button(onClick = { viewModel.submitStrategy() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Neutral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Submit Strategy", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

// ── Shared Hero ───────────────────────────────────────────────────────────────
@Composable
fun ModuleHero(
    color: Color,
    colorEnd: Color,
    title: String,
    sectionsDone: Int,
    total: Int,
    progress: Float,
    isRefreshing: Boolean,
    icon: @Composable (Modifier) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().drawBehind {
            drawRect(brush = Brush.linearGradient(
                colors = listOf(color, colorEnd),
                start = Offset(0f, 0f), end = Offset(size.width, size.height)))
            val waveH = 36.dp.toPx()
            val wavePath = Path().apply {
                moveTo(0f, size.height - waveH)
                cubicTo(size.width * 0.18f, size.height,
                    size.width * 0.36f, size.height - waveH * 1.1f,
                    size.width * 0.57f, size.height - waveH * 0.5f)
                cubicTo(size.width * 0.75f, size.height - waveH * 0.05f,
                    size.width * 0.88f, size.height - waveH * 0.7f,
                    size.width, size.height - waveH * 0.35f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(wavePath, color = Color(0xFFF4F6F8))
        }
    ) {
        // Watermark icon — actual Compose icon via graphicsLayer
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 32.dp)
                .graphicsLayer(alpha = 0.18f)
        ) {
            icon(Modifier.size(130.dp).align(Alignment.Center))
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 52.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon(Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.65f)))
                    Text("$sectionsDone of $total sections complete",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White, fontWeight = FontWeight.SemiBold))
                }
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                color = Color.White, trackColor = Color.White.copy(alpha = 0.22f))
            if (isRefreshing) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = Color.White.copy(alpha = 0.5f), trackColor = Color.Transparent)
            }
        }
    }
}

// ── Group Header ──────────────────────────────────────────────────────────────
@Composable
fun GroupHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(3.dp).height(16.dp)
            .clip(RoundedCornerShape(2.dp)).background(Neutral))
        Spacer(Modifier.width(8.dp))
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold, color = Neutral, letterSpacing = 1.2.sp))
    }
}

// ── Section Card — elevation 8dp ──────────────────────────────────────────────
@Composable
fun SectionCard(
    number: Int,
    title: String,
    isFilled: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape)
                    .background(if (isFilled) Neutral else Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center) {
                    if (isFilled) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text("$number", style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold, color = TextPrimary))
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

// ── Section Hint ──────────────────────────────────────────────────────────────
@Composable
fun SectionHint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
}

// ── Module Field — three states ───────────────────────────────────────────────
@Composable
fun ModuleField(value: String, onValueChange: (String) -> Unit, tint: Color,
                placeholder: String = "Type your answer here...") {
    val isFilled = value.isNotBlank()
    OutlinedTextField(value = value, onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6,
        shape = RoundedCornerShape(8.dp),
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = tint,
            unfocusedBorderColor = if (isFilled) BorderFilled else BorderEmpty,
            focusedContainerColor = CardWhite,
            focusedBorderColor = BorderFilled,
            cursorColor = Neutral))
}

// ── Budget Input ──────────────────────────────────────────────────────────────
@Composable
private fun BudgetInput(label: String, value: Int, modifier: Modifier, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { onChange(it.toIntOrNull() ?: 0) },
        label = { Text(label, fontSize = 11.sp) }, modifier = modifier,
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = FieldTint,
            unfocusedBorderColor = if (value > 0) BorderFilled else BorderEmpty,
            focusedContainerColor = CardWhite, focusedBorderColor = BorderFilled, cursorColor = Neutral))
}

// ── Lead Name Field ───────────────────────────────────────────────────────────
@Composable
fun LeadNameField(value: String, label: String, onValueChange: (String) -> Unit,
                  modifier: Modifier = Modifier, tint: Color) {
    OutlinedTextField(value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) }, modifier = modifier, singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = tint,
            unfocusedBorderColor = if (value.isNotBlank()) BorderFilled else BorderEmpty,
            focusedContainerColor = CardWhite, focusedBorderColor = BorderFilled, cursorColor = Neutral))
}

// ── Lead Toggle Row ───────────────────────────────────────────────────────────
@Composable
private fun LeadToggleRow(label: String, isEnabled: Boolean, firstName: String, lastName: String,
                          onToggle: (Boolean) -> Unit, onFirstChange: (String) -> Unit, onLastChange: (String) -> Unit,
                          tint: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (isEnabled) TextPrimary else TextSecondary))
            Switch(checked = isEnabled, onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, checkedTrackColor = Neutral,
                    uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFFDDDDDD)))
        }
        if (isEnabled) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LeadNameField(firstName, "First Name", onFirstChange, Modifier.weight(1f), tint)
                LeadNameField(lastName, "Last Name", onLastChange, Modifier.weight(1f), tint)
            }
        }
    }
}

// ── Target Market Dialog ──────────────────────────────────────────────────────
@Composable
private fun TargetMarketDialog(existingItem: TargetMarketItem?, onDismiss: () -> Unit,
                               onSave: (TargetMarketItem) -> Unit) {
    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var demographics by remember { mutableStateOf(existingItem?.demographics ?: "") }
    var behaviors by remember { mutableStateOf(existingItem?.behaviors ?: "") }
    var opportunities by remember { mutableStateOf(existingItem?.opportunities ?: "") }

    AlertDialog(onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(FieldTint),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.People, null, tint = HeroColor, modifier = Modifier.size(18.dp))
                }
                Text(if (existingItem != null) "Edit Target Market" else "Add Target Market",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DialogField("Market Name", name, { name = it }, HeroColor)
                DialogField("Demographics", demographics, { demographics = it }, HeroColor, minLines = 2)
                DialogField("Behaviours & Values", behaviors, { behaviors = it }, HeroColor, minLines = 2)
                DialogField("Market Opportunities", opportunities, { opportunities = it }, HeroColor, minLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onSave(TargetMarketItem(
                    id = existingItem?.id ?: getTimeMillis().toString(),
                    remoteId = existingItem?.remoteId, name = name,
                    demographics = demographics, behaviors = behaviors, opportunities = opportunities))
            }, colors = ButtonDefaults.buttonColors(containerColor = Neutral),
                shape = RoundedCornerShape(8.dp)) { Text("Save Market") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }, shape = RoundedCornerShape(16.dp))
}

// ── Dialog Field ──────────────────────────────────────────────────────────────
@Composable
fun DialogField(label: String, value: String, onValueChange: (String) -> Unit,
                accentColor: Color, minLines: Int = 1) {
    Column {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(
            color = accentColor, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp))
        Spacer(Modifier.height(5.dp))
        OutlinedTextField(value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(), minLines = minLines,
            maxLines = if (minLines > 1) 4 else 1, shape = RoundedCornerShape(8.dp),
            placeholder = {
                Text("Type here...", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CardWhite,
                unfocusedBorderColor = if (value.isNotBlank()) BorderFilled else BorderEmpty,
                focusedContainerColor = CardWhite, focusedBorderColor = accentColor, cursorColor = accentColor))
    }
}

// ── Expanding Field — tappable, opens full-screen editor ──────────────────────
@Composable
fun ExpandingField(
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    hint: String,
    tint: Color,
    placeholder: String = "Tap to write..."
) {
    var showEditor by remember { mutableStateOf(false) }
    val isFilled = value.isNotBlank()

    if (showEditor) {
        FullScreenEditor(
            value = value,
            title = title,
            hint = hint,
            tint = tint,
            onDone = { newValue -> onValueChange(newValue); showEditor = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showEditor = true }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(8.dp),
            placeholder = {
                Text(placeholder, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
            },
            trailingIcon = {
                Icon(
                    Icons.Default.Edit, null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isFilled) Neutral.copy(alpha = 0.6f) else TextMuted
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = tint,
                disabledBorderColor = if (isFilled) BorderFilled else BorderEmpty,
                disabledTextColor = TextPrimary,
                disabledPlaceholderColor = TextMuted,
                disabledTrailingIconColor = if (isFilled) Neutral.copy(alpha = 0.6f) else TextMuted
            )
        )
    }
}

// ── Full Screen Editor ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenEditor(
    value: String,
    title: String,
    hint: String,
    tint: Color,
    onDone: (String) -> Unit
) {
    var draft by remember { mutableStateOf(value) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = { onDone(draft) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        // Force standard full-width keyboard — prevents floating keyboard on dialog windows
        val view = LocalView.current
        SideEffect {
            (view.parent as? DialogWindowProvider)?.window
                ?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CardWhite
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Top bar ───────────────────────────────────────────────────
                TopAppBar(
                    title = {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { keyboardController?.hide(); onDone(draft) }) {
                            Icon(Icons.Default.ArrowBack, "Done", tint = TextPrimary)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { keyboardController?.hide(); onDone(draft) }
                        ) {
                            Text(
                                "Done",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Neutral,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
                )

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // ── Hint ──────────────────────────────────────────────────────
                Text(
                    hint,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                // ── Full text area ────────────────────────────────────────────
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            "Start writing here...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = tint,
                        unfocusedBorderColor = if (draft.isNotBlank()) BorderFilled else BorderEmpty,
                        focusedContainerColor = CardWhite,
                        focusedBorderColor = Neutral,
                        cursorColor = Neutral
                    )
                )

                // ── Save button ───────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWhite)
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 32.dp)
                ) {
                    if (draft.isNotBlank()) {
                        Text(
                            "${draft.trim().split("\\s+".toRegex()).size} words",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    Button(
                        onClick = { keyboardController?.hide(); onDone(draft) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (draft.isNotBlank()) tint.copy(alpha = 1f) else Color(0xFFEEEEEE)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            Icons.Default.Check, null,
                            modifier = Modifier.size(20.dp),
                            tint = if (draft.isNotBlank()) Neutral else TextMuted
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Save & Close",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp,
                            color = if (draft.isNotBlank()) Neutral else TextMuted
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}