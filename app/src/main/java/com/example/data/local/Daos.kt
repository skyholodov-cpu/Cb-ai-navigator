package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmark(id: Long, isBookmarked: Boolean)

    @Query("SELECT * FROM chat_messages WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedMessages(): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface MarketAssetDao {
    @Query("SELECT * FROM market_assets ORDER BY category ASC, ticker ASC")
    fun getAllAssetsFlow(): Flow<List<MarketAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(asset: MarketAssetEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(assets: List<MarketAssetEntity>)

    @Query("DELETE FROM market_assets WHERE ticker = :ticker")
    suspend fun delete(ticker: String)

    @Query("SELECT COUNT(*) FROM market_assets")
    suspend fun getCount(): Int
}

@Dao
interface SavedReportDao {
    @Query("SELECT * FROM saved_reports ORDER BY timestamp DESC")
    fun getAllReportsFlow(): Flow<List<SavedReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SavedReportEntity): Long

    @Query("DELETE FROM saved_reports WHERE id = :id")
    suspend fun deleteReport(id: Long)
}

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM personalized_portfolios ORDER BY createdAt DESC LIMIT 1")
    fun getLatestPortfolioFlow(): Flow<PersonalizedPortfolioEntity?>

    @Query("SELECT * FROM personalized_portfolios ORDER BY createdAt DESC")
    fun getAllPortfoliosFlow(): Flow<List<PersonalizedPortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: PersonalizedPortfolioEntity): Long

    @Query("DELETE FROM personalized_portfolios WHERE id = :id")
    suspend fun deletePortfolio(id: Long)

    @Query("DELETE FROM personalized_portfolios")
    suspend fun clearAllPortfolios()
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM financial_news ORDER BY timestamp DESC")
    fun getAllNewsFlow(): Flow<List<FinancialNewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: FinancialNewsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllNews(news: List<FinancialNewsEntity>)

    @Query("DELETE FROM financial_news WHERE id = :id")
    suspend fun deleteNews(id: String)
}
