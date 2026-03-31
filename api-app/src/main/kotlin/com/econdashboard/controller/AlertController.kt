package com.econdashboard.controller

import com.econdashboard.common.ApiResponse
import com.econdashboard.dto.AlertHistoryResponse
import com.econdashboard.dto.AlertRuleRequest
import com.econdashboard.dto.AlertRuleResponse
import com.econdashboard.dto.AlertRuleUpdateRequest
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
    @Operation(summary = "알림 규칙 목록 조회")
    @GetMapping("/rules")
    fun getAlertRules(): ApiResponse<List<AlertRuleResponse>> {
        return ApiResponse.success(alertService.getAlertRules())
    }

    @Operation(summary = "알림 규칙 생성")
    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    fun createAlertRule(
        @Valid @RequestBody request: AlertRuleRequest
    ): ApiResponse<AlertRuleResponse> {
        return ApiResponse.success(alertService.createAlertRule(request))
    }

    @Operation(summary = "알림 규칙 수정")
    @PutMapping("/rules/{id}")
    fun updateAlertRule(
        @PathVariable id: Long,
        @RequestBody request: AlertRuleUpdateRequest
    ): ApiResponse<AlertRuleResponse> {
        return ApiResponse.success(alertService.updateAlertRule(id, request))
    }

    @Operation(summary = "알림 규칙 삭제")
    @DeleteMapping("/rules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAlertRule(@PathVariable id: Long) {
        alertService.deleteAlertRule(id)
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
