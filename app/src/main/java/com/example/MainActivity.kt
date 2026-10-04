package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.FinancialNewsEntity
import com.example.data.local.MarketAssetEntity
import com.example.data.local.PersonalizedPortfolioEntity
import com.example.data.repository.NavigatorRepository
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.DisclaimerDialog
import com.example.ui.consultant.ConsultantScreen
import com.example.ui.consultant.ConsultantViewModel
import com.example.ui.market.MarketCompassScreen
import com.example.ui.news.FinancialNewsScreen
import com.example.ui.portfolio.PortfolioAndArchiveScreen
import com.example.ui.portfolio.PortfolioWizardDialog
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val consultantViewModel: ConsultantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = NavigatorRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                MainApp(
                    consultantViewModel = consultantViewModel,
                    repository = repository
                )
            }
        }
    }
}

sealed class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    object Consultant : NavigationTab("Навигатор", Icons.Default.Psychology, "tab_consultant")
    object News : NavigationTab("Новости AI", Icons.Default.Radar, "tab_news")
    object Compass : NavigationTab("Компас", Icons.Default.CompassCalibration, "tab_compass")
    object Portfolio : NavigationTab("Портфель", Icons.Default.AccountBalanceWallet, "tab_portfolio")
}

@Composable
fun MainApp(
    consultantViewModel: ConsultantViewModel,
    repository: NavigatorRepository
) {
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showPortfolioWizard by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val assets by repository.allAssetsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val newsList by repository.allNewsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val latestPortfolio by repository.latestPortfolioFlow.collectAsStateWithLifecycle(initialValue = null)
    val bookmarkedMessages by repository.bookmarkedMessagesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val tabs = remember {
        listOf(
            NavigationTab.Consultant,
            NavigationTab.News,
            NavigationTab.Compass,
            NavigationTab.Portfolio
        )
    }

    // BackHandler: Return to consultant tab if on sub-screen
    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 600.dp

        if (isExpanded) {
            // Tablet / Foldable NavigationRail Layout
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationRailItem(
                            selected = currentTab == index,
                            onClick = { currentTab = index },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(
                        currentTab = currentTab,
                        consultantViewModel = consultantViewModel,
                        assets = assets,
                        newsList = newsList,
                        latestPortfolio = latestPortfolio,
                        bookmarkedMessages = bookmarkedMessages,
                        onNavigateToChat = { currentTab = 0 },
                        onOpenDisclaimer = { showDisclaimerDialog = true },
                        onOpenApiKeyDialog = { showApiKeyDialog = true },
                        onOpenPortfolioWizard = { showPortfolioWizard = true },
                        onAnalyzeCustomNews = { text ->
                            coroutineScope.launch { repository.analyzeCustomNewsWithAI(text) }
                        },
                        onDeleteNews = { id ->
                            coroutineScope.launch { repository.deleteNews(id) }
                        },
                        onAddAsset = { asset ->
                            coroutineScope.launch { repository.addOrUpdateAsset(asset) }
                        },
                        onDeleteAsset = { ticker ->
                            coroutineScope.launch { repository.deleteAsset(ticker) }
                        },
                        onToggleBookmark = { id, current ->
                            coroutineScope.launch { repository.toggleBookmark(id, current) }
                        },
                        onStressTestPortfolio = { portfolio ->
                            val prompt = "Проведи глубокий стресс-тест моего инвестиционного портфеля «${portfolio.title}» (${portfolio.targetAllocation}). Горизонт: ${portfolio.investmentHorizon}, Риск-профиль: ${portfolio.riskTolerance}. Каковы уязвимости и план защиты?"
                            consultantViewModel.sendQuickPrompt(prompt)
                            currentTab = 0
                        }
                    )
                }
            }
        } else {
            // Standard Mobile Phone Layout
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = currentTab == index,
                                onClick = { currentTab = index },
                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                label = {
                                    Text(
                                        tab.title,
                                        fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(
                        currentTab = currentTab,
                        consultantViewModel = consultantViewModel,
                        assets = assets,
                        newsList = newsList,
                        latestPortfolio = latestPortfolio,
                        bookmarkedMessages = bookmarkedMessages,
                        onNavigateToChat = { currentTab = 0 },
                        onOpenDisclaimer = { showDisclaimerDialog = true },
                        onOpenApiKeyDialog = { showApiKeyDialog = true },
                        onOpenPortfolioWizard = { showPortfolioWizard = true },
                        onAnalyzeCustomNews = { text ->
                            coroutineScope.launch { repository.analyzeCustomNewsWithAI(text) }
                        },
                        onDeleteNews = { id ->
                            coroutineScope.launch { repository.deleteNews(id) }
                        },
                        onAddAsset = { asset ->
                            coroutineScope.launch { repository.addOrUpdateAsset(asset) }
                        },
                        onDeleteAsset = { ticker ->
                            coroutineScope.launch { repository.deleteAsset(ticker) }
                        },
                        onToggleBookmark = { id, current ->
                            coroutineScope.launch { repository.toggleBookmark(id, current) }
                        },
                        onStressTestPortfolio = { portfolio ->
                            val prompt = "Проведи глубокий стресс-тест моего инвестиционного портфеля «${portfolio.title}» (${portfolio.targetAllocation}). Горизонт: ${portfolio.investmentHorizon}, Риск-профиль: ${portfolio.riskTolerance}. Каковы уязвимости и план защиты?"
                            consultantViewModel.sendQuickPrompt(prompt)
                            currentTab = 0
                        }
                    )
                }
            }
        }
    }

    if (showPortfolioWizard) {
        PortfolioWizardDialog(
            repository = repository,
            onDismiss = { showPortfolioWizard = false },
            onSaveAndOpenChat = { portfolio, openChat ->
                coroutineScope.launch {
                    repository.savePortfolio(portfolio)
                }
                showPortfolioWizard = false
                if (openChat) {
                    val prompt = "Проведи аудит и стресс-тест моего нового портфеля «${portfolio.title}» (${portfolio.targetAllocation}). Цель: ${portfolio.financialGoal}, Горизонт: ${portfolio.investmentHorizon}, Риск: ${portfolio.riskTolerance}. Что улучшить?"
                    consultantViewModel.sendQuickPrompt(prompt)
                    currentTab = 0
                }
            }
        )
    }

    if (showDisclaimerDialog) {
        DisclaimerDialog(
            onDismiss = { showDisclaimerDialog = false }
        )
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentApiKey = repository.getApiKey(),
            onSaveKey = { newKey ->
                repository.saveCustomApiKey(newKey)
            },
            onDismiss = { showApiKeyDialog = false }
        )
    }
}

