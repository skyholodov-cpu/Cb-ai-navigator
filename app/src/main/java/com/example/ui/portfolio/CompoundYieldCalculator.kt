package com.example.ui.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.consultant.ConsultantViewModel
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.Cyan80
import java.text.DecimalFormat
import kotlin.math.pow

data class YearlyProjection(
    val year: Int,
    val investedTotal: Double,
    val nominalValue: Double,
    val realValue: Double
)

@Composable
fun CompoundYieldCalculator(
    consultantViewModel: ConsultantViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var initialDeposit by remember { mutableDoubleStateOf(300_000.0) }
    var monthlyContribution by remember { mutableDoubleStateOf(25_000.0) }
    var yearsHorizon by remember { mutableIntStateOf(10) }
    var expectedReturnRate by remember { mutableDoubleStateOf(12.0) } // %
    var inflationRate by remember { mutableDoubleStateOf(6.5) } // %

    var showYearlyBreakdown by remember { mutableStateOf(false) }

    // Number formatters
    val currencyFormatter = remember { DecimalFormat("#,### ₽") }
    val percentFormatter = remember { DecimalFormat("#0.0'%'") }

    // Projections computation
    val yearlyProjections = remember(initialDeposit, monthlyContribution, yearsHorizon, expectedReturnRate, inflationRate) {
        calculateProjections(
            initial = initialDeposit,
            monthly = monthlyContribution,
            years = yearsHorizon,
            returnRatePercent = expectedReturnRate,
            inflationRatePercent = inflationRate
        )
    }

    val finalProjection = yearlyProjections.lastOrNull() ?: YearlyProjection(yearsHorizon, 0.0, 0.0, 0.0)
    val totalInvested = finalProjection.investedTotal
    val nominalCapital = finalProjection.nominalValue
    val realCapital = finalProjection.realValue
    val compoundProfit = (nominalCapital - totalInvested).coerceAtLeast(0.0)
    val inflationErosion = (nominalCapital - realCapital).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("yield_calculator_header")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Калькулятор доходности и инфляции",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Сложный процент, ожидаемая доходность и реальная покупательская способность",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        // Key Metric Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nominal Capital
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("nominal_capital_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Номинальный капитал",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormatter.format(nominalCapital),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen
                            )
                        )
                        Text(
                            text = "+${currencyFormatter.format(compoundProfit)} дохода",
                            style = MaterialTheme.typography.labelSmall.copy(color = AccentGreen, fontSize = 10.sp)
                        )
                    }
                }

                // Real Inflation Adjusted Capital
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("real_capital_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Реальная стоимость",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormatter.format(realCapital),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        )
                        Text(
                            text = "С поправкой на $inflationRate% инфляции",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        )
                    }
                }
            }
        }

        // Secondary Info Row: Total Invested & Inflation Loss
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Собственные взносы:",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = currencyFormatter.format(totalInvested),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Cyan80)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Влияние инфляции (потери):",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "-${currencyFormatter.format(inflationErosion)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AccentRed)
                    )
                }
            }
        }

        // Interactive Visual Trajectory Chart (Canvas)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("compound_growth_chart_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Динамика роста портфеля",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$yearsHorizon лет",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Canvas Growth Trajectory
                    GrowthTrajectoryChart(
                        projections = yearlyProjections,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chart Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ChartLegendItem(color = AccentGreen, label = "Номинал (сложный %)")
                        ChartLegendItem(color = AccentGold, label = "Реальный капитал")
                        ChartLegendItem(color = Cyan80, label = "Вложено")
                    }
                }
            }
        }

        // Input Controls Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Параметры расчёта",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // 1. Initial Deposit Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Стартовый капитал:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                currencyFormatter.format(initialDeposit),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        Slider(
                            value = initialDeposit.toFloat(),
                            onValueChange = { initialDeposit = (it / 10_000).toInt() * 10_000.0 },
                            valueRange = 0f..3_000_000f,
                            steps = 29,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("slider_initial_deposit")
                        )
                    }

                    // 2. Monthly Contribution Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ежемесячное пополнение:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                currencyFormatter.format(monthlyContribution),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        Slider(
                            value = monthlyContribution.toFloat(),
                            onValueChange = { monthlyContribution = (it / 5_000).toInt() * 5_000.0 },
                            valueRange = 0f..200_000f,
                            steps = 39,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("slider_monthly_deposit")
                        )
                    }

                    // 3. Horizon (Years) Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Срок инвестирования:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "$yearsHorizon лет",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        Slider(
                            value = yearsHorizon.toFloat(),
                            onValueChange = { yearsHorizon = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 28,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("slider_horizon_years")
                        )
                    }

                    // 4. Expected Return Rate Slider & Presets
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ожидаемая годовая доходность:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                percentFormatter.format(expectedReturnRate),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AccentGreen)
                            )
                        }
                        Slider(
                            value = expectedReturnRate.toFloat(),
                            onValueChange = { expectedReturnRate = (it * 10).toInt() / 10.0 },
                            valueRange = 2f..25f,
                            colors = SliderDefaults.colors(thumbColor = AccentGreen, activeTrackColor = AccentGreen),
                            modifier = Modifier.testTag("slider_return_rate")
                        )

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetChip("Консервативно (8%)", expectedReturnRate == 8.0) { expectedReturnRate = 8.0 }
                            PresetChip("Баланс (12%)", expectedReturnRate == 12.0) { expectedReturnRate = 12.0 }
                            PresetChip("Агрессивно (18%)", expectedReturnRate == 18.0) { expectedReturnRate = 18.0 }
                        }
                    }

                    // 5. Expected Inflation Rate Slider & Presets
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ожидаемая годовая инфляция:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                percentFormatter.format(inflationRate),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AccentRed)
                            )
                        }
                        Slider(
                            value = inflationRate.toFloat(),
                            onValueChange = { inflationRate = (it * 10).toInt() / 10.0 },
                            valueRange = 2f..15f,
                            colors = SliderDefaults.colors(thumbColor = AccentRed, activeTrackColor = AccentRed),
                            modifier = Modifier.testTag("slider_inflation_rate")
                        )

                        // Inflation Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetChip("Низкая (4.5%)", inflationRate == 4.5) { inflationRate = 4.5 }
                            PresetChip("Базовая (6.5%)", inflationRate == 6.5) { inflationRate = 6.5 }
                            PresetChip("Высокая (10%)", inflationRate == 10.0) { inflationRate = 10.0 }
                        }
                    }
                }
            }
        }

        // Year-by-Year Breakdown Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showYearlyBreakdown = !showYearlyBreakdown }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Погодовая детализация капитала",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            imageVector = if (showYearlyBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = showYearlyBreakdown) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(vertical = 6.dp, horizontal = 8.dp)
                            ) {
                                Text("Год", modifier = Modifier.width(45.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Вложено", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Номинал", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Реальная", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            yearlyProjections.forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${p.year} г.", modifier = Modifier.width(45.dp), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text(currencyFormatter.format(p.investedTotal), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                                    Text(currencyFormatter.format(p.nominalValue), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold))
                                    Text(currencyFormatter.format(p.realValue), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = AccentGold))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Consult AI Navigator CTA
        item {
            ElevatedButton(
                onClick = {
                    val prompt = """
Проведи детальный инвестиционный аудит моего финансового плана:
- Стартовый капитал: ${currencyFormatter.format(initialDeposit)}
- Ежемесячные взносы: ${currencyFormatter.format(monthlyContribution)}
- Срок инвестирования: $yearsHorizon лет
- Ожидаемая годовая доходность: $expectedReturnRate%
- Ожидаемая инфляция: $inflationRate%
- Прогнозируемый номинальный капитал: ${currencyFormatter.format(nominalCapital)}
- Реальная покупательская способность: ${currencyFormatter.format(realCapital)} (потери от инфляции: ${currencyFormatter.format(inflationErosion)})

Сделай 5-ступенчатый разбор (краткий вывод, анализ инфляционных рисков, стресс-сценарии и практические шаги по выбору классов активов для защиты покупательской способности).
                    """.trimIndent()
                    consultantViewModel.sendQuickPrompt(prompt)
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
                    .testTag("consult_ai_yield_plan_button")
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Проанализировать план у AI Navigator",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun PresetChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
    }
}

@Composable
fun GrowthTrajectoryChart(
    projections: List<YearlyProjection>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (projections.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val paddingLeft = 16.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 24.dp.toPx()

        val chartWidth = w - paddingLeft - paddingRight
        val chartHeight = h - paddingTop - paddingBottom

        val maxVal = projections.maxOfOrNull { it.nominalValue }?.coerceAtLeast(1.0) ?: 1.0
        val minVal = 0.0

        val n = projections.size

        fun xForIndex(i: Int): Float {
            return paddingLeft + (i.toFloat() / (n - 1).coerceAtLeast(1)) * chartWidth
        }

        fun yForValue(v: Double): Float {
            val ratio = ((v - minVal) / (maxVal - minVal)).coerceIn(0.0, 1.0)
            return paddingTop + (1f - ratio.toFloat()) * chartHeight
        }

        // Draw horizontal grid lines
        for (grid in 1..3) {
            val y = paddingTop + (grid / 4f) * chartHeight
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(paddingLeft, y),
                end = Offset(w - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Path for Invested (Cyan80)
        val pathInvested = Path()
        projections.forEachIndexed { i, p ->
            val x = xForIndex(i)
            val y = yForValue(p.investedTotal)
            if (i == 0) pathInvested.moveTo(x, y) else pathInvested.lineTo(x, y)
        }

        // Path for Real Value (AccentGold)
        val pathReal = Path()
        projections.forEachIndexed { i, p ->
            val x = xForIndex(i)
            val y = yForValue(p.realValue)
            if (i == 0) pathReal.moveTo(x, y) else pathReal.lineTo(x, y)
        }

        // Path for Nominal Value (AccentGreen)
        val pathNominal = Path()
        val pathNominalArea = Path()
        pathNominalArea.moveTo(xForIndex(0), paddingTop + chartHeight)

        projections.forEachIndexed { i, p ->
            val x = xForIndex(i)
            val y = yForValue(p.nominalValue)
            if (i == 0) {
                pathNominal.moveTo(x, y)
                pathNominalArea.lineTo(x, y)
            } else {
                pathNominal.lineTo(x, y)
                pathNominalArea.lineTo(x, y)
            }
        }
        pathNominalArea.lineTo(xForIndex(n - 1), paddingTop + chartHeight)
        pathNominalArea.close()

        // Draw gradient area under Nominal curve
        drawPath(
            path = pathNominalArea,
            brush = Brush.verticalGradient(
                colors = listOf(AccentGreen.copy(alpha = 0.25f), Color.Transparent),
                startY = paddingTop,
                endY = paddingTop + chartHeight
            )
        )

        // Draw Invested Line
        drawPath(
            path = pathInvested,
            color = Cyan80,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Real Capital Line
        drawPath(
            path = pathReal,
            color = AccentGold,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Nominal Line
        drawPath(
            path = pathNominal,
            color = AccentGreen,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw End Points
        val lastIdx = n - 1
        drawCircle(
            color = AccentGreen,
            radius = 5.dp.toPx(),
            center = Offset(xForIndex(lastIdx), yForValue(projections[lastIdx].nominalValue))
        )
        drawCircle(
            color = AccentGold,
            radius = 4.5.dp.toPx(),
            center = Offset(xForIndex(lastIdx), yForValue(projections[lastIdx].realValue))
        )
    }
}

fun calculateProjections(
    initial: Double,
    monthly: Double,
    years: Int,
    returnRatePercent: Double,
    inflationRatePercent: Double
): List<YearlyProjection> {
    val result = mutableListOf<YearlyProjection>()
    val monthlyReturnRate = returnRatePercent / 100.0 / 12.0
    val annualInflationRate = inflationRatePercent / 100.0

    // Year 0
    result.add(
        YearlyProjection(
            year = 0,
            investedTotal = initial,
            nominalValue = initial,
            realValue = initial
        )
    )

    var currentNominal = initial
    var currentInvested = initial

    for (y in 1..years) {
        // Compound month-by-month for this year
        for (m in 1..12) {
            currentNominal = currentNominal * (1.0 + monthlyReturnRate) + monthly
            currentInvested += monthly
        }

        // Real value discounted by cumulative inflation: (1 + i)^y
        val cumulativeInflation = (1.0 + annualInflationRate).pow(y.toDouble())
        val currentReal = currentNominal / cumulativeInflation

        result.add(
            YearlyProjection(
                year = y,
                investedTotal = currentInvested,
                nominalValue = currentNominal,
                realValue = currentReal
            )
        )
    }

    return result
}
