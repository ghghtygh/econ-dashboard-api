package com.econdashboard.repository

import com.econdashboard.domain.Subscription
import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findByUserId(userId: String): List<Subscription>
    fun findByUserIdAndIndicatorId(userId: String, indicatorId: Long): Subscription?
    fun existsByUserIdAndIndicatorId(userId: String, indicatorId: Long): Boolean
}
