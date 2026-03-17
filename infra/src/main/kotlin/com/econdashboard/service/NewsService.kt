package com.econdashboard.service

import com.econdashboard.dto.NewsArticleResponse
import com.econdashboard.enums.NewsCategory
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.NewsArticleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NewsService(
    private val newsArticleRepository: NewsArticleRepository
) {

    fun getNewsList(category: NewsCategory?, pageable: Pageable): Page<NewsArticleResponse> {
        val articles = if (category != null) {
            newsArticleRepository.findByCategoryOrderByPublishedAtDesc(category, pageable)
        } else {
            newsArticleRepository.findAllByOrderByPublishedAtDesc(pageable)
        }
        return articles.map { NewsArticleResponse.from(it) }
    }

    fun getNewsById(id: Long): NewsArticleResponse {
        val article = newsArticleRepository.findById(id)
            .orElseThrow { NotFoundException("NewsArticle", id) }
        return NewsArticleResponse.from(article)
    }
}
