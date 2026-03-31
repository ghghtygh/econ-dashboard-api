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
     * FRED 소스의 거시경제·채권 지표를 수집합니다.
     * MACRO: CPI, 실업률, PCE, ISM PMI, 미시간 소비자심리지수 등
     * BOND: 장단기 금리차(T10Y2Y), 한국 국채 등
     * 월간/주간/일간 데이터이므로 하루 수회 수집으로 충분합니다.
     */
    fun collect(): Int {
        log.info("Starting FRED data collection (MACRO + BOND)")
        return dataCollectionService.collectBySourceAndCategories(
            DataSource.FRED,
            listOf(IndicatorCategory.MACRO, IndicatorCategory.BOND)
        )
    }
}
