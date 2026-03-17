package com.econdashboard.repository

import com.econdashboard.domain.NewsArticle
import com.econdashboard.enums.NewsCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface NewsArticleRepository : JpaRepository<NewsArticle, Long> {
    fun findByCategoryOrderByPublishedAtDesc(category: NewsCategory, pageable: Pageable): Page<NewsArticle>
    fun findAllByOrderByPublishedAtDesc(pageable: Pageable): Page<NewsArticle>
    fun existsByUrl(url: String): Boolean
}
