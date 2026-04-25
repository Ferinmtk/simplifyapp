package com.simplifybiz.mobile.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import simplifybiz.composeapp.generated.resources.Res
import simplifybiz.composeapp.generated.resources.logo_simplify_text
import simplifybiz.composeapp.generated.resources.logo_zen_stones

class DashboardScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<DashboardViewModel>()

        val user by viewModel.user.collectAsState()
        val strategyStatus by viewModel.strategyStatus.collectAsState()
        val leadershipStatus by viewModel.leadershipStatus.collectAsState()
        val marketingStatus by viewModel.marketingStatus.collectAsState()
        val salesStatus by viewModel.salesStatus.collectAsState()
        val operationsStatus by viewModel.operationsStatus.collectAsState()
        val peopleStatus by viewModel.peopleStatus.collectAsState()
        val moneyStatus by viewModel.moneyStatus.collectAsState()
        val objectiveStatus by viewModel.objectiveStatus.collectAsState()
        val rdStatus by viewModel.rdStatus.collectAsState()
        val riskStatus by viewModel.riskStatus.collectAsState()

        var showLogoutDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.loadData()
        }

        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.Black
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.logo_zen_stones),
                            contentDescription = null,
                            modifier = Modifier
                                .height(30.dp)
                                .aspectRatio(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Image(
                            painter = painterResource(Res.drawable.logo_simplify_text),
                            contentDescription = "SimplifyBiz",
                            modifier = Modifier
                                .height(20.dp)
                                .widthIn(max = 120.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { showLogoutDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.displayName?.take(1) ?: "U",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            containerColor = Color(0xFFF9F9F9)
        ) { padding ->

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Logout") },
                    text = { Text("Are you sure you want to log out?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.logout {
                                    navigator.replaceAll(LoginScreen())
                                }
                            }
                        ) {
                            Text("Logout", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text(text = "Cancel", color = Color.Black)
                        }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                        Text(
                            text = "Hello",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Text(
                            text = user?.displayName ?: "Business Owner",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item { MainSectionHeader("PLAN") }

                item { SubSectionHeader("Strategy") }
                item {
                    GridRow(
                        GridItem(
                            title = "Strategy",
                            icon = Icons.Outlined.Explore,
                            color = Color(0xFF7B1FA2),
                            status = strategyStatus
                        ) { navigator.push(StrategyScreen()) },
                        GridItem(
                            title = "Target Market",
                            icon = Icons.Outlined.Groups,
                            color = Color(0xFF7B1FA2),
                            status = "not_started"
                        ) { navigator.push(TargetMarketManagerScreen()) }
                    )
                }

                item { SubSectionHeader("Process") }
                item {
                    GridRow(
                        GridItem(
                            title = "Leadership",
                            icon = Icons.Outlined.Stars,
                            color = Color(0xFF7B1FA2),
                            status = leadershipStatus
                        ) { navigator.push(LeadershipScreen()) },
                        GridItem(
                            title = "Marketing",
                            icon = Icons.Outlined.Campaign,
                            color = Color(0xFFD81B60),
                            status = marketingStatus
                        ) { navigator.push(MarketingScreen()) }
                    )
                }

                item {
                    GridRow(
                        GridItem(
                            title = "Sales",
                            icon = Icons.Outlined.Handshake,
                            color = Color(0xFFD32F2F),
                            status = salesStatus
                        ) { navigator.push(SalesScreen()) },
                        GridItem(
                            title = "Operations",
                            icon = Icons.Outlined.Engineering,
                            color = Color(0xFFE65100),
                            status = operationsStatus
                        ) { navigator.push(OperationsScreen()) }
                    )
                }

                item {
                    GridRow(
                        item1 = GridItem(
                            title = "People",
                            icon = Icons.Outlined.Diversity3,
                            color = Color(0xFFAFB42B),
                            status = peopleStatus
                        ) { navigator.push(PeopleScreen()) },
                        item2 = GridItem(
                            title = "Money",
                            icon = Icons.Outlined.AttachMoney,
                            color = Color(0xFF388E3C),
                            status = moneyStatus
                        ) { navigator.push(MoneyScreen()) }
                    )
                }

                item {
                    GridRow(
                        GridItem(
                            title = "R&D",
                            icon = Icons.Outlined.Science,
                            color = Color(0xFF0097A7),
                            status = rdStatus
                        ) { navigator.push(ResearchAndDevelopmentScreen()) },
                        GridItem(
                            title = "Risk",
                            icon = Icons.Outlined.Security,
                            color = Color(0xFF1976D2),
                            status = riskStatus
                        ) { navigator.push(RiskScreen()) }
                    )
                }

                item { SubSectionHeader("Systems") }
                item {
                    GridRow(
                        GridItem(
                            title = "View Systems",
                            icon = Icons.Outlined.Hub,
                            color = Color(0xFF7B1FA2),
                            status = "not_started"
                        ) { navigator.push(SystemsManagerScreen()) },
                        null
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    MainSectionHeader("IMPLEMENT")
                }

                item {
                    GridRow(
                        GridItem(
                            title = "Objectives",
                            icon = Icons.Outlined.TrackChanges,
                            color = Color(0xFF7B1FA2),
                            status = objectiveStatus
                        ) { navigator.push(ObjectivesListScreen()) },
                        GridItem(
                            title = "Action Steps",
                            icon = Icons.Outlined.Checklist,
                            color = Color(0xFF7B1FA2),
                            status = "not_started"
                        ) { navigator.push(ActionStepsScreen()) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    MainSectionHeader("RESOURCES")
                }

                item {
                    GridRow(
                        GridItem("Links", Icons.Outlined.Link, Color(0xFF1976D2), "not_started") { navigator.push(LinksScreen()) },
                        null
                    )
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }

    data class GridItem(
        val title: String,
        val icon: ImageVector,
        val color: Color,
        val status: String = "not_started",
        val onClick: () -> Unit = {}
    )

    @Composable
    fun MainSectionHeader(title: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color(0xFF333333), shape = RoundedCornerShape(4.dp))
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )
        }
    }

    @Composable
    fun SubSectionHeader(title: String) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }

    @Composable
    fun GridRow(item1: GridItem, item2: GridItem?) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardItemCard(item1, Modifier.weight(1f))
            if (item2 != null) {
                DashboardItemCard(item2, Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    @Composable
    fun DashboardItemCard(item: GridItem, modifier: Modifier = Modifier) {
        val isSubmitted = item.status == "submitted"
        val isDraft = item.status == "draft"
        val isEmpty = item.status == "empty"
        val isNotStarted = item.status == "not_started" || item.status.isBlank()

        Card(
            modifier = modifier
                .height(100.dp)
                .clickable { item.onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.DarkGray
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isSubmitted -> {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Submitted",
                                tint = Color(0xFF4A148C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        isDraft -> {
                            Icon(
                                imageVector = Icons.Filled.Circle,
                                contentDescription = "Draft",
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        isEmpty || isNotStarted -> {
                            Icon(
                                imageVector = Icons.Outlined.Circle,
                                contentDescription = "Not Started",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Filled.Circle,
                                contentDescription = "In Progress",
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}