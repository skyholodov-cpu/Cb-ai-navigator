package com.example.ui.market

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.consultant.ConsultantViewModel
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.Cyan80

data class AssetMetricData(
    val ticker: String,
    val name: String,
    val category: String,
    val peRatio: Double?, // Price-to-Earnings, null for non-equity like Gold/BTC
    val forwardPe: Double?,
    val dividendYieldPercent: Double, // %
    val volatilityPercent: Double, // 30d/1y volatility %
    val return1YearPercent: Double, // %
    val beta: Double?, // Market correlation
    val debtToEquity: Double?, // Lower is better
    val moatRating: String // "Широкий (Wide)", "Средний (Narrow)", "Базовый (None)"
)

val DEFAULT_COMPARISON_ASSETS = listOf(
    AssetMetricData(
        ticker = "NVDA",
        name = "NVIDIA Corp.",
        category = "Акции США",
        peRatio = 54.2,
        forwardPe = 31.8,
        dividendYieldPercent = 0.03,
        volatilityPercent = 38.5,
        return1YearPercent = 142.0,
        beta = 1.68,
        debtToEquity = 0.42,
        moatRating = "Широкий (CUDA / AI)"
    ),
    AssetMetricData(
        ticker = "MSFT",
        name = "Microsoft Corp.",
        category = "Акции США",
        peRatio = 34.6,
        forwardPe = 28.4,
        dividendYieldPercent = 0.72,
        volatilityPercent = 18.2,
        return1YearPercent = 29.5,
        beta = 0.89,
        debtToEquity = 0.35,
        moatRating = "Широкий (Облако / ПО)"
    ),
    AssetMetricData(
        ticker = "AAPL",
        name = "Apple Inc.",
        category = "Акции США",
        peRatio = 32.1,
        forwardPe = 27.0,
        dividendYieldPercent = 0.45,
        volatilityPercent = 17.4,
        return1YearPercent = 24.8,
        beta = 0.95,
        debtToEquity = 1.45,
        moatRating = "Широкий (Экосистема)"
    ),
    AssetMetricData(
        ticker = "SPY",
        name = "S&P 500 ETF",
        category = "Индексы",
        peRatio = 26.8,
        forwardPe = 22.1,
        dividendYieldPercent = 1.25,
        volatilityPercent = 12.8,
        return1YearPercent = 22.4,
        beta = 1.00,
        debtToEquity = null,
        moatRating = "Рыночный базис"
    ),
    AssetMetricData(
        ticker = "SBER",
        name = "Сбербанк ПАО",
        category = "Акции РФ",
        peRatio = 4.1,
        forwardPe = 3.8,
        dividendYieldPercent = 11.2,
        volatilityPercent = 24.0,
        return1YearPercent = 18.5,
        beta = 0.92,
        debtToEquity = 0.65,
        moatRating = "Широкий (Финтех / Монополия)"
    ),
    AssetMetricData(
        ticker = "XAU",
        name = "Золото (XAU/USD)",
        category = "Сырьё",
        peRatio = null,
        forwardPe = null,
        dividendYieldPercent = 0.0,
        volatilityPercent = 11.5,
        return1YearPercent = 28.4,
        beta = 0.12,
        debtToEquity = null,
        moatRating = "Суверенный резерв"
    ),
    AssetMetricData(
        ticker = "BTC",
        name = "Bitcoin",
        category = "Криптовалюты",
        peRatio = null,
        forwardPe = null,
        dividendYieldPercent = 0.0,
        volatilityPercent = 48.0,
        return1YearPercent = 118.0,
        beta = 2.15,
        debtToEquity = null,
        moatRating = "Сетевой эффект"
    ),
    AssetMetricData(
        ticker = "BRENT",
        name = "Нефть Brent",
        category = "Сырьё",
        peRatio = null,
        forwardPe = null,
        dividendYieldPercent = 0.0,
        volatilityPercent = 27.2,
        return1YearPercent = -4.5,
        beta = 0.65,
        debtToEquity = null,
        moatRating = "Энергетический баланс"
    )
)

