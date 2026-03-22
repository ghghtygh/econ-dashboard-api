package com.econdashboard.controller

import com.econdashboard.common.ApiResponse
import com.econdashboard.dto.CorrelationMatrixResponse
import com.econdashboard.dto.CorrelationPairResponse
import com.econdashboard.service.CorrelationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Correlation", description = "지표 간 상관관계 API")
@RestController
@RequestMapping("/api/correlation")
class CorrelationController(
    private val correlationService: CorrelationService
) {

    @Operation(summary = "지표 쌍별 상관계수 조회", description = "선택한 지표들 간 피어슨 상관계수 (일간 수익률 기반)")
    @GetMapping("/pairs")
    fun getCorrelationPairs(
        @Parameter(description = "지표 ID 목록 (쉼표 구분)")
        @RequestParam indicatorIds: List<Long>,
        @Parameter(description = "시작일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "종료일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ApiResponse<List<CorrelationPairResponse>> {
        return ApiResponse.success(correlationService.getCorrelationPairs(indicatorIds, startDate, endDate))
    }

    @Operation(summary = "상관관계 매트릭스 조회", description = "선택한 지표들의 전체 상관관계 매트릭스")
    @GetMapping("/matrix")
    fun getCorrelationMatrix(
        @Parameter(description = "지표 ID 목록 (쉼표 구분)")
        @RequestParam indicatorIds: List<Long>,
        @Parameter(description = "시작일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "종료일")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ApiResponse<CorrelationMatrixResponse> {
        return ApiResponse.success(correlationService.getCorrelationMatrix(indicatorIds, startDate, endDate))
    }
}
