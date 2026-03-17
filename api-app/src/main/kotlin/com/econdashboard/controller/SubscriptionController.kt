package com.econdashboard.controller

import com.econdashboard.common.ApiResponse
import com.econdashboard.dto.SubscriptionRequest
import com.econdashboard.dto.SubscriptionResponse
import com.econdashboard.service.SubscriptionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Subscriptions", description = "지표 구독 API")
@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {
    @Operation(summary = "구독 목록 조회")
    @GetMapping
    fun getSubscriptions(
        @Parameter(description = "사용자 ID") @RequestParam userId: String
    ): ApiResponse<List<SubscriptionResponse>> {
        return ApiResponse.success(subscriptionService.getSubscriptions(userId))
    }

    @Operation(summary = "지표 구독")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun subscribe(
        @Valid @RequestBody request: SubscriptionRequest
    ): ApiResponse<SubscriptionResponse> {
        return ApiResponse.success(subscriptionService.subscribe(request))
    }

    @Operation(summary = "구독 해제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unsubscribe(
        @Parameter(description = "구독 ID") @PathVariable id: Long
    ) {
        subscriptionService.unsubscribe(id)
    }
}
