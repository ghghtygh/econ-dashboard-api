package com.econdashboard.controller

import com.econdashboard.dto.IndicatorResponse
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.exception.GlobalExceptionHandler
import com.econdashboard.exception.NotFoundException
import com.econdashboard.service.IndicatorService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(IndicatorController::class)
@Import(GlobalExceptionHandler::class)
@DisplayName("IndicatorController 테스트")
class IndicatorControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var indicatorService: IndicatorService

    private fun createResponse(
        id: Long = 1L,
        name: String = "S&P 500",
        symbol: String = "^GSPC",
        category: IndicatorCategory = IndicatorCategory.STOCK,
    ): IndicatorResponse {
        return IndicatorResponse(
            id = id,
            name = name,
            symbol = symbol,
            category = category,
            unit = "USD",
            source = DataSource.YAHOO,
            description = null,
            createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2026, 1, 1, 0, 0),
        )
    }

    @Nested
    @DisplayName("GET /api/indicators")
    inner class GetIndicators {

        @Test
        @DisplayName("전체 지표 목록을 반환한다")
        fun shouldReturnAllIndicators() {
            val responses = listOf(
                createResponse(1L, "S&P 500", "^GSPC"),
                createResponse(2L, "Bitcoin", "bitcoin", IndicatorCategory.CRYPTO),
            )
            every { indicatorService.getAllIndicators(null) } returns responses

            mockMvc.perform(get("/api/indicators"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("S&P 500"))
                .andExpect(jsonPath("$.data[1].name").value("Bitcoin"))
        }

        @Test
        @DisplayName("카테고리 필터로 지표를 조회한다")
        fun shouldReturnFilteredIndicators() {
            val responses = listOf(createResponse(1L, "S&P 500", "^GSPC"))
            every { indicatorService.getAllIndicators(IndicatorCategory.STOCK) } returns responses

            mockMvc.perform(get("/api/indicators").param("category", "STOCK"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].category").value("STOCK"))
        }

        @Test
        @DisplayName("결과가 없으면 빈 배열을 반환한다")
        fun shouldReturnEmptyArray() {
            every { indicatorService.getAllIndicators(any()) } returns emptyList()

            mockMvc.perform(get("/api/indicators"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(0))
        }
    }

    @Nested
    @DisplayName("GET /api/indicators/{id}")
    inner class GetIndicatorById {

        @Test
        @DisplayName("존재하는 ID로 조회 시 지표를 반환한다")
        fun shouldReturnIndicator() {
            every { indicatorService.getIndicatorById(1L) } returns createResponse()

            mockMvc.perform(get("/api/indicators/1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("S&P 500"))
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 404를 반환한다")
        fun shouldReturn404ForInvalidId() {
            every { indicatorService.getIndicatorById(999L) } throws NotFoundException("Indicator", 999L)

            mockMvc.perform(get("/api/indicators/999"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/indicators/categories")
    inner class GetCategories {

        @Test
        @DisplayName("모든 카테고리를 반환한다")
        fun shouldReturnAllCategories() {
            every { indicatorService.getCategories() } returns IndicatorCategory.entries.toList()

            mockMvc.perform(get("/api/indicators/categories"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data[0]").value("STOCK"))
        }
    }
}