enum class MetricCriteria(
    val title: String,
    val description: String,
    val formatValue: (AssetMetricData) -> String,
    val isBest: (AssetMetricData, List<AssetMetricData>) -> Boolean
) {
    PE_RATIO(
        title = "P/E (Цена / Прибыль)",
        description = "Мультипликатор окупаемости бизнеса. Чем ниже положительное значение, тем привлекательнее оценка.",
        formatValue = { it.peRatio?.let { pe -> String.format("%.1fx", pe) } ?: "— (Товар/Крипто)" },
        isBest = { item, all ->
            val pe = item.peRatio
            if (pe == null || pe <= 0) false
            else {
                val minPe = all.mapNotNull { it.peRatio }.filter { it > 0 }.minOrNull()
                minPe != null && pe == minPe
            }
        }
    ),
    FORWARD_PE(
        title = "Форвардный P/E",
        description = "Оценка цены к прогнозируемой будущей прибыли за 12 месяцев.",
        formatValue = { it.forwardPe?.let { fpe -> String.format("%.1fx", fpe) } ?: "—" },
        isBest = { item, all ->
            val fpe = item.forwardPe
            if (fpe == null || fpe <= 0) false
            else {
                val minFpe = all.mapNotNull { it.forwardPe }.filter { it > 0 }.minOrNull()
                minFpe != null && fpe == minFpe
            }
        }
    ),
    DIVIDEND_YIELD(
        title = "Дивидендная доходность",
        description = "Текущая дивидендная доходность к цене. Высокие регулярные выплаты защищают капитал.",
        formatValue = { String.format("%.2f%%", it.dividendYieldPercent) },
        isBest = { item, all ->
            val maxDiv = all.maxOfOrNull { it.dividendYieldPercent } ?: 0.0
            maxDiv > 0.1 && item.dividendYieldPercent == maxDiv
        }
    ),
    VOLATILITY(
        title = "Волатильность (Риск)",
        description = "Амплитуда колебаний цены за год. Чем ниже волатильность, тем стабильнее и безопаснее актив.",
        formatValue = { String.format("%.1f%%", it.volatilityPercent) },
        isBest = { item, all ->
            val minVol = all.minOfOrNull { it.volatilityPercent } ?: 0.0
            item.volatilityPercent == minVol
        }
    ),
    RETURN_1Y(
        title = "Доходность за 1 год",
        description = "Совокупный ценовой прирост актива за последние 12 месяцев.",
        formatValue = {
            val prefix = if (it.return1YearPercent >= 0) "+" else ""
            prefix + String.format("%.1f%%", it.return1YearPercent)
        },
        isBest = { item, all ->
            val maxRet = all.maxOfOrNull { it.return1YearPercent } ?: 0.0
            item.return1YearPercent == maxRet
        }
    ),
    BETA(
        title = "Бета (Чувствительность к рынку)",
        description = "Мера системного риска относительно широкого рынка (S&P 500 = 1.0). Меньше 1.0 — защитный профиль.",
        formatValue = { it.beta?.let { b -> String.format("%.2f", b) } ?: "—" },
        isBest = { item, all ->
            val betas = all.mapNotNull { it.beta }
            if (betas.isEmpty()) false
            else {
                val minBeta = betas.minOrNull()
                item.beta != null && item.beta == minBeta
            }
        }
    ),
    DEBT_EQUITY(
        title = "Долг / Капитал (D/E)",
        description = "Коэффициент финансового левериджа. Низкое значение указывает на устойчивость к высоким ставкам.",
        formatValue = { it.debtToEquity?.let { de -> String.format("%.2fx", de) } ?: "— (Без долга)" },
        isBest = { item, all ->
            val deList = all.mapNotNull { it.debtToEquity }
            if (deList.isEmpty()) false
            else {
                val minDe = deList.minOrNull()
                item.debtToEquity != null && item.debtToEquity == minDe
            }
        }
    ),
    MOAT_RATING(
        title = "Конкурентный ров (Moat)",
        description = "Защищённость бизнес-модели, технологическое или регуляторное преимущество компании.",
        formatValue = { it.moatRating },
        isBest = { item, _ -> item.moatRating.startsWith("Широкий", ignoreCase = true) }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssetComparisonMatrix(
    consultantViewModel: ConsultantViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allAssets = remember { mutableStateListOf(*DEFAULT_COMPARISON_ASSETS.toTypedArray()) }
    val selectedTickers = remember { mutableStateListOf("NVDA", "MSFT", "SBER", "XAU") }
    var showAddDialog by remember { mutableStateOf(false) }

    val activeAssets = remember(selectedTickers.toList(), allAssets.toList()) {
        allAssets.filter { it.ticker in selectedTickers }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Сравнительная матрица активов",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Интерактивное сопоставление мультипликаторов с подсветкой лучших показателей",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Asset Selection Chips
                    Text(
                        text = "Выбери активы для сравнения (минимум 2):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        allAssets.forEach { asset ->
                            val isSelected = asset.ticker in selectedTickers
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        if (selectedTickers.size > 2) {
                                            selectedTickers.remove(asset.ticker)
                                        }
                                    } else {
                                        selectedTickers.add(asset.ticker)
                                    }
                                },
                                label = { Text(asset.ticker) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("compare_chip_${asset.ticker}")
                            )
                        }

                        // Add custom asset chip
                        FilterChip(
                            selected = false,
                            onClick = { showAddDialog = true },
                            label = { Text("+ Добавить") },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.testTag("compare_add_asset_chip")
                        )
                    }
                }
            }
        }

        item {
            // Legend & Best Value Indicator Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Зелёным бейджем ★ подсвечен объективно лучший показатель в каждой категории",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AccentGreen,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // Comparison Table / Cards
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    val scrollState = rememberScrollState()

                    // Horizontal Scrollable Grid for Metrics
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                    ) {
                        // Table Header Row: Asset Names
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "Метрика / Актив",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.width(160.dp)
                            )

                            activeAssets.forEach { asset ->
                                Column(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .padding(horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = asset.ticker,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = asset.category,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Metric Rows
                        MetricCriteria.values().forEachIndexed { index, criteria ->
                            val isEven = index % 2 == 0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        if (isEven) MaterialTheme.colorScheme.surface
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    )
                                    .padding(vertical = 10.dp, horizontal = 12.dp)
                            ) {
                                // Metric Title Column
                                Column(modifier = Modifier.width(160.dp)) {
                                    Text(
                                        text = criteria.title,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                // Asset Value Columns
                                activeAssets.forEach { asset ->
                                    val formattedValue = criteria.formatValue(asset)
                                    val isWinning = criteria.isBest(asset, activeAssets)

                                    Box(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isWinning) {
                                            Surface(
                                                color = AccentGreen.copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.6f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = AccentGreen,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = formattedValue,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = AccentGreen,
                                                            fontSize = 11.sp
                                                        ),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = formattedValue,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                ),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Deep AI Comparison CTA Button
        item {
            ElevatedButton(
                onClick = {
                    val assetNames = activeAssets.joinToString(", ") { "${it.ticker} (${it.name})" }
                    val query = "Проведи глубокий сравнительный аудит выбранных активов: $assetNames. Сопоставь мультипликаторы P/E, дивиденды, волатильность, долговую нагрузку и конкурентные рвы. Сделай 5-ступенчатый разбор (краткий вывод, анализ, риски, сценарии, практические шаги)."
                    consultantViewModel.sendQuickPrompt(query)
                    onNavigateToChat()
                },
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("deep_compare_ai_button")
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Глубокое сравнение у AI Navigator",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    // Add Custom Asset Dialog
    if (showAddDialog) {
        AddCustomAssetDialog(
            onDismiss = { showAddDialog = false },
            onAddAsset = { newAsset ->
                allAssets.add(newAsset)
                selectedTickers.add(newAsset.ticker)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddCustomAssetDialog(
    onDismiss: () -> Unit,
    onAddAsset: (AssetMetricData) -> Unit
) {
    var ticker by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Акции") }
    var peInput by remember { mutableStateOf("") }
    var divInput by remember { mutableStateOf("") }
    var volInput by remember { mutableStateOf("") }
    var ret1yInput by remember { mutableStateOf("") }
    var betaInput by remember { mutableStateOf("1.0") }
    var moatInput by remember { mutableStateOf("Средний") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Добавить актив для сравнения", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase() },
                    label = { Text("Тикер (например: GOOGL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_custom_ticker")
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название (например: Alphabet Inc.)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = peInput,
                        onValueChange = { peInput = it },
                        label = { Text("P/E") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = divInput,
                        onValueChange = { divInput = it },
                        label = { Text("Дивиденды %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = volInput,
                        onValueChange = { volInput = it },
                        label = { Text("Волатильность %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ret1yInput,
                        onValueChange = { ret1yInput = it },
                        label = { Text("Доходность 1Г %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ticker.isNotBlank() && name.isNotBlank()) {
                        val pe = peInput.toDoubleOrNull()
                        val div = divInput.toDoubleOrNull() ?: 0.0
                        val vol = volInput.toDoubleOrNull() ?: 20.0
                        val ret1y = ret1yInput.toDoubleOrNull() ?: 10.0
                        val beta = betaInput.toDoubleOrNull() ?: 1.0

                        onAddAsset(
                            AssetMetricData(
                                ticker = ticker.trim(),
                                name = name.trim(),
                                category = category,
                                peRatio = pe,
                                forwardPe = pe?.let { it * 0.9 },
                                dividendYieldPercent = div,
                                volatilityPercent = vol,
                                return1YearPercent = ret1y,
                                beta = beta,
                                debtToEquity = 0.5,
                                moatRating = moatInput
                            )
                        )
                    }
                },
                enabled = ticker.isNotBlank() && name.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
