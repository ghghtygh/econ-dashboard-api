package com.econdashboard.repository

import com.econdashboard.domain.AlertRule
import org.springframework.data.jpa.repository.JpaRepository

interface AlertRuleRepository : JpaRepository<AlertRule, Long> {
    fun findByUserId(userId: String): List<AlertRule>
    fun findByIndicatorIdAndEnabledTrue(indicatorId: Long): List<AlertRule>
}
