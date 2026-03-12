package com.econdashboard.repository

import com.econdashboard.domain.Indicator
import com.econdashboard.enums.IndicatorCategory
import org.springframework.data.jpa.repository.JpaRepository

interface IndicatorRepository : JpaRepository<Indicator, Long> {

    fun findByCategory(category: IndicatorCategory): List<Indicator>

    fun findBySymbol(symbol: String): Indicator?

    fun findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(
        name: String,
        symbol: String
    ): List<Indicator>
}
