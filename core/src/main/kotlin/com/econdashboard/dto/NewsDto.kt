package com.econdashboard.dto

import com.econdashboard.domain.NewsArticle
import com.econdashboard.enums.NewsCategory
import java.time.LocalDateTime

data class NewsArticleResponse(
    val id: Long,
    val title: String,
    val summary: String?,
    val url: String,
    val source: String?,
    val author: String?,
    val imageUrl: String?,
    val category: NewsCategory,
    val publishedAt: LocalDateTime,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(article: NewsArticle) = NewsArticleResponse(
            id = article.id,
            title = article.title,
            summary = article.summary,
            url = article.url,
            source = article.source,
            author = article.author,
            imageUrl = article.imageUrl,
            category = article.category,
            publishedAt = article.publishedAt,
            createdAt = article.createdAt
        )
    }
}
