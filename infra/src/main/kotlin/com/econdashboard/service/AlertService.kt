package com.econdashboard.service

import com.econdashboard.domain.AlertHistory
import com.econdashboard.domain.AlertRule
import com.econdashboard.dto.AlertHistoryResponse
import com.econdashboard.dto.AlertRuleRequest
import com.econdashboard.dto.AlertRuleResponse
import com.econdashboard.enums.AlertConditionType
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.AlertHistoryRepository
import com.econdashboard.repository.AlertRuleRepository
import com.econdashboard.repository.IndicatorDataRepository
import com.econdashboard.repository.IndicatorRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
@Transactional(readOnly = true)
class AlertService(
    private val alertRuleRepository: AlertRuleRepository,
    private val alertHistoryRepository: AlertHistoryRepository,
    private val indicatorRepository: IndicatorRepository,
    private val indicatorDataRepository: IndicatorDataRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createAlertRule(request: AlertRuleRequest): AlertRuleResponse {
        val indicator = indicatorRepository.findById(request.indicatorId)
            .orElseThrow { NotFoundException("Indicator", request.indicatorId) }

        val rule = AlertRule(
            userId = request.userId,
            indicator = indicator,
            conditionType = request.conditionType,
            threshold = request.threshold
        )
        return AlertRuleResponse.from(alertRuleRepository.save(rule))
    }

    fun getAlertHistory(userId: String, pageable: Pageable): Page<AlertHistoryResponse> {
        return alertHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { AlertHistoryResponse.from(it) }
    }

    @Transactional
    fun checkAlerts(indicatorId: Long) {
        val rules = alertRuleRepository.findByIndicatorIdAndEnabledTrue(indicatorId)
        if (rules.isEmpty()) return

        val latestData = indicatorDataRepository.findTopByIndicatorIdOrderByDateDesc(indicatorId) ?: return
        val currentValue = latestData.value

        rules.forEach { rule ->
            val triggered = when (rule.conditionType) {
                AlertConditionType.ABOVE -> currentValue >= rule.threshold
                AlertConditionType.BELOW -> currentValue <= rule.threshold
                AlertConditionType.CHANGE_PCT -> {
                    val changePct = latestData.change
                    changePct != null && changePct.abs() >= rule.threshold
                }
            }

            if (triggered) {
                val message = buildAlertMessage(rule, currentValue, latestData.change)
                val history = AlertHistory(
                    userId = rule.userId,
                    indicator = rule.indicator,
                    alertRule = rule,
                    conditionType = rule.conditionType,
                    threshold = rule.threshold,
                    actualValue = currentValue,
                    message = message
                )
                alertHistoryRepository.save(history)
                log.info("Alert triggered: userId={}, indicator={}, condition={}, threshold={}, actual={}",
                    rule.userId, rule.indicator.symbol, rule.conditionType, rule.threshold, currentValue)
            }
        }
    }

    private fun buildAlertMessage(rule: AlertRule, actualValue: BigDecimal, change: BigDecimal?): String {
        val indicatorName = rule.indicator.name
        return when (rule.conditionType) {
            AlertConditionType.ABOVE ->
                "${indicatorName}이(가) ${rule.threshold} 이상에 도달했습니다. (현재: $actualValue)"
            AlertConditionType.BELOW ->
                "${indicatorName}이(가) ${rule.threshold} 이하로 하락했습니다. (현재: $actualValue)"
            AlertConditionType.CHANGE_PCT -> {
                val pct = change?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
                "${indicatorName}의 변동률이 ${rule.threshold}%를 초과했습니다. (변동률: ${pct}%)"
            }
        }
    }
}
