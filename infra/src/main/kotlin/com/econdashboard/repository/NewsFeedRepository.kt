package com.econdashboard.repository

import com.econdashboard.domain.NewsFeed
import org.springframework.data.jpa.repository.JpaRepository

interface NewsFeedRepository : JpaRepository<NewsFeed, Long> {

    fun findByEnabledTrue(): List<NewsFeed>
}
