package com.econdashboard.collector

import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.service.DataCollectionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MacroCollector(
    private val dataCollectionService: DataCollectionService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * FRED 소스의 거시경제 지표를 수집합니다.
     * CPI, 실업률, PCE, ISM PMI, 미시간 소비자심리지수 등
     * 월간/주간 데이터이므로 하루 1회 수집으로 충분합니다.
     */
    fun collect() {
        log.info("Starting FRED macro data collection")
        dataCollectionService.collectBySourceAndCategories(
            DataSource.FRED,
            listOf(IndicatorCategory.MACRO)
        )
    }
}
