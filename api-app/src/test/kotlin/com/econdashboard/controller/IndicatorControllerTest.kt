package com.econdashboard.controller

import com.econdashboard.dto.IndicatorDataResponse
import com.econdashboard.dto.IndicatorResponse
import com.econdashboard.dto.IndicatorSeriesResponse
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.exception.GlobalExceptionHandler
import com.econdashboard.exception.NotFoundException
import com.econdashboard.service.IndicatorService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(IndicatorController::class)
class IndicatorControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var indicatorService: IndicatorService

    private val now = LocalDateTime.of(2025, 6, 1, 12, 0, 0)

    private fun sampleIndicatorResponse(id: Long = 1L) = IndicatorResponse(
        id = id,
        name = "S&P 500",
        symbol = "SPX",
        category = IndicatorCategory.STOCK,
        unit = "포인트",
        source = DataSource.YAHOO,
        description = "미국 대표 주가지수",
        createdAt = now,
        updatedAt = now
    )

    @Test
    fun `GET indicators - 전체 조회`() {
        every { indicatorService.getAllIndicators(null) } returns listOf(sampleIndicatorResponse())

        mockMvc.get("/api/indicators")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[0].name") { value("S&P 500") }
                jsonPath("$.data[0].symbol") { value("SPX") }
            }
    }

    @Test
    fun `GET indicators - 카테고리 필터`() {
        every { indicatorService.getAllIndicators(IndicatorCategory.STOCK) } returns listOf(sampleIndicatorResponse())

        mockMvc.get("/api/indicators?category=STOCK")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.length()") { value(1) }
            }
    }

    @Test
    fun `GET indicator by id - 정상`() {
        every { indicatorService.getIndicatorById(1L) } returns sampleIndicatorResponse()

        mockMvc.get("/api/indicators/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(1) }
                jsonPath("$.data.name") { value("S&P 500") }
            }
    }

    @Test
    fun `GET indicator by id - 미존재시 404`() {
        every { indicatorService.getIndicatorById(999L) } throws NotFoundException("Indicator", 999L)

        mockMvc.get("/api/indicators/999")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("NOT_FOUND") }
            }
    }

    @Test
    fun `GET categories - 전체 카테고리`() {
        every { indicatorService.getCategories() } returns IndicatorCategory.entries.toList()

        mockMvc.get("/api/indicators/categories")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }
    }

    @Test
    fun `GET indicator data - 시계열 데이터`() {
        val dataResponse = IndicatorDataResponse(
            id = 1L,
            indicatorId = 1L,
            date = LocalDate.of(2025, 6, 1),
            value = BigDecimal("5000.00"),
            open = null,
            high = null,
            low = null,
            close = null,
            volume = null,
            change = BigDecimal("1.5")
        )
        every { indicatorService.getIndicatorData(1L, any(), any(), any()) } returns
            PageImpl(listOf(dataResponse), PageRequest.of(0, 100), 1)

        mockMvc.get("/api/indicators/1/data?from=2025-01-01&to=2025-12-31")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.content[0].value") { value(5000.00) }
            }
    }

    @Test
    fun `POST series - 복수 지표 조회`() {
        every { indicatorService.getMultipleIndicatorSeries(any()) } returns listOf(
            IndicatorSeriesResponse(indicatorId = 1L, data = emptyList())
        )

        mockMvc.post("/api/indicators/series") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"indicatorIds":[1],"startDate":"2025-01-01","endDate":"2025-12-31"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data[0].indicatorId") { value(1) }
        }
    }
}
