package com.econdashboard.controller

import com.econdashboard.common.ApiResponse
import com.econdashboard.dto.AlertHistoryResponse
import com.econdashboard.dto.AlertRuleRequest
import com.econdashboard.dto.AlertRuleResponse
import com.econdashboard.service.AlertService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Alerts", description = "알림 API")
@RestController
@RequestMapping("/api/alerts")
class AlertController(
    private val alertService: AlertService
) {
    @Operation(summary = "알림 규칙 생성")
    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    fun createAlertRule(
        @Valid @RequestBody request: AlertRuleRequest
    ): ApiResponse<AlertRuleResponse> {
        return ApiResponse.success(alertService.createAlertRule(request))
    }

    @Operation(summary = "알림 이력 조회")
    @GetMapping
    fun getAlertHistory(
        @Parameter(description = "사용자 ID") @RequestParam userId: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<AlertHistoryResponse>> {
        return ApiResponse.success(alertService.getAlertHistory(userId, pageable))
    }
}
