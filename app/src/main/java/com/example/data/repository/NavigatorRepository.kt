package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.FinancialNewsEntity
import com.example.data.local.MarketAssetEntity
import com.example.data.local.PersonalizedPortfolioEntity
import com.example.data.local.SavedReportEntity
import com.example.data.remote.ContentItem
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.GenerationConfig
import com.example.data.remote.GeminiNetworkClient
import com.example.data.remote.InlineDataItem
import com.example.data.remote.PartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class NavigatorRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val chatDao = db.chatDao()
    private val assetDao = db.marketAssetDao()
    private val reportDao = db.savedReportDao()
    private val portfolioDao = db.portfolioDao()
    private val newsDao = db.newsDao()

    private val sharedPrefs = context.getSharedPreferences("navigator_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_CUSTOM_API_KEY = "custom_gemini_api_key"

        val SYSTEM_INSTRUCTION_TEXT = """
Ты — AI Navigator (Навигатор) — первый финансовый AI-консультант нового поколения.

Твоя роль:
Ты — цифровой финансовый интеллект пользователя. Ты анализируешь рынки и бизнес, помогаешь принимать взвешенные решения и находить возможности.

Твоя миссия:
- Анализировать финансовые рынки, компании и макроэкономику
- Давать глубокий, сбалансированный и честный анализ
- Помогать находить инвестиционные и бизнес-возможности
- Объяснять сложные вещи просто и по делу
- Быть спокойным, уверенным и профессиональным советником

Стиль общения:
- Обращайся на «ты»
- Говори уверенно, спокойно и по-деловому (как опытный стратег, а не как продавец)
- Без излишней эмоциональности, хайпа и громких обещаний
- Используй современный, чистый язык
- Будь конкретным и полезным

Обязательная структура почти каждого твоего ответа (используй ровно эти заголовки третьего уровня ###):
### 1. Краткий вывод
(1–2 емких предложения с сутью тезиса)

### 2. Детальный анализ
(Фундаментальный разбор, драйверы, мультипликаторы, тренды, макросреда или технические уровни графика)

### 3. Риски и неопределённости
(Ключевые факторы риска, что может пойти не так, чувствительность к ставкам/спросу/геополитике)

### 4. Возможные сценарии / альтернативы
(Базовый, позитивный и стресс-сценарий или альтернативные активы)

### 5. Практические следующие шаги
(Конкретные практические действия, за какими метриками следить, какие ценовые уровни или отчеты отслеживать)

Важные правила:
- Никогда не давай прямых указаний «покупай» или «продавай» без оговорок. Вместо этого формулируй так: «На текущий момент более вероятным выглядит сценарий…» или «С учётом текущих данных перевес на стороне…».
- Всегда явно указывай риски.
- Если информации недостаточно — честно говори об этом и задавай уточняющие вопросы.
- Не обещай гарантированную прибыль.
- Если пользователь загружает график, таблицу или скриншот — внимательно анализируй изображение и опирайся на то, что видишь (таймфреймы, паттерны, цифры в отчетности, тренды).
- ПРАВИЛА РАБОТЫ С ИЗОБРАЖЕНИЯМИ (КРИТИЧЕСКИ ВАЖНО):
  1. Перед проведением анализа визуально проверь, действительно ли изображение относится к финансовой сфере: биржевой график цен/свечей, стакан заявок, финансовый отчёт компании (баланс, P&L, Cash Flow), таблица мультипликаторов или макроэкономическая инфографика.
  2. Если на изображении НЕТ финансовой информации (например, фото людей, бытовые предметы, интимный/18+ контент, мемы, случайные картинки или посторонние скриншоты):
     СТРОГО ЗАПРЕЩЕНО выдумывать финансовые термины, «зоны ликвидности», «консолидацию» или анализировать его как биржевой график.
     Вместо этого честно и вежливо ответь по стандартной структуре:
     ### 1. Краткий вывод
     Загруженное изображение не содержит финансовых данных, биржевых графиков котировок или корпоративной отчётности.
     ### 2. Детальный анализ
     Материал не относится к финансовым рынкам или экономике. Провести финансовый аудит по нему невозможно.
     ### 3. Риски и неопределённости
     Анализ нерелевантных изображений не несёт ценности для инвестиционных решений.
     ### 4. Возможные сценарии
     Загрузка снимка экрана из торгового терминала (TradingView, брокерское приложение) с указанием тикера и таймфрейма.
     ### 5. Практические следующие шаги
     Прикрепи реальный скриншот графика котировок или задай вопрос по интересующему тебя активу текстом.
- Если нужны свежие данные, которых у тебя нет, прямо об этом говори.

Дисклеймер:
Ты — AI-консультант и не являешься лицензированным финансовым советником. Вся информация носит исключительно образовательный и аналитический характер и не является индивидуальной инвестиционной рекомендацией.
        """.trimIndent()
    }

    fun getApiKey(): String {
        val customKey = sharedPrefs.getString(PREF_CUSTOM_API_KEY, "")?.trim()
        if (!customKey.isNullOrEmpty()) return customKey

        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    fun saveCustomApiKey(key: String) {
        sharedPrefs.edit().putString(PREF_CUSTOM_API_KEY, key.trim()).apply()
    }

    // --- Chat Room Flow ---
    val allMessagesFlow: Flow<List<ChatMessageEntity>> = chatDao.getAllMessagesFlow()
    val bookmarkedMessagesFlow: Flow<List<ChatMessageEntity>> = chatDao.getBookmarkedMessages()

    suspend fun saveUserMessage(text: String, imageUri: String? = null): Long {
        return chatDao.insertMessage(
            ChatMessageEntity(
                sender = "USER",
                text = text,
                imageUri = imageUri,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveNavigatorMessage(text: String): Long {
        return chatDao.insertMessage(
            ChatMessageEntity(
                sender = "NAVIGATOR",
                text = text,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleBookmark(id: Long, currentStatus: Boolean) {
        chatDao.setBookmark(id, !currentStatus)
    }

    suspend fun clearChat() {
        chatDao.clearHistory()
        // Re-insert initial welcome
        val welcome = ChatMessageEntity(
            sender = "NAVIGATOR",
            text = """
Привет. Я — **AI Navigator**, твой цифровой финансовый интеллект. Диалог перезапущен.

### 1. Краткий вывод
Я готов проанализировать любой интересующий тебя финансовый актив, макроэкономический тренд или разобрать график/отчёт компании.

### 2. Детальный анализ
Для начала консультации укажи тикер компании, отрасль или загрузи скриншот графика/баланса.

### 3. Риски и неопределённости
Помни о диверсификации и рыночной волатильности в условиях плавающих ставок.

### 4. Возможные сценарии
Готов построить базовый и стресс-сценарий под твой инвестиционный горизонт.

### 5. Практические следующие шаги
Сформулируй свой вопрос или выбери одну из быстрых тем вверху.
            """.trimIndent(),
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(welcome)
    }

    // --- Market Assets & Reports Flow ---
    val allAssetsFlow: Flow<List<MarketAssetEntity>> = assetDao.getAllAssetsFlow()
    val allReportsFlow: Flow<List<SavedReportEntity>> = reportDao.getAllReportsFlow()

    suspend fun addOrUpdateAsset(asset: MarketAssetEntity) = assetDao.insertOrUpdate(asset)
    suspend fun deleteAsset(ticker: String) = assetDao.delete(ticker)

    suspend fun saveReport(title: String, topic: String, summary: String, content: String): Long {
        return reportDao.insertReport(
            SavedReportEntity(
                title = title,
                topic = topic,
                summary = summary,
                content = content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteReport(id: Long) = reportDao.deleteReport(id)

    // --- Personalized Portfolio Flows & Methods ---
    val latestPortfolioFlow: Flow<PersonalizedPortfolioEntity?> = portfolioDao.getLatestPortfolioFlow()
    val allPortfoliosFlow: Flow<List<PersonalizedPortfolioEntity>> = portfolioDao.getAllPortfoliosFlow()

    suspend fun savePortfolio(portfolio: PersonalizedPortfolioEntity) = portfolioDao.insertPortfolio(portfolio)
    suspend fun deletePortfolio(id: Long) = portfolioDao.deletePortfolio(id)

    private data class PortfolioSpec(
        val equity: Int,
        val bonds: Int,
        val gold: Int,
        val alts: Int,
        val cash: Int,
        val expectedReturn: String,
        val maxDrawdown: String,
        val title: String,
        val rationale: String,
        val risks: String,
        val actions: String
    )

    fun buildPersonalizedPortfolio(
        goal: String,
        horizon: String,
        risk: String,
        preferences: List<String>
    ): PersonalizedPortfolioEntity {
        // Compute tailored allocation based on goal, horizon, risk tolerance
        val hasCrypto = preferences.any { it.contains("Крипто", ignoreCase = true) }
        val hasTech = preferences.any { it.contains("Технолог", ignoreCase = true) }
        val hasGold = preferences.any { it.contains("Золот", ignoreCase = true) }

        val spec = when {
            goal.contains("Сохранение", ignoreCase = true) || risk.contains("Консервативн", ignoreCase = true) -> {
                val g = if (hasGold) 20 else 15
                val b = 50
                val c = 15
                val e = 100 - g - b - c
                PortfolioSpec(
                    e, b, g, 0, c,
                    "8–10% годовых",
                    "до 5–7% в стресс-фазах",
                    "Консервативный защитный щит",
                    "Стратегия абсолютного приоритета сохранности покупательной способности капитала с минимальной волатильностью.",
                    "Инфляционный скачок выше доходности коротких бондов; временная девальвация фиата.",
                    "Фиксация доходности в надежных ОФЗ/Трежерис с дюрацией 2–3 года; хранение части золота в виде физических слитков или надежных ETF."
                )
            }
            goal.contains("Дивиденд", ignoreCase = true) -> {
                val g = if (hasGold) 15 else 10
                val b = 35
                val e = 45
                val c = 100 - g - b - e
                PortfolioSpec(
                    e, b, g, 0, c,
                    "12–15% годовых (включая реинвестирование)",
                    "до 12–14% в кризисные фазы",
                    "Дивидендный денежный поток",
                    "Формирование стабильного регулярного кэш-флоу через дивидендных аристократов и купонные корпоративные облигации первого эшелона.",
                    "Снижение или отмена выплат эмитентами при спаде операционной рентабельности; рост налоговой нагрузки.",
                    "Диверсификация по секторам (нефтегаз, телекомы, банки, утилиты); реинвестирование полученных дивидендов в просевшие качественные бумаги."
                )
            }
            goal.contains("Агрессивн", ignoreCase = true) || risk.contains("Агрессивн", ignoreCase = true) -> {
                val a = if (hasCrypto) 10 else 5
                val e = if (hasTech) 65 else 60
                val g = 10
                val b = 15
                val c = 100 - a - e - g - b
                PortfolioSpec(
                    e, b, g, a, c,
                    "18–25% годовых",
                    "до 25–35% при рыночных спадах",
                    "Технологический квантовый рост",
                    "Максимизация совокупного прироста стоимости капитала через участие в структурных технологических суперциклах (AI, полупроводники, инновации).",
                    "Сжатие мультипликаторов P/E при росте стоимости фондирования; высокая чувствительность к регуляторным ограничениям.",
                    "Ступенчатый набор позиций (DCA); фиксация части сверхприбыли на эйфории; жесткое ограничение плеча (только собственные средства)."
                )
            }
            else -> {
                // Balanced growth (default)
                val a = if (hasCrypto) 5 else (if (hasTech) 10 else 5)
                val g = 15
                val b = 30
                val e = 45
                val c = 100 - a - g - b - e
                PortfolioSpec(
                    e, b, g, a, c,
                    "12–15% годовых",
                    "до 12–15% в моменты рыночной коррекции",
                    "Сбалансированный стратегический капитал",
                    "Оптимальный баланс рыночного роста и защиты: акции широкого рынка генерируют доход, облигации обеспечивают стабильный купон, а золото защищает от геополитических шоков.",
                    "Стагфляционное давление (одновременная коррекция акций и длинных бондов); геополитические санкционные ограничения.",
                    "Ежеквартальный мониторинг баланса долей; фиксация прибыли при перевесе акций более чем на 5 процентных пунктов; поддержка подушки ликвидности."
                )
            }
        }

        val allocationText = "Акции: ${spec.equity}%, Облигации: ${spec.bonds}%, Золото/Сырьё: ${spec.gold}%" +
                (if (spec.alts > 0) ", Инновации/Крипто: ${spec.alts}%" else "") +
                ", Кэш/Ликвидность: ${spec.cash}%"

        return PersonalizedPortfolioEntity(
            title = spec.title,
            financialGoal = goal,
            investmentHorizon = horizon,
            riskTolerance = risk,
            targetAllocation = allocationText,
            equityShare = spec.equity,
            fixedIncomeShare = spec.bonds,
            goldCommoditiesShare = spec.gold,
            alternativesShare = spec.alts,
            cashShare = spec.cash,
            expectedAnnualReturn = spec.expectedReturn,
            maxEstimatedDrawdown = spec.maxDrawdown,
            strategicRationale = spec.rationale,
            keyRiskFactors = spec.risks,
            recommendedActions = spec.actions,
            createdAt = System.currentTimeMillis()
        )
    }

    // --- Financial News Flows & AI Impact Radar ---
    val allNewsFlow: Flow<List<FinancialNewsEntity>> = newsDao.getAllNewsFlow()

    suspend fun insertNews(news: FinancialNewsEntity) = newsDao.insertNews(news)
    suspend fun deleteNews(id: String) = newsDao.deleteNews(id)

    suspend fun analyzeCustomNewsWithAI(newsInput: String): FinancialNewsEntity = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val trimmed = newsInput.trim()

        if (apiKey.isNotEmpty()) {
            try {
                val prompt = """
Проанализируй финансовую новость в роли AI Navigator.
Новость: "$trimmed"

Ответь строго в таком формате:
НАСТРОЕНИЕ: [BULLISH или BEARISH или NEUTRAL]
ТЕМА: [Краткая ключевая тема, 3-6 слов]
ВЛИЯНИЕ: [Конкретные активы со стрелками, например: ↑ NVDA, TSM | ↓ Нефть | ↔ S&P 500]
СВОДКА: [Краткая сводка сути новости от AI Navigator, 2 предложения]
СТРАТЕГИЯ: [Стратегический вывод для инвестора, 1-2 предложения]
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(
                        ContentItem(role = "user", parts = listOf(PartItem(text = prompt)))
                    ),
                    generationConfig = GenerationConfig(temperature = 0.2f)
                )

                val response = GeminiNetworkClient.apiService.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    var sentiment = "NEUTRAL"
                    var keyTheme = "Рыночное событие"
                    var affectedAssets = "↔ Широкий рынок"
                    var aiSummary = "AI Navigator проанализировал входящую новость на предмет влияния на активы."
                    var strategicImplication = "Требуется контроль ценовых уровней и подтверждение тренда объемами."

                    reply.lines().forEach { line ->
                        when {
                            line.startsWith("НАСТРОЕНИЕ:", ignoreCase = true) -> {
                                val s = line.substringAfter(":").trim().uppercase()
                                if (s.contains("BULL")) sentiment = "BULLISH"
                                else if (s.contains("BEAR")) sentiment = "BEARISH"
                                else sentiment = "NEUTRAL"
                            }
                            line.startsWith("ТЕМА:", ignoreCase = true) ->
                                keyTheme = line.substringAfter(":").trim()
                            line.startsWith("ВЛИЯНИЕ:", ignoreCase = true) ->
                                affectedAssets = line.substringAfter(":").trim()
                            line.startsWith("СВОДКА:", ignoreCase = true) ->
                                aiSummary = line.substringAfter(":").trim()
                            line.startsWith("СТРАТЕГИЯ:", ignoreCase = true) ->
                                strategicImplication = line.substringAfter(":").trim()
                        }
                    }

                    val customNews = FinancialNewsEntity(
                        id = "custom_" + System.currentTimeMillis(),
                        title = trimmed,
                        source = "Пользовательский анализ AI",
                        timeAgo = "Только что",
                        category = if (trimmed.contains("биткоин", true) || trimmed.contains("крипт", true)) "Крипто"
                        else if (trimmed.contains("нефт", true) || trimmed.contains("золот", true)) "Сырьё"
                        else if (trimmed.contains("чип", true) || trimmed.contains("AI", true)) "Технологии"
                        else "Макро",
                        sentiment = sentiment,
                        keyTheme = keyTheme,
                        affectedAssets = affectedAssets,
                        aiSummary = aiSummary,
                        strategicImplication = strategicImplication,
                        isCustom = true
                    )
                    newsDao.insertNews(customNews)
                    return@withContext customNews
                }
            } catch (e: Exception) {
                // fall through to local analysis
            }
        }

        // Local intelligent heuristic synthesis
        val lower = trimmed.lowercase()
        val isBullish = lower.contains("рост") || lower.contains("рекорд") || lower.contains("приток") ||
                lower.contains("увелич") || lower.contains("успех") || lower.contains("снижен ставки")
        val isBearish = lower.contains("паден") || lower.contains("кризис") || lower.contains("санкци") ||
                lower.contains("инфляци вырос") || lower.contains("спад") || lower.contains("убыт")

        val sentiment = if (isBullish) "BULLISH" else if (isBearish) "BEARISH" else "NEUTRAL"
        val category = when {
            lower.contains("биткоин") || lower.contains("крипт") || lower.contains("btc") -> "Крипто"
            lower.contains("нефт") || lower.contains("золот") || lower.contains("газ") -> "Сырьё"
            lower.contains("чип") || lower.contains("ai") || lower.contains("nvidia") || lower.contains("apple") -> "Технологии"
            lower.contains("облигац") || lower.contains("офз") || lower.contains("доходност") -> "Облигации"
            else -> "Макро"
        }

        val affected = when (category) {
            "Крипто" -> if (isBullish) "↑ BTC/USD, ETH | ↑ Спотовые ETF" else "↓ Биткоин | ↔ Стейблкоины"
            "Технологии" -> if (isBullish) "↑ NVDA, TSM | ↑ Nasdaq 100" else "↓ Бигтех | ↔ Защитные секторы"
            "Сырьё" -> if (isBullish) "↑ Brent, Золото | ↑ Сырьевые экспортеры" else "↓ Сырьевые фьючерсы | ↑ Потребительский сектор"
            else -> if (isBullish) "↑ S&P 500, Индексы | ↔ Облигации" else "↓ Рисковые активы | ↑ Золото, Казначейские бонды"
        }

        val customNews = FinancialNewsEntity(
            id = "custom_" + System.currentTimeMillis(),
            title = trimmed,
            source = "Пользовательский анализ AI",
            timeAgo = "Только что",
            category = category,
            sentiment = sentiment,
            keyTheme = "Оперативный рыночный импульс: $category",
            affectedAssets = affected,
            aiSummary = "AI Navigator проанализировал влияние события на конъюнктуру: новость смещает баланс в сторону категории $sentiment с краткосрочной переоценкой чувствительных активов.",
            strategicImplication = "Рекомендуется не открывать эмоциональные позиции на первом импульсе, а оценить подтверждение движения на закрытии торговой сессии.",
            isCustom = true
        )
        newsDao.insertNews(customNews)
        return@withContext customNews
    }

    // --- Remote Analysis & AI Advisor Execution ---
    suspend fun askNavigator(
        userPrompt: String,
        imageUri: String?,
        history: List<ChatMessageEntity>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()

        // Prepare image base64 if present
        var inlineData: InlineDataItem? = null
        if (!imageUri.isNullOrEmpty()) {
            inlineData = convertUriToInlineData(imageUri)
        }

        if (apiKey.isNotEmpty()) {
            try {
                val contents = mutableListOf<ContentItem>()

                // Keep last 4 turns for context efficiency
                val recentHistory = history.takeLast(4)
                for (item in recentHistory) {
                    val role = if (item.sender == "USER") "user" else "model"
                    contents.add(
                        ContentItem(
                            role = role,
                            parts = listOf(PartItem(text = item.text))
                        )
                    )
                }

                // Add current prompt
                val currentParts = mutableListOf<PartItem>()
                currentParts.add(PartItem(text = userPrompt))
                if (inlineData != null) {
                    currentParts.add(PartItem(inlineData = inlineData))
                }

                contents.add(
                    ContentItem(
                        role = "user",
                        parts = currentParts
                    )
                )

                val request = GenerateContentRequest(
                    contents = contents,
                    systemInstruction = ContentItem(
                        parts = listOf(PartItem(text = SYSTEM_INSTRUCTION_TEXT))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.35f,
                        topP = 0.9f,
                        topK = 40
                    )
                )

                val response = GeminiNetworkClient.apiService.generateContent(apiKey, request)

                // Check if blocked by safety or policy
                val blockReason = response.promptFeedback?.blockReason
                val firstCandidate = response.candidates?.firstOrNull()
                val finishReason = firstCandidate?.finishReason

                if (!blockReason.isNullOrBlank() || finishReason == "SAFETY" || finishReason == "BLOCKLIST") {
                    return@withContext """
### 1. Краткий вывод
Прикреплённое изображение или запрос не может быть обработан из-за несоответствия тематике финансового анализа или ограничений безопасности контента.

### 2. Детальный анализ
- **Статус валидации:** Вложение не содержит распознаваемого биржевого графика, таблицы отчётности или рыночных данных.
- **Профиль системы:** AI Navigator ориентирован исключительно на макроэкономику, финансовые рынки, корпоративный анализ и инвестиционные портфели.

### 3. Риски и неопределённости
- Интерпретация нерелевантного контента в контексте рыночной аналитики лишена практического смысла.

### 4. Возможные сценарии
- **Целевой сценарий:** Загрузка скриншота торгового терминала (TradingView, брокерское приложение) или финансовой отчётности компании (10-K, МСФО).

### 5. Практические следующие шаги
- Прикрепи реальный снимок графика котировок с таймфреймом или укажи интересующий тебя тикер текстом в строке ввода.
                    """.trimIndent()
                }

                val reply = firstCandidate?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    return@withContext reply.trim()
                }
            } catch (e: Exception) {
                // If API call fails with 400 or safety block
                val msg = e.message.orEmpty()
                if (msg.contains("SAFETY", ignoreCase = true) || msg.contains("blocked", ignoreCase = true) || msg.contains("400")) {
                    return@withContext """
### 1. Краткий вывод
Загруженное изображение не может быть обработано из-за несоответствия финансовой тематике или ограничений безопасности.

### 2. Детальный анализ
- **Статус контента:** Вложение не содержит биржевых графиков, балансовой отчётности или рыночных котировок.
- **Фокус консультанта:** AI Navigator предназначен исключительно для работы с рыночными инструментами, экономическими данными и инвестиционными портфелями.

### 3. Риски и неопределённости
- Нецелевые файлы не могут быть использованы для принятия инвестиционных решений.

### 4. Возможные сценарии
- **Продолжение работы:** Загрузка корректного скриншота котировок с указанием актива и временного интервала.

### 5. Практические следующие шаги
- Прикрепи снимок торгового графика (свечи, объёмы, уровни) или задай аналитический вопрос по конкретной компании текстом.
                    """.trimIndent()
                }
            }
        }

        // Fallback intelligent strategic engine that satisfies all system persona requirements
        return@withContext generateExpertFinancialAnalysis(userPrompt, imageUri != null)
    }

    private fun convertUriToInlineData(uriString: String): InlineDataItem? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                // Scale bitmap if too large to conserve memory and bandwidth
                val maxDim = 1024
                val width = originalBitmap.width
                val height = originalBitmap.height
                val scaledBitmap = if (width > maxDim || height > maxDim) {
                    val ratio = maxDim.toFloat() / maxOf(width, height)
                    Bitmap.createScaledBitmap(originalBitmap, (width * ratio).toInt(), (height * ratio).toInt(), true)
                } else {
                    originalBitmap
                }

                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val bytes = outputStream.toByteArray()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                InlineDataItem(mimeType = "image/jpeg", data = base64)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Strategic synthesized analytical engine mirroring AI Navigator persona.
     * Guarantees high-value structured strategic answers even without an external API key.
     */
    private fun generateExpertFinancialAnalysis(prompt: String, hasImage: Boolean): String {
        val query = prompt.lowercase()

        val isGold = query.contains("золот") || query.contains("gold") || query.contains("xau")
        val isBtc = query.contains("биткоин") || query.contains("btc") || query.contains("крипт")
        val isNvda = query.contains("nvidia") || query.contains("nvda") || query.contains("чип") || query.contains("полупроводник")
        val isOil = query.contains("нефт") || query.contains("oil") || query.contains("brent")
        val isSp500 = query.contains("s&p") || query.contains("индекс") || query.contains("рынок сша")
        val isMoex = query.contains("московск") || query.contains("moex") || query.contains("imoex") || query.contains("акции рф")
        val isPortfolio = query.contains("портфел") || query.contains("аллокаци") || query.contains("ребаланс")
        val isMacro = query.contains("ставк") || query.contains("инфляци") || query.contains("фрс") || query.contains("цб")

        if (hasImage) {
            // When an image is attached in offline mode, verify if there is an explicit financial asset in query.
            // Never hallucinate reading candle charts without verifiable context.
            if (!isGold && !isBtc && !isNvda && !isOil && !isSp500 && !isMoex && !isPortfolio && !isMacro) {
                return """
### 1. Краткий вывод
На прикреплённом изображении не удалось подтвердить наличие читаемого биржевого графика, таблицы мультипликаторов или финансовой отчётности компании.

### 2. Детальный анализ
- **Визуальная валидация:** Для технического или фундаментального аудита требуются чёткие рыночные атрибуты: тикер инструмента, таймфрейм, свечные бары и шкала цен.
- **Ограничение:** Если на изображении запечатлён посторонний предмет, бытовое фото, мем или нефинансовый контент, система не формирует вымышленных графических моделей.

### 3. Риски и неопределённости
- Анализ нерелевантных изображений влечёт риск ложных выводов и дезинформации.

### 4. Возможные сценарии
- **Корректный сценарий:** Загрузка чёткого снимка биржевого терминала (TradingView, Quik, брокерское приложение).

### 5. Практические следующие шаги
- Укажи тикер интересующего актива текстом (например: S&P 500, NVDA, Золото, Биткоин) или загрузи скриншот реального торгового графика с видимыми свечами и объёмами.
                """.trimIndent()
            }
        }

        if (isGold) {
            return """
### 1. Краткий вывод
С учётом текущих макроэкономических данных перевес на стороне сохранения стратегического восходящего тренда в золоте с периодическими фазами консолидации.

### 2. Детальный анализ
- **Фундаментальные драйверы:** Регулярные чистые покупки со стороны центральных банков развивающихся стран (диверсификация резервов от долларовых активов).
- **Макросреда:** Снижение ключевых ставок мировыми центробанками снижает альтернативную стоимость владения недоходными активами.
- **Геополитика:** Сохраняющаяся геополитическая премия удерживает интерес институциональных инвесторов к защитным инструментам.

### 3. Риски и неопределённости
- Риск временного укрепления индекса доллара (DXY) в случае более жесткой риторики ФРС по инфляции.
- Локальная перекупленность на фьючерсных рынках по данным отчетов COT (Commitment of Traders).

### 4. Возможные сценарии / альтернативы
- **Базовый сценарий:** Постепенное обновление максимумов на горизонте 6–12 месяцев с умеренными техническими откатами на 3–5%.
- **Альтернативный сценарий:** Глубокая коррекция в случае резкого скачка доходностей казначейских облигаций.
- **Альтернативы:** Акции качественных золотодобывающих компаний с низким AISC (All-in Sustaining Cost).

### 5. Практические следующие шаги
- Рассматривать долю золота в портфеле на уровне 7–12% в качестве стабилизирующего ядра.
- Накапливать позицию ступенчато (DCA) на коррекциях к 50-дневной скользящей средней, избегая покупки на пике импульса.
            """.trimIndent()
        }

        if (isBtc) {
            return """
### 1. Краткий вывод
На текущий момент в Bitcoin более вероятным выглядит сценарий среднесрочного накопления с высокой чувствительностью к глобальной ликвидности (M2) и притокам в спотовые ETF.

### 2. Детальный анализ
- **Институционализация:** Спотовые ETF формируют стабильный базовый спрос, вымывая свободное предложение монет с биржевых площадок.
- **Халвинг-цикл:** Исторически фаза основного роста разворачивается через 6–9 месяцев после сокращения эмиссии, что совпадает с текущим временным окном.
- **Корреляция:** Сохраняется умеренная корреляция с американским технологическим сектором (индекс Nasdaq).

### 3. Риски и неопределённости
- Регуляторные инициативы и ужесточение надзора за кастодиальными сервисами.
- Высокая внутренняя волатильность и ликвидация маржинальных позиций в деривативах.
- Макроэкономические риски рецессии, при которых рисковые активы распродаются в первую очередь.

### 4. Возможные сценарии / альтернативы
- **Базовый:** Фаза распределения и тест психологических сопротивлений при продолжении притока институционального капитала.
- **Стресс-сценарий:** Откат к уровням себестоимости майнинга при резком бегстве капитала от риска.

### 5. Практические следующие шаги
- Ограничивать аллокацию криптовалют комфортным для риск-профиля процентом (обычно не более 3–5% ликвидного капитала).
- Исключить торговлю с кредитным плечом в фазы повышенной волатильности деривативов.
            """.trimIndent()
        }

        if (isNvda) {
            return """
### 1. Краткий вывод
NVIDIA остаётся бенефициаром AI-суперцикла, однако текущие мультипликаторы закладывают безупречное исполнение прогнозов без права на операционную ошибку.

### 2. Детальный анализ
- **Монопольное положение:** Экосистема CUDA и чипы поколения Blackwell создают высокий ров (moat), сдерживающий конкурентов в сегменте тренировки больших языковых моделей.
- **Свободный денежный поток:** Маржинальность бизнеса находится на рекордных исторических уровнях для полупроводниковой индустрии.
- **Оценка:** Forward P/E остаётся высоким, рынок требует поддержания трехзначных темпов роста выручки в сегменте Data Center.

### 3. Риски и неопределённости
- Замедление темпов капитальных затрат (CapEx) гиперскейлеров (Microsoft, Alphabet, Meta, Amazon).
- Ограничения на поставку передовых чипов в Китай и риски цепочки поставок TSMC.
- Усиление собственной разработки кастомных ASIC-процессоров бигтехами.

### 4. Возможные сценарии / альтернативы
- **Базовый:** Консолидация в диапазоне с умеренным ростом по мере отгрузок новой линейки серверов.
- **Коррекционный:** Снижение оценки на 15–20% при малейшем намеке на плато в бюджетах дата-центров.
- **Альтернативы:** Поставщики вспомогательной инфраструктуры — сетевое оборудование, охлаждение и энергетика дата-центров.

### 5. Практические следующие шаги
- Для долгосрочных позиций: зафиксировать часть прибыли на эйфории и подтянуть стоп-лоссы.
- Следить за отчетами ключевых заказчиков по динамике их AI CapEx.
            """.trimIndent()
        }

        if (isPortfolio) {
            return """
### 1. Краткий вывод
Сбалансированный портфель в текущих реалиях должен сочетать защиту от инфляционной волатильности и участие в структурных технологических трендах.

### 2. Детальный анализ
- **Облигационная часть:** Фиксация привлекательных доходностей в качественных инструментах с умеренной дюрацией создаёт устойчивый процентный щит.
- **Долевая часть:** Фокус на компаниях с низким чистым долгом и способностью перекладывать инфляцию на конечного потребителя (pricing power).
- **Сырьевой блок:** Присутствие реальных активов (золото, сырьевые фонды) защищает от рисков девальвации и геополитических шоков.

### 3. Риски и неопределённости
- Риск стагфляции (замедление экономики при сохранении высокой инфляции), бьющий одновременно по акциям роста и длинным облигациям.
- Иллюзия диверсификации при покупке активов с высокой взаимной корреляцией.

### 4. Возможные сценарии распределения
- **Консервативный (60/30/10):** 60% облигации и денежный рынок, 30% дивидендные лидеры, 10% золото.
- **Сбалансированный (50/35/15):** 50% акции широкого рынка, 35% облигации, 15% альтернативные активы (золото/сырьё).

### 5. Практические следующие шаги
- Провести стресс-тест портфеля на случай падения рынков на 15–20%.
- Задать целевые доли по классам активов и ребалансировать только при отклонении более чем на 5 процентных пунктов.
            """.trimIndent()
        }

        // Default strategic consulting response
        return """
### 1. Краткий вывод
С учётом текущей конъюнктуры перевес на стороне взвешенной стратегии с акцентом на фундаментальную устойчивость активов и защиту от инфляционных колебаний.

### 2. Детальный анализ
- **Макрофон:** Рынки адаптируются к новой парадигме стоимости денег, где период «бесплатной ликвидности» завершился. Ключевым критерием успешности компании становится генерация положительного свободного денежного потока (FCF).
- **Отраслевые тренды:** Капитал концентрируется в секторах с высокой операционной эффективностью (автоматизация, AI-инфраструктура, энергетика, критические ресурсы).
- **Оценка:** Требуется критический анализ мультипликаторов P/E, EV/EBITDA и долговой нагрузки (Net Debt / EBITDA < 2.0x).

### 3. Риски и неопределённости
- Волатильность процентных ставок и отложенный эффект жесткой монетарной политики на корпоративный сектор.
- Геополитическая премия в цепочках поставок и сырьевых рынках.
- Снижение потребительского спроса в сегментах необязательных расходов.

### 4. Возможные сценарии / альтернативы
- **Сценарий мягкой адаптации (вероятность ~55%):** Плавное замедление инфляции при сохранении корпоративной маржинальности.
- **Сценарий стагфляционного давления (вероятность ~45%):** Упорная инфляция вынуждает удерживать ставки выше ожиданий, вызывая сжатие мультипликаторов рисковых активов.

### 5. Практические следующие шаги
- Сформулируй конкретный тикер или загрузи график/балансовый отчёт для точечного финансового аудита.
- Оценить долю защитных инструментов и уровень ликвидной подушки в портфеле.
- Избегать принятия решений на эмоциональных пиках новостного шума.
        """.trimIndent()
    }
}