@Composable
fun ScreenContent(
    currentTab: Int,
    consultantViewModel: ConsultantViewModel,
    assets: List<MarketAssetEntity>,
    newsList: List<FinancialNewsEntity>,
    latestPortfolio: PersonalizedPortfolioEntity?,
    bookmarkedMessages: List<com.example.data.local.ChatMessageEntity>,
    onNavigateToChat: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    onOpenApiKeyDialog: () -> Unit,
    onOpenPortfolioWizard: () -> Unit,
    onAnalyzeCustomNews: (String) -> Unit,
    onDeleteNews: (String) -> Unit,
    onAddAsset: (MarketAssetEntity) -> Unit,
    onDeleteAsset: (String) -> Unit,
    onToggleBookmark: (Long, Boolean) -> Unit,
    onStressTestPortfolio: (PersonalizedPortfolioEntity) -> Unit
) {
    when (currentTab) {
        0 -> ConsultantScreen(
            viewModel = consultantViewModel,
            onOpenDisclaimer = onOpenDisclaimer,
            onOpenPortfolioWizard = onOpenPortfolioWizard
        )
        1 -> FinancialNewsScreen(
            newsList = newsList,
            onAnalyzeCustomNews = onAnalyzeCustomNews,
            onDeleteNews = onDeleteNews,
            consultantViewModel = consultantViewModel,
            onNavigateToChat = onNavigateToChat
        )
        2 -> MarketCompassScreen(
            assets = assets,
            consultantViewModel = consultantViewModel,
            onNavigateToChat = onNavigateToChat,
            onOpenDisclaimer = onOpenDisclaimer,
            newsList = newsList
        )
        3 -> PortfolioAndArchiveScreen(
            bookmarkedMessages = bookmarkedMessages,
            userAssets = assets,
            latestPortfolio = latestPortfolio,
            onOpenWizard = onOpenPortfolioWizard,
            onStressTestPortfolio = onStressTestPortfolio,
            onToggleBookmark = onToggleBookmark,
            onAddAsset = onAddAsset,
            onDeleteAsset = onDeleteAsset,
            onOpenApiKeyDialog = onOpenApiKeyDialog,
            onOpenDisclaimer = onOpenDisclaimer,
            consultantViewModel = consultantViewModel,
            onNavigateToChat = onNavigateToChat
        )
    }
}
