package com.econdashboard.controller

import com.econdashboard.dto.AlertHistoryResponse
import com.econdashboard.dto.AlertRuleResponse
import com.econdashboard.enums.AlertConditionType
import com.econdashboard.exception.GlobalExceptionHandler
import com.econdashboard.exception.NotFoundException
import com.econdashboard.service.AlertService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.LocalDateTime

@WebMvcTest(AlertController::class)
class AlertControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var alertService: AlertService

    private val now = LocalDateTime.of(2025, 6, 1, 12, 0, 0)

    @Test
    fun `POST alert rule - 정상 생성`() {
        val response = AlertRuleResponse(
            id = "1",
            indicatorId = 1L,
            indicatorName = "비트코인",
            condition = "above",
            threshold = BigDecimal("70000"),
            severity = "warning",
            message = "",
            enabled = true,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )
        every { alertService.createAlertRule(any()) } returns response

        mockMvc.post("/api/alerts/rules") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"user1","indicatorId":1,"condition":"above","threshold":70000}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data.condition") { value("above") }
        }
    }

    @Test
    fun `POST alert rule - 존재하지 않는 지표`() {
        every { alertService.createAlertRule(any()) } throws NotFoundException("Indicator", 999L)

        mockMvc.post("/api/alerts/rules") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"user1","indicatorId":999,"condition":"above","threshold":70000}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error.code") { value("NOT_FOUND") }
        }
    }

    @Test
    fun `GET alert rules - 규칙 목록 조회`() {
        val response = AlertRuleResponse(
            id = "1",
            indicatorId = 1L,
            indicatorName = "비트코인",
            condition = "above",
            threshold = BigDecimal("70000"),
            severity = "warning",
            message = "",
            enabled = true,
            createdAt = now.toString(),
            updatedAt = now.toString()
        )
        every { alertService.getAlertRules() } returns listOf(response)

        mockMvc.get("/api/alerts/rules")
            .andExpect {
                status { isOk() }
                jsonPath("$.data[0].id") { value("1") }
                jsonPath("$.data[0].condition") { value("above") }
            }
    }

    @Test
    fun `GET alert history - 사용자별 이력`() {
        val history = AlertHistoryResponse(
            id = 1L,
            userId = "user1",
            indicatorId = 1L,
            indicatorName = "비트코인",
            alertRuleId = 1L,
            conditionType = AlertConditionType.ABOVE,
            threshold = BigDecimal("70000"),
            actualValue = BigDecimal("71000"),
            message = "비트코인이(가) 70000 이상에 도달했습니다.",
            createdAt = now
        )
        every { alertService.getAlertHistory("user1", any()) } returns PageImpl(listOf(history))

        mockMvc.get("/api/alerts?userId=user1")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.content[0].userId") { value("user1") }
                jsonPath("$.data.content[0].actualValue") { value(71000) }
            }
    }
}
