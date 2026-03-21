package com.econdashboard.service

import com.econdashboard.domain.NewsArticle
import com.econdashboard.enums.NewsCategory
import com.econdashboard.repository.NewsFeedRepository
import com.econdashboard.repository.NewsArticleRepository
import com.rometools.rome.io.SyndFeedInput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.xml.sax.InputSource
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class NewsCollectionService(
    private val newsArticleRepository: NewsArticleRepository,
    private val newsFeedRepository: NewsFeedRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun collectNews(): Int {
        val feeds = newsFeedRepository.findByEnabledTrue()

        if (feeds.isEmpty()) {
            log.warn("No enabled news feeds found")
            return 0
        }

        var totalCollected = 0

        feeds.forEach { feed ->
            try {
                val count = collectFromFeed(feed.url, feed.category)
                totalCollected += count
                log.info("Collected {} news from feed: {} ({})", count, feed.name, feed.category)
            } catch (e: Exception) {
                log.error("Failed to collect news from feed {} ({}): {}", feed.name, feed.url, e.message)
            }
        }

        log.info("Total news collected: {} from {} feeds", totalCollected, feeds.size)
        return totalCollected
    }

    private fun collectFromFeed(feedUrl: String, category: NewsCategory): Int {
        val input = SyndFeedInput()
        val feed = input.build(InputSource(URI(feedUrl).toURL().openStream()))
        var count = 0

        feed.entries.forEach { entry ->
            val articleUrl = entry.link ?: return@forEach

            if (newsArticleRepository.existsByUrl(articleUrl)) {
                return@forEach
            }

            val publishedAt = entry.publishedDate?.toInstant()
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalDateTime()
                ?: LocalDateTime.now()

            val article = NewsArticle(
                title = entry.title ?: "Untitled",
                summary = entry.description?.value?.take(1000),
                url = articleUrl,
                source = feed.title ?: "Unknown",
                author = entry.author,
                category = category,
                publishedAt = publishedAt
            )

            newsArticleRepository.save(article)
            count++
        }

        return count
    }
}
