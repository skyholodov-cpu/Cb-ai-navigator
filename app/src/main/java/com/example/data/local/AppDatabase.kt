package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChatMessageEntity::class,
        MarketAssetEntity::class,
        SavedReportEntity::class,
        PersonalizedPortfolioEntity::class,
        FinancialNewsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun marketAssetDao(): MarketAssetDao
    abstract fun savedReportDao(): SavedReportDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_navigator_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val initialMessage = ChatMessageEntity(
                    sender = "NAVIGATOR",
                    text = """
Привет. Я — **AI Navigator**, твой цифровой финансовый интеллект и стратегический советник нового поколения. 

Моя задача — помогать тебе разбирать рыночные тренды, оценивать бизнес-модели компаний, анализировать макроэкономику и формировать персонализированный инвестиционный портфель под твои цели.

### 1. Краткий вывод
Ты находишься в едином аналитическом пространстве: здесь можно разобрать любой финансовый актив, сформировать персональный портфель или отследить влияние свежих новостей на рынки.

### 2. Детальный анализ
Рынки сейчас находятся в фазе переоценки процентных ставок и глобального технологического цикла (AI-инфраструктура, энергетика, сырьевые рынки). В такие периоды дисциплина и фундамент имеют критическое преимущество перед импульсивными сделками.

### 3. Риски и неопределённости
- Волатильность процентных ставок и инфляционные сюрпризы
- Геополитическая премия в сырьевых товарах и логистике
- Риск переоценки отдельных технологических компаний с высокими мультипликаторами

### 4. Возможные сценарии
- **Базовый:** Умеренный рост качественных компаний с устойчивым денежным потоком и дивидендной доходностью.
- **Альтернативный:** Затяжное плато процентных ставок с локальными коррекциями в рисковых активах.

### 5. Практические следующие шаги
- Нажми кнопку **«Сформировать портфель»** вверху, чтобы пройти 30-секундный аудит целей, горизонта и толерантности к риску.
- Изучи раздел **«Новости AI»**, где я анализирую настроения и влияние событий на конкретные активы.
- Прикрепи скриншот графика или финансовой отчётности через значок скрепки для визуального разбора.
                    """.trimIndent(),
                    timestamp = System.currentTimeMillis()
                )
                database.chatDao().insertMessage(initialMessage)

                val defaultAssets = listOf(
                    MarketAssetEntity(
                        ticker = "S&P 500",
                        name = "Индекс широкого рынка США",
                        category = "Индексы",
                        priceFormatted = "5,864.67",
                        changePercent = 0.42,
                        notes = "Опора на технологический сектор и корпоративную прибыль",
                        aiThesisSummary = "На текущий момент перевес на стороне умеренного продолжения тренда при контроле инфляционных рисков."
                    ),
                    MarketAssetEntity(
                        ticker = "GOLD (XAU/USD)",
                        name = "Золото спот",
                        category = "Сырьё",
                        priceFormatted = "$2,652.80",
                        changePercent = 0.85,
                        notes = "Защитный актив против геополитики и девальвации фиата",
                        aiThesisSummary = "Стратегический хедж. Спрос мировых ЦБ сохраняет сильную фундаментальную поддержку."
                    ),
                    MarketAssetEntity(
                        ticker = "BTC/USD",
                        name = "Биткоин",
                        category = "Криптовалюты",
                        priceFormatted = "$64,280",
                        changePercent = 1.34,
                        notes = "Цифровой дефицитный актив, институциональные притоки через ETF",
                        aiThesisSummary = "Высокая волатильность. Более вероятен сценарий среднесрочной консолидации с фазами сильных импульсов."
                    ),
                    MarketAssetEntity(
                        ticker = "BRENT",
                        name = "Нефть Brent",
                        category = "Сырьё",
                        priceFormatted = "$78.45",
                        changePercent = -0.52,
                        notes = "Влияние квот ОПЕК+ и спроса в Китае",
                        aiThesisSummary = "Баланс спроса и предложения чувствителен к геополитической премии на Ближнем Востоке."
                    ),
                    MarketAssetEntity(
                        ticker = "NVDA",
                        name = "NVIDIA Corp.",
                        category = "Акции",
                        priceFormatted = "$124.50",
                        changePercent = 2.15,
                        notes = "Лидер полупроводниковой инфраструктуры для генеративного AI",
                        aiThesisSummary = "Сильные операционные показатели, но высокие требования рынка к продолжению экспоненциального роста выручки."
                    ),
                    MarketAssetEntity(
                        ticker = "US 10Y",
                        name = "Казначейские облигации США 10 лет",
                        category = "Облигации",
                        priceFormatted = "4.08%",
                        changePercent = -0.03,
                        notes = "Базовая безрисковая ставка глобальной финансовой системы",
                        aiThesisSummary = "Динамика отражает ожидания темпов снижения ставки ФРС в ближайшие кварталы."
                    )
                )
                database.marketAssetDao().insertAll(defaultAssets)

                // Prepopulate initial institutional financial news with AI Radar analysis
                val initialNews = listOf(
                    FinancialNewsEntity(
                        id = "news_1",
                        title = "ФРС сохраняет базовую ставку, сигнализируя о зависимости решений от данных по инфляции",
                        source = "Bloomberg Macro",
                        timeAgo = "1 час назад",
                        category = "Макро",
                        sentiment = "NEUTRAL",
                        keyTheme = "Монетарная политика и доходности трежерис",
                        affectedAssets = "↔ US 10Y | ↑ Золото | ↓ Акции с высоким долгом",
                        aiSummary = "Регулятор избегает поспешного смягчения, удерживая реальные ставки на ограничительном уровне. Рынки переоценивают траекторию снижения ставок на второе полугодие.",
                        strategicImplication = "Фиксация привлекательной купонной доходности в облигациях средней дюрации (3-5 лет) оправдана. В акциях предпочтение компаниям с отрицательным чистым долгом."
                    ),
                    FinancialNewsEntity(
                        id = "news_2",
                        title = "Гиперскейлеры увеличивают AI CapEx: совокупные затраты на дата-центры превысят $200 млрд",
                        source = "Financial Times Tech",
                        timeAgo = "3 часа назад",
                        category = "Технологии",
                        sentiment = "BULLISH",
                        keyTheme = "AI-инфраструктура и полупроводниковый суперцикл",
                        affectedAssets = "↑ NVDA, TSM, AVGO | ↑ Энергетические утилиты | ↔ S&P 500",
                        aiSummary = "Инвестиционный цикл в серверную инфраструктуру не снижает обороты. Бенефициарами выступают не только производители чипов, но и поставщики охлаждения и электроэнергии.",
                        strategicImplication = "Позитивный фон для сектора полупроводников, но мультипликаторы уязвимы к временным задержкам монетизации софта конечными клиентами."
                    ),
                    FinancialNewsEntity(
                        id = "news_3",
                        title = "Центральные банки развивающихся стран увеличили закупки золота на 18% в текущем квартале",
                        source = "World Gold Council",
                        timeAgo = "5 часов назад",
                        category = "Сырьё",
                        sentiment = "BULLISH",
                        keyTheme = "Суверенная дедолларизация и защита резервов",
                        affectedAssets = "↑ Золото (XAU/USD) | ↑ Золотодобытчики | ↔ Индекс DXY",
                        aiSummary = "Геополитическая фрагментация и риски санкционной блокировки фиатных резервов создают неэластичный структурный спрос на физический металл.",
                        strategicImplication = "Подтверждает целесообразность удержания 10-15% портфеля в золоте в качестве стратегического безрискового якоря."
                    ),
                    FinancialNewsEntity(
                        id = "news_4",
                        title = "ОПЕК+ обсуждает перенос сроков постепенного восстановления добычи нефти из-за спроса в Азии",
                        source = "Reuters Energy",
                        timeAgo = "7 часов назад",
                        category = "Сырьё",
                        sentiment = "BEARISH",
                        keyTheme = "Баланс спроса и предложения на рынке углеводородов",
                        affectedAssets = "↓ Brent Crude | ↓ Нефтегазовый сектор | ↑ Авиаперевозчики",
                        aiSummary = "Замедление промышленного спроса в Китае сдерживает цены на нефть, вынуждая картель продлевать добровольные ограничения.",
                        strategicImplication = "Рекомендуется осторожность в акциях сырьевых цикликов с высокой себестоимостью добычи. Предпочтение компаниям с высокой дивидендной доходностью."
                    ),
                    FinancialNewsEntity(
                        id = "news_5",
                        title = "Институциональный чистый приток в спотовые Bitcoin ETF превысил $1.2 млрд за неделю",
                        source = "CoinDesk Markets",
                        timeAgo = "9 часов назад",
                        category = "Крипто",
                        sentiment = "BULLISH",
                        keyTheme = "Институционализация цифровых дефицитных активов",
                        affectedAssets = "↑ BTC/USD | ↑ Криптобиржи | ↔ Технологический сектор",
                        aiSummary = "Включение биткоина в портфели wealth-менеджеров и пенсионных фондов вымывает биржевые остатки, формируя устойчивую ценовую поддержку.",
                        strategicImplication = "Для умеренно-агрессивных инвесторов аллокация в 3-5% способна повысить коэффициент Шарпа портфеля при жестком контроле волатильности."
                    )
                )
                database.newsDao().insertAllNews(initialNews)

                // Prepopulate a sample balanced portfolio
                val initialPortfolio = PersonalizedPortfolioEntity(
                    title = "Сбалансированный рост & Защита капитала",
                    financialGoal = "Умеренный рост с защитой от инфляции",
                    investmentHorizon = "3–5 лет",
                    riskTolerance = "Умеренная (просадка до 15%)",
                    targetAllocation = "Акции: 45%, Облигации: 30%, Золото: 15%, Денежный рынок/Кэш: 10%",
                    equityShare = 45,
                    fixedIncomeShare = 30,
                    goldCommoditiesShare = 15,
                    alternativesShare = 0,
                    cashShare = 10,
                    expectedAnnualReturn = "11–14% годовых",
                    maxEstimatedDrawdown = "до 12–14% в кризисные фазы",
                    strategicRationale = "Классический баланс роста широкого рынка и процентной подушки доходности с золотым суверенным хеджем.",
                    keyRiskFactors = "Затяжное удержание высоких ставок ФРС/ЦБ, геополитические шоки в цепочках поставок.",
                    recommendedActions = "Ежеквартальная ребалансировка при отклонении долей более чем на 5 п.п.; постепенный набор позиций через усреднение (DCA)."
                )
                database.portfolioDao().insertPortfolio(initialPortfolio)
            }
        }
    }
}
