package com.econdashboard.domain

import com.econdashboard.enums.NewsCategory
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "news_articles",
    indexes = [
        Index(name = "idx_news_articles_category", columnList = "category"),
        Index(name = "idx_news_articles_published_at", columnList = "published_at")
    ]
)
class NewsArticle(

    @Column(nullable = false, length = 500)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var summary: String? = null,

    @Column(nullable = false, length = 1000, unique = true)
    var url: String,

    @Column(length = 500)
    var source: String? = null,

    @Column(length = 500)
    var author: String? = null,

    @Column(length = 1000)
    var imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: NewsCategory,

    @Column(name = "published_at", nullable = false)
    var publishedAt: LocalDateTime,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
