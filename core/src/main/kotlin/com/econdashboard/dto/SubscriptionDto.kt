package com.econdashboard.dto

import com.econdashboard.domain.Subscription
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class SubscriptionRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다")
    val userId: String,

    @field:NotNull(message = "지표 ID는 필수입니다")
    val indicatorId: Long
)

data class SubscriptionResponse(
    val id: Long,
    val userId: String,
    val indicatorId: Long,
    val indicatorName: String,
    val indicatorSymbol: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(subscription: Subscription) = SubscriptionResponse(
            id = subscription.id,
            userId = subscription.userId,
            indicatorId = subscription.indicator.id,
            indicatorName = subscription.indicator.name,
            indicatorSymbol = subscription.indicator.symbol,
            createdAt = subscription.createdAt
        )
    }
}
