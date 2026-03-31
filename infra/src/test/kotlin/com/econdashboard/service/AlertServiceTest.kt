package com.econdashboard.service

import com.econdashboard.domain.AlertHistory
import com.econdashboard.domain.AlertRule
import com.econdashboard.domain.Indicator
import com.econdashboard.domain.IndicatorData
import com.econdashboard.dto.AlertRuleRequest
import com.econdashboard.enums.AlertConditionType
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.AlertHistoryRepository
import com.econdashboard.repository.AlertRuleRepository
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AlertServiceTest {

    private val alertRuleRepository: AlertRuleRepository = mockk()
    private val alertHistoryRepository: AlertHistoryRepository = mockk()
    private val indicatorRepository: IndicatorRepository = mockk()
    private val indicatorDataRepository: IndicatorDataRepository = mockk()

    private lateinit var alertService: AlertService
    private lateinit var indicator: Indicator

    @BeforeEach
    fun setUp() {
        alertService = AlertService(alertRuleRepository, alertHistoryRepository, indicatorRepository, indicatorDataRepository)
        indicator = Indicator(
            name = "비트코인",
            symbol = "BTC-USD",
            category = IndicatorCategory.CRYPTO,
            unit = "USD",
            source = DataSource.COINGECKO
        ).apply {
            val idField = Indicator::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
    }

    @Test
    fun `createAlertRule - 정상 생성`() {
        val request = AlertRuleRequest(
            userId = "user1",
            indicatorId = 1L,
            conditionType = AlertConditionType.ABOVE,
            threshold = BigDecimal("70000")
        )
        every { indicatorRepository.findById(1L) } returns Optional.of(indicator)
        every { alertRuleRepository.save(any()) } answers {
            (firstArg() as AlertRule).apply {
                val idField = AlertRule::class.java.getDeclaredField("id")
                idField.isAccessible = true
                idField.set(this, 1L)
            }
        }

        val result = alertService.createAlertRule(request)

        assertEquals("user1", result.userId)
        assertEquals(AlertConditionType.ABOVE, result.conditionType)
        assertEquals(BigDecimal("70000"), result.threshold)
        verify { alertRuleRepository.save(any()) }
    }

    @Test
    fun `createAlertRule - 존재하지 않는 지표`() {
        val request = AlertRuleRequest(
            userId = "user1",
            indicatorId = 999L,
            conditionType = AlertConditionType.ABOVE,
            threshold = BigDecimal("70000")
        )
        every { indicatorRepository.findById(999L) } returns Optional.empty()

        assertThrows<NotFoundException> {
            alertService.createAlertRule(request)
        }
    }

    @Test
    fun `getAlertHistory - 사용자별 이력 조회`() {
        val pageable = PageRequest.of(0, 20)
        val rule = AlertRule(
            userId = "user1",
            indicator = indicator,
            conditionType = AlertConditionType.ABOVE,
            threshold = BigDecimal("70000")
        ).apply {
            val idField = AlertRule::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
        val history = AlertHistory(
            userId = "user1",
            indicator = indicator,
            alertRule = rule,
            conditionType = AlertConditionType.ABOVE,
            threshold = BigDecimal("70000"),
            actualValue = BigDecimal("71000"),
            message = "비트코인이(가) 70000 이상에 도달했습니다."
        ).apply {
            val idField = AlertHistory::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
        every { alertHistoryRepository.findByUserIdOrderByCreatedAtDesc("user1", pageable) } returns PageImpl(listOf(history))

        val result = alertService.getAlertHistory("user1", pageable)

        assertEquals(1, result.totalElements)
        assertEquals("user1", result.content[0].userId)
    }

    @Test
    fun `checkAlerts - ABOVE 조건 트리거`() {
        val rule = AlertRule(
            userId = "user1",
            indicator = indicator,
            conditionType = AlertConditionType.ABOVE,
            threshold = BigDecimal("70000")
        ).apply {
            val idField = AlertRule::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
        val latestData = IndicatorData(
            indicator = indicator,
            date = LocalDate.now(),
            value = BigDecimal("71000")
        )

        every { alertRuleRepository.findByIndicatorIdAndEnabledTrue(1L) } returns listOf(rule)
        every { indicatorDataRepository.findTopByIndicatorIdOrderByDateDesc(1L) } returns latestData
        every { alertHistoryRepository.save(any()) } answers { firstArg() }

        alertService.checkAlerts(1L)

        verify { alertHistoryRepository.save(match { it.actualValue == BigDecimal("71000") }) }
    }

    @Test
    fun `checkAlerts - BELOW 조건 미트리거`() {
        val rule = AlertRule(
            userId = "user1",
            indicator = indicator,
            conditionType = AlertConditionType.BELOW,
            threshold = BigDecimal("60000")
        ).apply {
            val idField = AlertRule::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
        val latestData = IndicatorData(
            indicator = indicator,
            date = LocalDate.now(),
            value = BigDecimal("65000")
        )

        every { alertRuleRepository.findByIndicatorIdAndEnabledTrue(1L) } returns listOf(rule)
        every { indicatorDataRepository.findTopByIndicatorIdOrderByDateDesc(1L) } returns latestData

        alertService.checkAlerts(1L)

        verify(exactly = 0) { alertHistoryRepository.save(any()) }
    }

    @Test
    fun `checkAlerts - CHANGE_PCT 조건 트리거`() {
        val rule = AlertRule(
            userId = "user1",
            indicator = indicator,
            conditionType = AlertConditionType.CHANGE_PCT,
            threshold = BigDecimal("5")
        ).apply {
            val idField = AlertRule::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
        val latestData = IndicatorData(
            indicator = indicator,
            date = LocalDate.now(),
            value = BigDecimal("71000"),
            change = BigDecimal("7.5")
        )

        every { alertRuleRepository.findByIndicatorIdAndEnabledTrue(1L) } returns listOf(rule)
        every { indicatorDataRepository.findTopByIndicatorIdOrderByDateDesc(1L) } returns latestData
        every { alertHistoryRepository.save(any()) } answers { firstArg() }

        alertService.checkAlerts(1L)

        verify { alertHistoryRepository.save(any()) }
    }

    @Test
    fun `checkAlerts - 규칙 없으면 아무것도 하지 않음`() {
        every { alertRuleRepository.findByIndicatorIdAndEnabledTrue(1L) } returns emptyList()

        alertService.checkAlerts(1L)

        verify(exactly = 0) { indicatorDataRepository.findTopByIndicatorIdOrderByDateDesc(any()) }
    }

    @Test
    fun `checkAlerts - 데이터 없으면 아무것도 하지 않음`() {
        val rule = AlertRule(
            userId = "user1",
            indicator = indicator,
            conditionType = AlertConditionType.ABOVE,
            threshold = BigDecimal("70000")
        )
        every { alertRuleRepository.findByIndicatorIdAndEnabledTrue(1L) } returns listOf(rule)
        every { indicatorDataRepository.findTopByIndicatorIdOrderByDateDesc(1L) } returns null

        alertService.checkAlerts(1L)

        verify(exactly = 0) { alertHistoryRepository.save(any()) }
    }
}
