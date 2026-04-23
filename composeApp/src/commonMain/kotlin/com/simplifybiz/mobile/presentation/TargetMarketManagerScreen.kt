package com.simplifybiz.mobile.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Groups
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.simplifybiz.mobile.data.TargetMarketItem
import com.simplifybiz.mobile.presentation.components.LuxuryTextField
import io.ktor.util.date.getTimeMillis
import org.koin.compose.viewmodel.koinViewModel

// ── Color Palette (matches StrategyScreen) ────────────────────────────────────
private val ModulePurple      = Color(0xFF7B1FA2)
private val ModulePurpleDark  = Color(0xFF4A148C)
private val ModulePurpleEnd   = Color(0xFF8E24AA)
private val ModulePurpleLight = Color(0xFFF3E5F5)
private val CardWhite         = Color(0xFFFFFFFF)
private val PageBackground    = Color(0xFFF4F6F8)
private val TextPrimary       = Color(0xFF1C1C1E)
private val TextSecondary     = Color(0xFF6B7280)
private val TextMuted         = Color(0xFFBDBDBD)
private val BorderEmpty       = Color(0xFFE0E0E0)
private val BorderFilled      = Color(0xFF1C1C1E)
private val DividerColor      = Color(0xFFF5F5F5)
private val DestructiveRed    = Color(0xFFEF5350)

class TargetMarketManagerScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = koinViewModel<StrategyViewModel>()
        val state by viewModel.uiState.collectAsState()

        var expandedId by remember { mutableStateOf<String?>(null) }
        var editingItem by remember { mutableStateOf<TargetMarketItem?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        var showDeleteConfirm by remember { mutableStateOf<TargetMarketItem?>(null) }

        DisposableEffect(Unit) { onDispose { viewModel.saveDraft() } }

        if (showDialog) {
            TargetMarketDialog(
                existingItem = editingItem,
                onDismiss = { showDialog = false; editingItem = null },
                onSave = { item ->
                    if (editingItem != null) viewModel.updateTargetMarket(item)
                    else viewModel.addTargetMarket(item)
                    showDialog = false
                    editingItem = null
                }
            )
        }

        showDeleteConfirm?.let { item ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title = {
                    Text(
                        "Remove Market?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        "\"${item.name}\" will be permanently removed.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.removeTargetMarket(item.id)
                            if (expandedId == item.id) expandedId = null
                            showDeleteConfirm = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DestructiveRed),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Remove") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Target Market",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ModulePurple)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { editingItem = null; showDialog = true },
                    containerColor = ModulePurple,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Add Target Market")
                }
            },
            containerColor = PageBackground
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                // ── Hero ─────────────────────────────────────────────────────
                TargetMarketHero(count = state.targetMarkets.size)

                // ── List ──────────────────────────────────────────────────────
                if (state.targetMarkets.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
                    ) {
                        items(state.targetMarkets, key = { it.id }) { item ->
                            MarketCard(
                                item = item,
                                isExpanded = expandedId == item.id,
                                onToggle = {
                                    expandedId = if (expandedId == item.id) null else item.id
                                },
                                onEdit = { editingItem = item; showDialog = true },
                                onDelete = { showDeleteConfirm = item }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────
@Composable
private fun TargetMarketHero(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Purple gradient
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF7B1FA2), Color(0xFF8E24AA)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )

                // Groups icon watermark — circles representing people
                val cx = size.width - 56.dp.toPx()
                val cy = size.height - 16.dp.toPx()
                val alpha = 0.06f
                val strokeW = 5.dp.toPx()

                // Outer ring
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 72.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeW)
                )
                // Middle ring
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 48.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeW)
                )
                // Person circles (3 dots suggesting people)
                drawCircle(color = Color.White.copy(alpha = alpha), radius = 10.dp.toPx(),
                    center = Offset(cx, cy - 18.dp.toPx()))
                drawCircle(color = Color.White.copy(alpha = alpha), radius = 8.dp.toPx(),
                    center = Offset(cx - 20.dp.toPx(), cy - 8.dp.toPx()))
                drawCircle(color = Color.White.copy(alpha = alpha), radius = 8.dp.toPx(),
                    center = Offset(cx + 20.dp.toPx(), cy - 8.dp.toPx()))

                // Wave
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Groups, null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    "Strategic Planning",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.65f)
                    )
                )
                Text(
                    if (count == 0) "No markets defined yet"
                    else "$count market${if (count > 1) "s" else ""} defined",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(ModulePurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Groups, null,
                    tint = ModulePurple,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                "No Target Markets Yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            Text(
                "Tap the + button to define who your business serves.\nBe specific — niche beats broad.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Market Card ───────────────────────────────────────────────────────────────
@Composable
private fun MarketCard(
    item: TargetMarketItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // ── Header row ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial badge — filled purple when expanded, light when collapsed
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isExpanded) ModulePurple else ModulePurpleLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = if (isExpanded) Color.White else ModulePurple,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    if (!isExpanded) {
                        val preview = item.opportunities.ifBlank {
                            item.demographics.ifBlank { "Tap to view details" }
                        }
                        Text(
                            preview,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Edit, "Edit",
                        tint = if (isExpanded) ModulePurple else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.Delete, "Delete",
                        tint = DestructiveRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isExpanded) ModulePurple else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // ── Expanded detail ───────────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 14.dp, end = 14.dp, bottom = 14.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = DividerColor)

                    val hasContent = item.demographics.isNotBlank()
                            || item.behaviors.isNotBlank()
                            || item.opportunities.isNotBlank()

                    if (!hasContent) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ModulePurpleLight, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info, null,
                                tint = ModulePurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "No details yet — tap edit to fill in this market.",
                                style = MaterialTheme.typography.bodySmall.copy(color = ModulePurple)
                            )
                        }
                    } else {
                        if (item.demographics.isNotBlank()) {
                            DetailBlock("Demographics", item.demographics)
                        }
                        if (item.behaviors.isNotBlank()) {
                            DetailBlock("Behaviours & Values", item.behaviors)
                        }
                        if (item.opportunities.isNotBlank()) {
                            DetailBlock("Opportunities", item.opportunities)
                        }
                    }
                }
            }

            // ── Active strip ──────────────────────────────────────────────
            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ModulePurple, ModulePurple.copy(alpha = 0f))
                            )
                        )
                )
            }
        }
    }
}

