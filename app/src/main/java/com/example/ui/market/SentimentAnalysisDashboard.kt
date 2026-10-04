package com.example.ui.market

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FinancialNewsEntity
import com.example.ui.consultant.ConsultantViewModel
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.Cyan80
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class IndexSentimentItem(
    val name: String,
    val ticker: String,
    val sentimentScore: Int, // 0-100
    val sentimentLabel: String,
    val rsi: Double,
    val trendDirection: String, // "UP", "DOWN", "FLAT"
    val changeToday: String,
    val primaryDriver: String
)

val MAJOR_INDICES_SENTIMENT = listOf(
    IndexSentimentItem(
        name = "S&P 500",
        ticker = "SPX",
        sentimentScore = 68,
        sentimentLabel = "Умеренная жадность",
        rsi = 59.2,
        trendDirection = "UP",
        changeToday = "+0.45%",
        primaryDriver = "Сезон квартальных отчётностей и адаптация к политике ФРС"
    ),
    IndexSentimentItem(
        name = "Nasdaq 100",
        ticker = "NDX",
        sentimentScore = 74,
        sentimentLabel = "Жадность (AI Ралли)",
        rsi = 65.4,
        trendDirection = "UP",
        changeToday = "+0.88%",
        primaryDriver = "Инвестиционные бюджеты гиперскейлеров в серверы и чипы"
    ),
    IndexSentimentItem(
        name = "Dow Jones",
        ticker = "DJI",
        sentimentScore = 52,
        sentimentLabel = "Нейтральный баланс",
        rsi = 48.7,
        trendDirection = "FLAT",
        changeToday = "-0.12%",
        primaryDriver = "Ротация капитала между циклическими и защитными секторами"
    ),
    IndexSentimentItem(
        name = "Индекс МосБиржи",
        ticker = "IMOEX",
        sentimentScore = 58,
        sentimentLabel = "Осторожный оптимизм",
        rsi = 52.1,
        trendDirection = "UP",
        changeToday = "+0.35%",
        primaryDriver = "Высокие дивидендные доходности и удержание цен на сырьё"
    ),
    IndexSentimentItem(
        name = "DAX 40 (Европа)",
        ticker = "DAX",
        sentimentScore = 48,
        sentimentLabel = "Нейтрально",
        rsi = 46.8,
        trendDirection = "FLAT",
        changeToday = "+0.08%",
        primaryDriver = "Смягчение политики ЕЦБ на фоне умеренного промпроизводства"
    ),
    IndexSentimentItem(
        name = "Nikkei 225 (Япония)",
        ticker = "N225",
        sentimentScore = 64,
        sentimentLabel = "Позитив",
        rsi = 56.5,
        trendDirection = "UP",
        changeToday = "+0.62%",
        primaryDriver = "Корпоративные реформы управления и экспортная выручка"
    )
)

data class SentimentSubIndicator(
    val name: String,
    val valueLabel: String,
    val status: String,
    val score: Int, // 0-100
    val description: String
)

val SUB_INDICATORS = listOf(
    SentimentSubIndicator(
        name = "Рыночный импульс (Momentum)",
        valueLabel = "S&P 500 на +4.8% выше 125d MA",
        status = "Жадность",
        score = 72,
        description = "Индекс торгуется уверенно выше своей 125-дневной скользящей средней"
    ),
    SentimentSubIndicator(
        name = "Ширина рынка (Stock Price Breadth)",
        valueLabel = "McClellan Oscillator: +85",
        status = "Умеренная жадность",
        score = 64,
        description = "Количество растущих акций превышает падающие на Нью-Йоркской бирже"
    ),
    SentimentSubIndicator(
        name = "Волатильность (VIX Index)",
        valueLabel = "VIX: 14.65 (Ниже 50d MA)",
        status = "Экстремальная жадность",
        score = 80,
        description = "Премия за риск в опционах минимальна, участники спокойны"
    ),
    SentimentSubIndicator(
        name = "Спрос на защитные активы (Safe Haven)",
        valueLabel = "Акции опережают бонды на 2.4%",
        status = "Жадность",
        score = 67,
        description = "Инвесторы предпочитают риск удержанию казначейских облигаций"
    ),
    SentimentSubIndicator(
        name = "Спрос на высокодоходные бонды (Junk Bonds)",
        valueLabel = "Спред доходности: 295 б.п.",
        status = "Жадность",
        score = 70,
        description = "Кредитный спред сжат, риск дефолтов оценивается как низкий"
    )
)

