package com.econdashboard.service

import com.econdashboard.domain.NewsArticle
import com.econdashboard.enums.NewsCategory
import com.econdashboard.repository.NewsArticleRepository
import com.rometools.rome.io.SyndFeedInput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.xml.sax.InputSource
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class NewsCollectionService(
    private val newsArticleRepository: NewsArticleRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        val RSS_FEEDS = mapOf(
            "https://feeds.finance.yahoo.com/rss/2.0/headline?s=^GSPC&region=US&lang=en-US" to NewsCategory.STOCK,
            "https://feeds.finance.yahoo.com/rss/2.0/headline?s=^DJI&region=US&lang=en-US" to NewsCategory.STOCK,
            "https://feeds.finance.yahoo.com/rss/2.0/headline?s=EURUSD=X&region=US&lang=en-US" to NewsCategory.FOREX,
            "https://feeds.finance.yahoo.com/rss/2.0/headline?s=BTC-USD&region=US&lang=en-US" to NewsCategory.CRYPTO,
            "https://feeds.finance.yahoo.com/rss/2.0/headline?s=GC=F&region=US&lang=en-US" to NewsCategory.COMMODITY,
            "https://feeds.finance.yahoo.com/rss/2.0/headline?s=^TNX&region=US&lang=en-US" to NewsCategory.BOND
        )
    }

    @Transactional
    fun collectNews(): Int {
        var totalCollected = 0

        RSS_FEEDS.forEach { (feedUrl, category) ->
            try {
                val count = collectFromFeed(feedUrl, category)
                totalCollected += count
                log.info("Collected {} news from feed: {}", count, feedUrl)
            } catch (e: Exception) {
                log.error("Failed to collect news from feed {}: {}", feedUrl, e.message)
            }
        }

        log.info("Total news collected: {}", totalCollected)
        return totalCollected
    }

    private fun collectFromFeed(feedUrl: String, category: NewsCategory): Int {
        val input = SyndFeedInput()
        @Suppress("DEPRECATION")
        val feed = input.build(InputSource(URL(feedUrl).openStream()))
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