// ── Detail Block ──────────────────────────────────────────────────────────────
@Composable
private fun DetailBlock(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ModulePurpleLight, RoundedCornerShape(8.dp))
            .padding(10.dp, 10.dp, 10.dp, 10.dp)
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = ModulePurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
        )
    }
}

// ── Target Market Dialog ──────────────────────────────────────────────────────
@Composable
private fun TargetMarketDialog(
    existingItem: TargetMarketItem?,
    onDismiss: () -> Unit,
    onSave: (TargetMarketItem) -> Unit
) {
    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var demographics by remember { mutableStateOf(existingItem?.demographics ?: "") }
    var behaviors by remember { mutableStateOf(existingItem?.behaviors ?: "") }
    var opportunities by remember { mutableStateOf(existingItem?.opportunities ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Purple header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ModulePurpleLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Groups, null,
                        tint = ModulePurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    if (existingItem != null) "Edit Target Market" else "Add Target Market",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogField(label = "Market Name", value = name, onValueChange = { name = it })
                DialogField(label = "Demographics", value = demographics,
                    onValueChange = { demographics = it }, minLines = 2)
                DialogField(label = "Behaviours & Values", value = behaviors,
                    onValueChange = { behaviors = it }, minLines = 2)
                DialogField(label = "Market Opportunities", value = opportunities,
                    onValueChange = { opportunities = it }, minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            TargetMarketItem(
                                id = existingItem?.id ?: getTimeMillis().toString(),
                                remoteId = existingItem?.remoteId,
                                name = name,
                                demographics = demographics,
                                behaviors = behaviors,
                                opportunities = opportunities
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ModulePurple),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Save Market") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

// ── Dialog Field — three-state system ────────────────────────────────────────
@Composable
private fun DialogField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1
) {
    val isFilled = value.isNotBlank()
    Column {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = ModulePurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        )
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            maxLines = if (minLines > 1) 4 else 1,
            shape = RoundedCornerShape(8.dp),
            placeholder = {
                Text(
                    "Type here...",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CardWhite,
                unfocusedBorderColor = if (isFilled) BorderFilled else BorderEmpty,
                focusedContainerColor = CardWhite,
                focusedBorderColor = ModulePurple,
                cursorColor = ModulePurple
            )
        )
    }
}