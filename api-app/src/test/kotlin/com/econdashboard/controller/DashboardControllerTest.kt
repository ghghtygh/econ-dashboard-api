package com.econdashboard.controller

import com.econdashboard.dto.DashboardWidgetResponse
import com.econdashboard.enums.ChartType
import com.econdashboard.exception.GlobalExceptionHandler
import com.econdashboard.exception.NotFoundException
import com.econdashboard.service.DashboardService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDateTime

@WebMvcTest(DashboardController::class)
class DashboardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var dashboardService: DashboardService

    private val now = LocalDateTime.of(2025, 6, 1, 12, 0, 0)

    private fun sampleWidgetResponse(id: Long = 1L) = DashboardWidgetResponse(
        id = id,
        title = "주가지수",
        chartType = ChartType.LINE,
        positionX = 0,
        positionY = 0,
        width = 2,
        height = 1,
        config = null,
        indicatorId = 1L,
        createdAt = now,
        updatedAt = now
    )

    @Test
    fun `GET widgets - 위젯 목록 조회`() {
        every { dashboardService.getAllWidgets() } returns listOf(sampleWidgetResponse())

        mockMvc.get("/api/dashboard/widgets")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[0].title") { value("주가지수") }
                jsonPath("$.data[0].chartType") { value("LINE") }
            }
    }

    @Test
    fun `POST widgets - 일괄 저장`() {
        every { dashboardService.saveWidgets(any()) } returns listOf(sampleWidgetResponse())

        mockMvc.post("/api/dashboard/widgets") {
            contentType = MediaType.APPLICATION_JSON
            content = """[{"title":"주가지수","chartType":"LINE","positionX":0,"positionY":0,"width":2,"height":1,"indicatorId":1}]"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data[0].title") { value("주가지수") }
        }
    }

    @Test
    fun `PUT widget - 수정`() {
        every { dashboardService.updateWidget(1L, any()) } returns sampleWidgetResponse().copy(title = "수정됨")

        mockMvc.put("/api/dashboard/widgets/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"수정됨","chartType":"LINE","positionX":0,"positionY":0,"width":2,"height":1}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.title") { value("수정됨") }
        }
    }

    @Test
    fun `PUT widget - 미존재시 404`() {
        every { dashboardService.updateWidget(999L, any()) } throws NotFoundException("DashboardWidget", 999L)

        mockMvc.put("/api/dashboard/widgets/999") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"test","chartType":"LINE","positionX":0,"positionY":0}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error.code") { value("NOT_FOUND") }
        }
    }

    @Test
    fun `DELETE widget - 삭제`() {
        every { dashboardService.deleteWidget(1L) } just runs

        mockMvc.delete("/api/dashboard/widgets/1")
            .andExpect {
                status { isNoContent() }
            }
    }

    @Test
    fun `DELETE widget - 미존재시 404`() {
        every { dashboardService.deleteWidget(999L) } throws NotFoundException("DashboardWidget", 999L)

        mockMvc.delete("/api/dashboard/widgets/999")
            .andExpect {
                status { isNotFound() }
            }
    }
}
