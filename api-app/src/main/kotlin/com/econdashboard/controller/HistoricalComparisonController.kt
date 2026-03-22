package com.econdashboard.controller

import com.econdashboard.common.ApiResponse
import com.econdashboard.dto.ComparisonSeriesResponse
import com.econdashboard.dto.CrisisPreset
import com.econdashboard.service.HistoricalComparisonService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Historical Comparison", description = "이력 비교 API")
@RestController
@RequestMapping("/api/comparison")
class HistoricalComparisonController(
    private val comparisonService: HistoricalComparisonService
) {

    @Operation(summary = "위기 프리셋 목록 조회", description = "비교 가능한 과거 위기 시점 프리셋 목록")
    @GetMapping("/presets")
    fun getPresets(): ApiResponse<List<CrisisPreset>> {
        return ApiResponse.success(comparisonService.getPresets())
    }

    @Operation(summary = "프리셋 기반 비교", description = "과거 위기 시점 프리셋과 현재 기간 비교")
    @GetMapping("/preset/{presetId}")
    fun compareWithPreset(
        @Parameter(description = "프리셋 ID (gfc-2008, covid-2020, tightening-2022)")
        @PathVariable presetId: String,
        @Parameter(description = "지표 ID")
        @RequestParam indicatorId: Long,
        @Parameter(description = "현재 기간 시작일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) currentStart: LocalDate,
        @Parameter(description = "현재 기간 종료일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) currentEnd: LocalDate
    ): ApiResponse<ComparisonSeriesResponse> {
        return ApiResponse.success(
            comparisonService.compareWithPreset(indicatorId, presetId, currentStart, currentEnd)
        )
    }

    @Operation(summary = "사용자 지정 기간 비교", description = "두 기간의 지표 데이터를 정규화하여 비교")
    @GetMapping("/custom")
    fun compareCustom(
        @Parameter(description = "지표 ID")
        @RequestParam indicatorId: Long,
        @Parameter(description = "현재 기간 시작일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) currentStart: LocalDate,
        @Parameter(description = "현재 기간 종료일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) currentEnd: LocalDate,
        @Parameter(description = "비교 기간 시작일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) historicalStart: LocalDate,
        @Parameter(description = "비교 기간 종료일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) historicalEnd: LocalDate
    ): ApiResponse<ComparisonSeriesResponse> {
        return ApiResponse.success(
            comparisonService.compare(indicatorId, currentStart, currentEnd, historicalStart, historicalEnd)
        )
    }
}
