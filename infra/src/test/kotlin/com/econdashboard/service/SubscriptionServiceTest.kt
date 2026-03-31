package com.econdashboard.service

import com.econdashboard.domain.Indicator
import com.econdashboard.domain.Subscription
import com.econdashboard.dto.SubscriptionRequest
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.exception.BusinessException
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.IndicatorRepository
import com.econdashboard.repository.SubscriptionRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals

class SubscriptionServiceTest {

    private val subscriptionRepository: SubscriptionRepository = mockk()
    private val indicatorRepository: IndicatorRepository = mockk()

    private lateinit var subscriptionService: SubscriptionService
    private lateinit var indicator: Indicator

    @BeforeEach
    fun setUp() {
        subscriptionService = SubscriptionService(subscriptionRepository, indicatorRepository)
        indicator = Indicator(
            name = "S&P 500",
            symbol = "SPX",
            category = IndicatorCategory.STOCK,
            unit = "포인트",
            source = DataSource.YAHOO
        ).apply {
            val idField = Indicator::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
    }

    @Test
    fun `getSubscriptions - 사용자 구독 목록 조회`() {
        val subscription = Subscription(
            userId = "user1",
            indicator = indicator
        ).apply {
            val idField = Subscription::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }

        every { subscriptionRepository.findByUserId("user1") } returns listOf(subscription)

        val result = subscriptionService.getSubscriptions("user1")

        assertEquals(1, result.size)
        assertEquals("user1", result[0].userId)
        assertEquals("SPX", result[0].indicatorSymbol)
    }

    @Test
    fun `subscribe - 정상 구독`() {
        val request = SubscriptionRequest(userId = "user1", indicatorId = 1L)

        every { subscriptionRepository.existsByUserIdAndIndicatorId("user1", 1L) } returns false
        every { indicatorRepository.findById(1L) } returns Optional.of(indicator)
        every { subscriptionRepository.save(any()) } answers {
            (firstArg() as Subscription).apply {
                val idField = Subscription::class.java.getDeclaredField("id")
                idField.isAccessible = true
                idField.set(this, 1L)
            }
        }

        val result = subscriptionService.subscribe(request)

        assertEquals("user1", result.userId)
        assertEquals(1L, result.indicatorId)
    }

    @Test
    fun `subscribe - 이미 구독 중인 경우`() {
        val request = SubscriptionRequest(userId = "user1", indicatorId = 1L)

        every { subscriptionRepository.existsByUserIdAndIndicatorId("user1", 1L) } returns true

        val ex = assertThrows<BusinessException> {
            subscriptionService.subscribe(request)
        }
        assertEquals("ALREADY_SUBSCRIBED", ex.errorCode)
    }

    @Test
    fun `subscribe - 존재하지 않는 지표`() {
        val request = SubscriptionRequest(userId = "user1", indicatorId = 999L)

        every { subscriptionRepository.existsByUserIdAndIndicatorId("user1", 999L) } returns false
        every { indicatorRepository.findById(999L) } returns Optional.empty()

        assertThrows<NotFoundException> {
            subscriptionService.subscribe(request)
        }
    }

    @Test
    fun `unsubscribe - 정상 삭제`() {
        every { subscriptionRepository.existsById(1L) } returns true
        every { subscriptionRepository.deleteById(1L) } just runs

        subscriptionService.unsubscribe(1L)

        verify { subscriptionRepository.deleteById(1L) }
    }

    @Test
    fun `unsubscribe - 존재하지 않는 구독`() {
        every { subscriptionRepository.existsById(999L) } returns false

        assertThrows<NotFoundException> {
            subscriptionService.unsubscribe(999L)
        }
    }
}
