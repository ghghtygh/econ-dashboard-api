package com.econdashboard.repository

import com.econdashboard.domain.DashboardWidget
import org.springframework.data.jpa.repository.JpaRepository

interface DashboardWidgetRepository : JpaRepository<DashboardWidget, Long> {

    fun findAllByOrderByPositionYAscPositionXAsc(): List<DashboardWidget>
}
