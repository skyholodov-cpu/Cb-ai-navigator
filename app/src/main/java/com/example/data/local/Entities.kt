package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String = "default_session",
    val sender: String, // "USER" or "NAVIGATOR"
    val text: String,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)

@Entity(tableName = "market_assets")
data class MarketAssetEntity(
    @PrimaryKey
    val ticker: String,
    val name: String,
    val category: String, // "Акции", "Индексы", "Криптовалюты", "Сырьё", "Облигации"
    val priceFormatted: String,
    val changePercent: Double,
    val notes: String = "",
    val aiThesisSummary: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_reports")
data class SavedReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val topic: String,
    val summary: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "personalized_portfolios")
data class PersonalizedPortfolioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val financialGoal: String,
    val investmentHorizon: String,
    val riskTolerance: String,
    val targetAllocation: String,
    val equityShare: Int,
    val fixedIncomeShare: Int,
    val goldCommoditiesShare: Int,
    val alternativesShare: Int,
    val cashShare: Int,
    val expectedAnnualReturn: String,
    val maxEstimatedDrawdown: String,
    val strategicRationale: String,
    val keyRiskFactors: String,
    val recommendedActions: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "financial_news")
data class FinancialNewsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val source: String,
    val timeAgo: String,
    val category: String, // "Макро", "Технологии", "Сырьё", "Крипто", "Облигации"
    val sentiment: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val keyTheme: String,
    val affectedAssets: String,
    val aiSummary: String,
    val strategicImplication: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isCustom: Boolean = false
)
