package com.econdashboard.controller

import com.econdashboard.common.ApiResponse
import com.econdashboard.dto.DashboardWidgetRequest
import com.econdashboard.dto.DashboardWidgetResponse
import com.econdashboard.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Dashboard", description = "대시보드 위젯 API")
@RestController
@RequestMapping("/api/dashboard/widgets")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @Operation(summary = "위젯 목록 조회")
    @GetMapping
    fun getWidgets(): ApiResponse<List<DashboardWidgetResponse>> {
        return ApiResponse.success(dashboardService.getAllWidgets())
    }

    @Operation(summary = "위젯 생성/일괄 저장", description = "위젯 목록을 일괄 저장합니다 (기존 위젯을 모두 교체)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun saveWidgets(
        @Valid @RequestBody requests: List<@Valid DashboardWidgetRequest>
    ): ApiResponse<List<DashboardWidgetResponse>> {
        return ApiResponse.success(dashboardService.saveWidgets(requests))
    }

    @Operation(summary = "위젯 수정")
    @PutMapping("/{id}")
    fun updateWidget(
        @Parameter(description = "위젯 ID") @PathVariable id: Long,
        @Valid @RequestBody request: DashboardWidgetRequest
    ): ApiResponse<DashboardWidgetResponse> {
        return ApiResponse.success(dashboardService.updateWidget(id, request))
    }

    @Operation(summary = "위젯 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteWidget(
        @Parameter(description = "위젯 ID") @PathVariable id: Long
    ) {
        dashboardService.deleteWidget(id)
    }
}
