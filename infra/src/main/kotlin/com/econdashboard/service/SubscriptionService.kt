package com.econdashboard.service

import com.econdashboard.domain.Subscription
import com.econdashboard.dto.SubscriptionRequest
import com.econdashboard.dto.SubscriptionResponse
import com.econdashboard.exception.BusinessException
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.IndicatorRepository
import com.econdashboard.repository.SubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val indicatorRepository: IndicatorRepository
) {
    fun getSubscriptions(userId: String): List<SubscriptionResponse> {
        return subscriptionRepository.findByUserId(userId)
            .map { SubscriptionResponse.from(it) }
    }

    @Transactional
    fun subscribe(request: SubscriptionRequest): SubscriptionResponse {
        if (subscriptionRepository.existsByUserIdAndIndicatorId(request.userId, request.indicatorId)) {
            throw BusinessException("ALREADY_SUBSCRIBED", "이미 구독 중인 지표입니다")
        }

        val indicator = indicatorRepository.findById(request.indicatorId)
            .orElseThrow { NotFoundException("Indicator", request.indicatorId) }

        val subscription = Subscription(
            userId = request.userId,
            indicator = indicator
        )
        return SubscriptionResponse.from(subscriptionRepository.save(subscription))
    }

    @Transactional
    fun unsubscribe(id: Long) {
        if (!subscriptionRepository.existsById(id)) {
            throw NotFoundException("Subscription", id)
        }
        subscriptionRepository.deleteById(id)
    }
}
