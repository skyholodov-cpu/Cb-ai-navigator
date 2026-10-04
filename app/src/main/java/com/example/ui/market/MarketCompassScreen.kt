package com.example.ui.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.FinancialNewsEntity
import com.example.data.local.MarketAssetEntity
import com.example.ui.consultant.ConsultantViewModel
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.Cyan80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketCompassScreen(
    assets: List<MarketAssetEntity>,
    consultantViewModel: ConsultantViewModel,
    onNavigateToChat: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    newsList: List<FinancialNewsEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedCompassTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Компас рынков",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Компас рынков",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onOpenDisclaimer,
                    modifier = Modifier.testTag("compass_disclaimer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Дисклеймер",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        TabRow(
            selectedTabIndex = selectedCompassTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().testTag("compass_tab_row")
        ) {
            Tab(
                selected = selectedCompassTab == 0,
                onClick = { selectedCompassTab = 0 },
                text = { Text("Обзор рынка", fontSize = 12.sp, fontWeight = if (selectedCompassTab == 0) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_compass_overview")
            )
            Tab(
                selected = selectedCompassTab == 1,
                onClick = { selectedCompassTab = 1 },
                text = { Text("Сравнение", fontSize = 12.sp, fontWeight = if (selectedCompassTab == 1) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_compass_compare")
            )
            Tab(
                selected = selectedCompassTab == 2,
                onClick = { selectedCompassTab = 2 },
                text = { Text("Страх и Жадность", fontSize = 12.sp, fontWeight = if (selectedCompassTab == 2) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_compass_sentiment")
            )
        }

        when (selectedCompassTab) {
            1 -> {
                AssetComparisonMatrix(
                    consultantViewModel = consultantViewModel,
                    onNavigateToChat = onNavigateToChat
                )
            }
            2 -> {
                SentimentAnalysisDashboard(
                    newsList = newsList,
                    consultantViewModel = consultantViewModel,
                    onNavigateToChat = onNavigateToChat
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            // Macro Sentiment Dashboard
            item {
                MacroBarometerCard()
            }

            // Market Assets Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ключевые бенчмарки и активы",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Аудит Навигатора",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Watchlist Asset Cards
            items(
                items = assets,
                key = { it.ticker }
            ) { asset ->
                MarketAssetCard(
                    asset = asset,
                    onRequestAnalysis = {
                        val prompt = "Проведи глубокий аудит актива ${asset.ticker} (${asset.name}). Краткий вывод, драйверы, риски, сценарии и практические шаги."
                        consultantViewModel.sendQuickPrompt(prompt)
                        onNavigateToChat()
                    }
                )
            }

            // Strategic Macro Themes Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Стратегические темы и суперциклы",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            item {
                StrategicThemeCard(
                    title = "AI-суперцикл в инфраструктуре",
                    category = "Технологический сектор",
                    summary = "Переход от обучения моделей к масштабному инференсу требует кратного роста серверных мощностей, сетевых коммутаторов и электроэнергии.",
                    tag = "Инфраструктура",
                    accentColor = Cyan80,
                    onClick = {
                        consultantViewModel.sendQuickPrompt(
                            "Дай стратегический разбор темы: AI-суперцикл в дата-центрах и полупроводниках. Какие компании выигрывают, где риски переоценки?"
                        )
                        onNavigateToChat()
                    }
                )
            }

            item {
                StrategicThemeCard(
                    title = "Золото и суверенная дедолларизация",
                    category = "Макро & Резервы",
                    summary = "Чистые закупки монетарного золота центральными банками Глобального Юга формируют долгосрочный структурный спрос независимый от процентных ставок.",
                    tag = "Хедж",
                    accentColor = AccentGold,
                    onClick = {
                        consultantViewModel.sendQuickPrompt(
                            "Проанализируй долгосрочные перспективы золота: спрос центральных банков, геополитическая премия и сценарии динамики."
                        )
                        onNavigateToChat()
                    }
                )
            }

            item {
                StrategicThemeCard(
                    title = "Управление инфляционными рисками и облигации",
                    category = "Долговой рынок",
                    summary = "В условиях плато монетарной политики дюрация портфеля требует калибровки: короткий край даёт высокий кэш-флоу, длинный чувствителен к смене цикла.",
                    tag = "Фиксированный доход",
                    accentColor = AccentGreen,
                    onClick = {
                        consultantViewModel.sendQuickPrompt(
                            "Как оптимально распределить капитал между акциями, облигациями и золотом в условиях плато ставок? Дай стресс-сценарий."
                        )
                        onNavigateToChat()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        }
    }
    }
}

@Composable
fun MacroBarometerCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "МАКРОЭКОНОМИЧЕСКИЙ БАРОМЕТР",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Режим: Дисциплинированный рост",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentGreen,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroIndicatorItem(
                    title = "Ставки ФРС/ЦБ",
                    value = "Плато / Пауза",
                    subtitle = "Точечное смягчение",
                    modifier = Modifier.weight(1f)
                )
                MacroIndicatorItem(
                    title = "Глобальная M2",
                    value = "Умеренный рост",
                    subtitle = "Поддержка ликвидности",
                    modifier = Modifier.weight(1f)
                )
                MacroIndicatorItem(
                    title = "Премия за риск",
                    value = "Нейтральная",
                    subtitle = "Фокус на FCF и долг",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MacroIndicatorItem(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp
            )
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun MarketAssetCard(
    asset: MarketAssetEntity,
    onRequestAnalysis: () -> Unit
) {
    val isPositive = asset.changePercent >= 0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = asset.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = asset.ticker,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = asset.priceFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = (if (isPositive) AccentGreen else AccentRed).copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isPositive) AccentGreen else AccentRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f%%", asset.changePercent),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) AccentGreen else AccentRed
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = asset.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            if (asset.aiThesisSummary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = asset.aiThesisSummary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ElevatedButton(
                onClick = onRequestAnalysis,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analyze_asset_${asset.ticker}")
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Запросить разбор у Навигатора",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun StrategicThemeCard(
    title: String,
    category: String,
    summary: String,
    tag: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Открыть стратегический аудит →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
