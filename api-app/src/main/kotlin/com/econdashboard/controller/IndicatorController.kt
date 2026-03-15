package com.econdashboard.controller

import com.econdashboard.common.ApiResponse
import com.econdashboard.dto.IndicatorDataResponse
import com.econdashboard.dto.IndicatorResponse
import com.econdashboard.dto.IndicatorSeriesRequest
import com.econdashboard.dto.IndicatorSeriesResponse
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.service.IndicatorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Indicators", description = "경제 지표 API")
@RestController
@RequestMapping("/api/indicators")
class IndicatorController(
    private val indicatorService: IndicatorService
) {

    @Operation(summary = "전체 지표 목록 조회", description = "카테고리 필터를 통한 지표 목록 조회")
    @GetMapping
    fun getIndicators(
        @Parameter(description = "카테고리 필터")
        @RequestParam(required = false) category: IndicatorCategory?
    ): ApiResponse<List<IndicatorResponse>> {
        return ApiResponse.success(indicatorService.getAllIndicators(category))
    }

    @Operation(summary = "지표 상세 조회")
    @GetMapping("/{id}")
    fun getIndicator(
        @Parameter(description = "지표 ID") @PathVariable id: Long
    ): ApiResponse<IndicatorResponse> {
        return ApiResponse.success(indicatorService.getIndicatorById(id))
    }

    @Operation(summary = "카테고리 목록 조회")
    @GetMapping("/categories")
    fun getCategories(): ApiResponse<List<IndicatorCategory>> {
        return ApiResponse.success(indicatorService.getCategories())
    }

    @Operation(summary = "시계열 데이터 조회", description = "지정 기간의 시계열 데이터를 페이징 처리하여 조회")
    @GetMapping("/{id}/data")
    fun getIndicatorData(
        @Parameter(description = "지표 ID") @PathVariable id: Long,
        @Parameter(description = "시작 날짜 (yyyy-MM-dd)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @Parameter(description = "종료 날짜 (yyyy-MM-dd)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
        @PageableDefault(size = 100, sort = ["date"]) pageable: Pageable
    ): ApiResponse<Page<IndicatorDataResponse>> {
        return ApiResponse.success(indicatorService.getIndicatorData(id, from, to, pageable))
    }

    @Operation(summary = "복수 지표 시계열 일괄 조회")
    @PostMapping("/series")
    fun getMultipleSeries(
        @Valid @RequestBody request: IndicatorSeriesRequest
    ): ApiResponse<List<IndicatorSeriesResponse>> {
        return ApiResponse.success(indicatorService.getMultipleIndicatorSeries(request))
    }
}