@Composable
fun SentimentAnalysisDashboard(
    newsList: List<FinancialNewsEntity>,
    consultantViewModel: ConsultantViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Current aggregate Fear & Greed score
    val currentScore = 67

    // Aggregate news sentiments
    val totalNews = newsList.size
    val bullishCount = newsList.count { it.sentiment.equals("BULLISH", ignoreCase = true) }
    val bearishCount = newsList.count { it.sentiment.equals("BEARISH", ignoreCase = true) }
    val neutralCount = newsList.count { it.sentiment.equals("NEUTRAL", ignoreCase = true) }

    val bullishPercent = if (totalNews > 0) (bullishCount * 100 / totalNews) else 50
    val bearishPercent = if (totalNews > 0) (bearishCount * 100 / totalNews) else 25
    val neutralPercent = if (totalNews > 0) (neutralCount * 100 / totalNews) else 25

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Speedometer Gauge Card: Fear & Greed Index
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("fear_greed_gauge_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Индекс Страха и Жадности",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Fear & Greed Index (Рыночный барометр)",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speedometer Canvas Arc
                    FearAndGreedSpeedometer(
                        score = currentScore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Score Label & Badge
                    Text(
                        text = "$currentScore / 100",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = getScoreColor(currentScore)
                        )
                    )
                    Text(
                        text = getScoreLabel(currentScore),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = getScoreColor(currentScore)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Historical Comparisons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        HistoricalScoreItem("Вчера", 64, getScoreLabel(64))
                        HistoricalScoreItem("Неделю назад", 55, getScoreLabel(55))
                        HistoricalScoreItem("Месяц назад", 42, getScoreLabel(42))
                    }
                }
            }
        }

        // News Sentiment Aggregator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("news_sentiment_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Newspaper,
                            contentDescription = null,
                            tint = Cyan80,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Агрегатор настроений новостей AI",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Автоматический анализ тональности ленты: $totalNews публикаций в базе",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-color segmented progress bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    ) {
                        if (bullishPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(bullishPercent.toFloat().coerceAtLeast(1f))
                                    .fillMaxSize()
                                    .background(AccentGreen)
                            )
                        }
                        if (neutralPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(neutralPercent.toFloat().coerceAtLeast(1f))
                                    .fillMaxSize()
                                    .background(Cyan80)
                            )
                        }
                        if (bearishPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(bearishPercent.toFloat().coerceAtLeast(1f))
                                    .fillMaxSize()
                                    .background(AccentRed)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Бычьих: $bullishPercent%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Cyan80))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Нейтральных: $neutralPercent%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Медвежьих: $bearishPercent%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }
            }
        }

        // Major Stock Indices Sentiment Board
        item {
            Text(
                text = "Настроения основных фондовых индексов",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        MAJOR_INDICES_SENTIMENT.forEach { indexItem ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = indexItem.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = indexItem.ticker,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = indexItem.primaryDriver,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val trendColor = if (indexItem.trendDirection == "UP") AccentGreen
                                    else if (indexItem.trendDirection == "DOWN") AccentRed
                                    else Cyan80

                                    Icon(
                                        imageVector = if (indexItem.trendDirection == "UP") Icons.Default.ArrowUpward
                                        else if (indexItem.trendDirection == "DOWN") Icons.Default.ArrowDownward
                                        else Icons.Default.HorizontalRule,
                                        contentDescription = null,
                                        tint = trendColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = indexItem.changeToday,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = trendColor
                                        )
                                    )
                                }
                                Text(
                                    text = "RSI: ${indexItem.rsi}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sentiment score progress
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = indexItem.sentimentLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = getScoreColor(indexItem.sentimentScore),
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.width(130.dp)
                            )
                            LinearProgressIndicator(
                                progress = { indexItem.sentimentScore / 100f },
                                modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                                color = getScoreColor(indexItem.sentimentScore),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${indexItem.sentimentScore}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = getScoreColor(indexItem.sentimentScore)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Sub-indicators breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Компоненты индекса настроений",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SUB_INDICATORS.forEachIndexed { i, ind ->
                        if (i > 0) Spacer(modifier = Modifier.height(12.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ind.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = ind.status,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = getScoreColor(ind.score),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = ind.valueLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = ind.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Trigger AI Strategy Discussion
        item {
            ElevatedButton(
                onClick = {
                    val prompt = "Проведи глубокий аудит текущих настроений фондовых индексов. Индекс Страха и Жадности находится на отметке $currentScore (Жадность). S&P 500 показывает умеренную жадность (68), Nasdaq 100 на уровне 74, а VIX находится на отметке 14.65. Сделай 5-ступенчатый разбор (краткий вывод, анализ, риски перегрева, сценарии и практические шаги по хеджированию)."
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
                    .testTag("ai_sentiment_audit_button")
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Аудит настроений у AI Navigator",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun HistoricalScoreItem(period: String, score: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = period, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "$score", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = getScoreColor(score)))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = getScoreColor(score), fontSize = 10.sp))
    }
}

@Composable
fun FearAndGreedSpeedometer(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val radius = (width.coerceAtMost(height * 1.8f) / 2f) * 0.85f
        val center = Offset(width / 2f, height * 0.95f)
        val strokeWidth = 24.dp.toPx()

        val arcRect = Size(radius * 2, radius * 2)
        val topLeft = Offset(center.x - radius, center.y - radius)

        // Draw segmented arc: 180 deg total, from 180 to 360
        // Segment 1: Extreme Fear (0-25) -> angle 45 deg
        drawArc(
            color = Color(0xFFE53935),
            startAngle = 180f,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = topLeft,
            size = arcRect,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Segment 2: Fear (25-45) -> angle 36 deg
        drawArc(
            color = Color(0xFFFB8C00),
            startAngle = 225f,
            sweepAngle = 36f,
            useCenter = false,
            topLeft = topLeft,
            size = arcRect,
            style = Stroke(width = strokeWidth)
        )

        // Segment 3: Neutral (45-55) -> angle 18 deg
        drawArc(
            color = Color(0xFFFDD835),
            startAngle = 261f,
            sweepAngle = 18f,
            useCenter = false,
            topLeft = topLeft,
            size = arcRect,
            style = Stroke(width = strokeWidth)
        )

        // Segment 4: Greed (55-75) -> angle 36 deg
        drawArc(
            color = Color(0xFF7CB342),
            startAngle = 279f,
            sweepAngle = 36f,
            useCenter = false,
            topLeft = topLeft,
            size = arcRect,
            style = Stroke(width = strokeWidth)
        )

        // Segment 5: Extreme Greed (75-100) -> angle 45 deg
        drawArc(
            color = Color(0xFF2E7D32),
            startAngle = 315f,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = topLeft,
            size = arcRect,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw Needle based on animatedScore
        // 0 -> 180 deg, 100 -> 360 deg
        val currentAngleDeg = 180f + (animatedScore.value.coerceIn(0f, 100f) / 100f) * 180f
        val currentAngleRad = currentAngleDeg * (PI / 180f)

        val needleLength = radius * 0.82f
        val needleEnd = Offset(
            x = (center.x + needleLength * cos(currentAngleRad)).toFloat(),
            y = (center.y + needleLength * sin(currentAngleRad)).toFloat()
        )

        // Draw needle line
        drawLine(
            color = Color.White,
            start = center,
            end = needleEnd,
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Pivot center circles
        drawCircle(
            color = AccentGold,
            radius = 10.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.Black,
            radius = 4.dp.toPx(),
            center = center
        )
    }
}

fun getScoreColor(score: Int): Color {
    return when {
        score < 25 -> Color(0xFFE53935)
        score < 45 -> Color(0xFFFB8C00)
        score < 55 -> Color(0xFFFDD835)
        score < 75 -> Color(0xFF7CB342)
        else -> Color(0xFF2E7D32)
    }
}

fun getScoreLabel(score: Int): String {
    return when {
        score < 25 -> "Экстремальный страх"
        score < 45 -> "Страх"
        score < 55 -> "Нейтрально"
        score < 75 -> "Жадность"
        else -> "Экстремальная жадность"
    }
}
