package com.example.ui.portfolio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PersonalizedPortfolioEntity
import com.example.data.repository.NavigatorRepository
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.Cyan80

@Composable
fun PortfolioWizardDialog(
    repository: NavigatorRepository,
    onDismiss: () -> Unit,
    onSaveAndOpenChat: ((PersonalizedPortfolioEntity, Boolean) -> Unit)
) {
    var step by remember { mutableIntStateOf(1) } // 1: Цель, 2: Горизонт, 3: Риск, 4: Классы активов, 5: Результат

    var selectedGoal by remember { mutableStateOf("Умеренный долгосрочный рост") }
    var selectedHorizon by remember { mutableStateOf("3–7 лет (долгосрочный)") }
    var selectedRisk by remember { mutableStateOf("Умеренная (просадка до 12–15%)") }
    val selectedPreferences = remember { mutableStateListOf("Акции технологического сектора", "Золото и суверенный хедж") }

    var generatedPortfolio by remember { mutableStateOf<PersonalizedPortfolioEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (step < 5) "Конструктор портфеля • Шаг $step/4" else "Персональная стратегия",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "WizardStepTransition"
                ) { currentStep ->
                    when (currentStep) {
                        1 -> StepGoals(
                            selectedGoal = selectedGoal,
                            onGoalSelected = { selectedGoal = it }
                        )
                        2 -> StepHorizon(
                            selectedHorizon = selectedHorizon,
                            onHorizonSelected = { selectedHorizon = it }
                        )
                        3 -> StepRisk(
                            selectedRisk = selectedRisk,
                            onRiskSelected = { selectedRisk = it }
                        )
                        4 -> StepPreferences(
                            selectedPreferences = selectedPreferences,
                            onToggle = { pref ->
                                if (selectedPreferences.contains(pref)) {
                                    if (selectedPreferences.size > 1) selectedPreferences.remove(pref)
                                } else {
                                    selectedPreferences.add(pref)
                                }
                            }
                        )
                        5 -> generatedPortfolio?.let { portfolio ->
                            StepResult(portfolio = portfolio)
                        } ?: Text("Генерация стратегии...")
                    }
                }
            }
        },
        confirmButton = {
            if (step < 4) {
                Button(
                    onClick = { step++ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("wizard_next_button")
                ) {
                    Text("Далее")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            } else if (step == 4) {
                Button(
                    onClick = {
                        val portfolio = repository.buildPersonalizedPortfolio(
                            goal = selectedGoal,
                            horizon = selectedHorizon,
                            risk = selectedRisk,
                            preferences = selectedPreferences
                        )
                        generatedPortfolio = portfolio
                        step = 5
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("wizard_generate_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Сформировать портфель")
                }
            } else {
                Button(
                    onClick = {
                        generatedPortfolio?.let { onSaveAndOpenChat(it, false) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("wizard_save_button")
                ) {
                    Text("Сохранить портфель")
                }
            }
        },
        dismissButton = {
            if (step in 2..4) {
                TextButton(onClick = { step-- }) {
                    Text("Назад")
                }
            } else if (step == 5) {
                OutlinedButton(
                    onClick = {
                        generatedPortfolio?.let { onSaveAndOpenChat(it, true) }
                    }
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Аудит у Навигатора", fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
fun StepGoals(selectedGoal: String, onGoalSelected: (String) -> Unit) {
    val goals = listOf(
        Pair("Сохранение капитала", "Защита от инфляции и валютных рисков с минимальной просадкой"),
        Pair("Пассивный дивидендный доход", "Регулярный предсказуемый денежный поток от дивидендов и купонов"),
        Pair("Умеренный долгосрочный рост", "Классический сбалансированный прирост с разумной защитной подушкой"),
        Pair("Агрессивный прирост капитала", "Максимальная доходность на структурных технологических суперциклах")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "1. Какова твоя главная финансовая цель?",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "AI Navigator адаптирует структуру активов под желаемый баланс доходности и защищенности.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        goals.forEach { (title, desc) ->
            SelectableCard(
                title = title,
                subtitle = desc,
                isSelected = selectedGoal.startsWith(title.take(10)),
                onClick = { onGoalSelected(title) }
            )
        }
    }
}

@Composable
fun StepHorizon(selectedHorizon: String, onHorizonSelected: (String) -> Unit) {
    val horizons = listOf(
        Pair("Краткосрочный (< 1 года)", "Высокая ликвидность, отсутствие риска просадки к дате изъятия"),
        Pair("Среднесрочный (1–3 года)", "Фиксация доходности в качественных облигациях и дивидендных акциях"),
        Pair("Долгосрочный (3–7 лет)", "Возможность пересидеть рыночные циклы и получить премию за риск"),
        Pair("Стратегический (7+ лет)", "Формирование долгосрочного капитала и семейного фонда с сложным процентом")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "2. На какой срок планируются инвестиции?",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Горизонт инвестирования определяет допустимую долю волатильных акций и дюрацию облигаций.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        horizons.forEach { (title, desc) ->
            SelectableCard(
                title = title,
                subtitle = desc,
                isSelected = selectedHorizon.startsWith(title.take(8)),
                onClick = { onHorizonSelected(title) }
            )
        }
    }
}

@Composable
fun StepRisk(selectedRisk: String, onRiskSelected: (String) -> Unit) {
    val risks = listOf(
        Pair("Консервативная (просадка до 5%)", "Приоритет сохранности капитала, минимум волатильности"),
        Pair("Умеренная (просадка до 12–15%)", "Спокойное отношение к умеренным колебаниям ради доходности выше инфляции"),
        Pair("Динамичная (просадка до 20–25%)", "Готовность к временным коррекциям ради опережения бенчмарков"),
        Pair("Агрессивная (просадка 30%+)", "Фокус на максимальном росте; высокая эмоциональная дисциплина")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "3. Твоя толерантность к риску и просадкам?",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Честная оценка психологической готовности к временным рыночным откатам.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        risks.forEach { (title, desc) ->
            SelectableCard(
                title = title,
                subtitle = desc,
                isSelected = selectedRisk.startsWith(title.take(8)),
                onClick = { onRiskSelected(title) }
            )
        }
    }
}

@Composable
fun StepPreferences(selectedPreferences: List<String>, onToggle: (String) -> Unit) {
    val options = listOf(
        "Акции технологического сектора",
        "Золото и суверенный хедж",
        "Облигации и денежный рынок",
        "Криптовалюты и цифровые активы",
        "Нефтегаз и сырьевой цикл",
        "Дивидендные аристократы"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "4. Предпочтительные классы активов",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Выбери классы активов, которые ты хочешь видеть в ядре своей стратегии:",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        options.forEach { option ->
            val checked = selectedPreferences.contains(option)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(option) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (checked) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StepResult(portfolio: PersonalizedPortfolioEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AccentGreen.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "СТРАТЕГИЯ AI NAVIGATOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = portfolio.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // Target Allocation Visualization
        Text(
            text = "Целевое распределение активов:",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        AllocationBreakdownBar(portfolio = portfolio)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Ожидаемая доходность:", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Text(portfolio.expectedAnnualReturn, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AccentGreen))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Оценка макс. просадки:", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Text(portfolio.maxEstimatedDrawdown, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AccentGold))
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "Стратегический тезис:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = portfolio.strategicRationale,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                )
            }
        }
    }
}

@Composable
fun AllocationBreakdownBar(portfolio: PersonalizedPortfolioEntity) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
        ) {
            if (portfolio.equityShare > 0) {
                Box(modifier = Modifier.weight(portfolio.equityShare.toFloat()).background(Cyan80))
            }
            if (portfolio.fixedIncomeShare > 0) {
                Box(modifier = Modifier.weight(portfolio.fixedIncomeShare.toFloat()).background(AccentGreen))
            }
            if (portfolio.goldCommoditiesShare > 0) {
                Box(modifier = Modifier.weight(portfolio.goldCommoditiesShare.toFloat()).background(AccentGold))
            }
            if (portfolio.alternativesShare > 0) {
                Box(modifier = Modifier.weight(portfolio.alternativesShare.toFloat()).background(Color(0xFFA78BFA)))
            }
            if (portfolio.cashShare > 0) {
                Box(modifier = Modifier.weight(portfolio.cashShare.toFloat()).background(Color(0xFF64748B)))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendPill("Акции ${portfolio.equityShare}%", Cyan80)
            LegendPill("Облигации ${portfolio.fixedIncomeShare}%", AccentGreen)
            LegendPill("Золото ${portfolio.goldCommoditiesShare}%", AccentGold)
            if (portfolio.alternativesShare > 0) {
                LegendPill("Альт. ${portfolio.alternativesShare}%", Color(0xFFA78BFA))
            }
            LegendPill("Кэш ${portfolio.cashShare}%", Color(0xFF64748B))
        }
    }
}

@Composable
fun LegendPill(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp))
    }
}

@Composable
fun SelectableCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}
