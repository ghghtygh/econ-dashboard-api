package com.econdashboard.controller

import com.econdashboard.service.DataCollectionService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/collect/historical")
class HistoricalCollectionController(
    private val dataCollectionService: DataCollectionService
) {

    /**
     * 전체 지표의 과거 데이터 수집
     * POST /api/collect/historical?from=2024-01-01&to=2024-12-31
     */
    @PostMapping
    fun collectAll(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
    ): ResponseEntity<Map<String, Any>> {
        val count = dataCollectionService.collectAllHistorical(from, to)
        return ResponseEntity.ok(mapOf(
            "message" to "Historical data collection completed",
            "from" to from.toString(),
            "to" to to.toString(),
            "totalDataPoints" to count
        ))
    }

    /**
     * 특정 심볼의 과거 데이터 수집
     * POST /api/collect/historical/symbols?symbols=bitcoin,^GSPC&from=2024-01-01&to=2024-12-31
     */
    @PostMapping("/symbols")
    fun collectBySymbols(
        @RequestParam symbols: List<String>,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
    ): ResponseEntity<Map<String, Any>> {
        val count = dataCollectionService.collectHistoricalBySymbols(symbols, from, to)
        return ResponseEntity.ok(mapOf(
            "message" to "Historical data collection completed",
            "symbols" to symbols,
            "from" to from.toString(),
            "to" to to.toString(),
            "totalDataPoints" to count
        ))
    }
}
