package com.econdashboard.repository

import com.econdashboard.domain.AlertHistory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AlertHistoryRepository : JpaRepository<AlertHistory, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: String, pageable: Pageable): Page<AlertHistory>
}
