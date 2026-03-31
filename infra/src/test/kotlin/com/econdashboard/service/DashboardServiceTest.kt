package com.econdashboard.service

import com.econdashboard.domain.DashboardWidget
import com.econdashboard.domain.Indicator
import com.econdashboard.dto.DashboardWidgetRequest
import com.econdashboard.enums.ChartType
import com.econdashboard.enums.DataSource
import com.econdashboard.enums.IndicatorCategory
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.DashboardWidgetRepository
import com.econdashboard.repository.IndicatorRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals

class DashboardServiceTest {

    private val dashboardWidgetRepository: DashboardWidgetRepository = mockk()
    private val indicatorRepository: IndicatorRepository = mockk()

    private lateinit var dashboardService: DashboardService
    private lateinit var indicator: Indicator

    @BeforeEach
    fun setUp() {
        dashboardService = DashboardService(dashboardWidgetRepository, indicatorRepository)
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

    private fun createWidget(id: Long = 1L, title: String = "주가지수"): DashboardWidget {
        return DashboardWidget(
            title = title,
            chartType = ChartType.LINE,
            positionX = 0,
            positionY = 0,
            width = 2,
            height = 1,
            indicator = indicator
        ).apply {
            val idField = DashboardWidget::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, id)
        }
    }

    @Test
    fun `getAllWidgets - 위젯 목록 조회`() {
        val widget = createWidget()
        every { dashboardWidgetRepository.findAllByOrderByPositionYAscPositionXAsc() } returns listOf(widget)

        val result = dashboardService.getAllWidgets()

        assertEquals(1, result.size)
        assertEquals("주가지수", result[0].title)
        assertEquals(ChartType.LINE, result[0].chartType)
    }

    @Test
    fun `createWidget - 정상 생성`() {
        val request = DashboardWidgetRequest(
            title = "비트코인",
            chartType = ChartType.CANDLESTICK,
            positionX = 0,
            positionY = 0,
            width = 2,
            height = 1,
            indicatorId = 1L
        )
        every { indicatorRepository.findById(1L) } returns Optional.of(indicator)
        every { dashboardWidgetRepository.save(any()) } answers {
            (firstArg() as DashboardWidget).apply {
                val idField = DashboardWidget::class.java.getDeclaredField("id")
                idField.isAccessible = true
                idField.set(this, 1L)
            }
        }

        val result = dashboardService.createWidget(request)

        assertEquals("비트코인", result.title)
        assertEquals(ChartType.CANDLESTICK, result.chartType)
        assertEquals(1L, result.indicatorId)
    }

    @Test
    fun `createWidget - indicatorId 없이 생성`() {
        val request = DashboardWidgetRequest(
            title = "뉴스 위젯",
            chartType = ChartType.NUMBER,
            positionX = 0,
            positionY = 0,
            width = 1,
            height = 1
        )
        every { dashboardWidgetRepository.save(any()) } answers {
            (firstArg() as DashboardWidget).apply {
                val idField = DashboardWidget::class.java.getDeclaredField("id")
                idField.isAccessible = true
                idField.set(this, 2L)
            }
        }

        val result = dashboardService.createWidget(request)

        assertEquals("뉴스 위젯", result.title)
        assertEquals(null, result.indicatorId)
    }

    @Test
    fun `updateWidget - 정상 수정`() {
        val widget = createWidget()
        val request = DashboardWidgetRequest(
            title = "수정된 위젯",
            chartType = ChartType.BAR,
            positionX = 1,
            positionY = 1,
            width = 3,
            height = 2,
            indicatorId = 1L
        )

        every { dashboardWidgetRepository.findById(1L) } returns Optional.of(widget)
        every { indicatorRepository.findById(1L) } returns Optional.of(indicator)
        every { dashboardWidgetRepository.save(any()) } answers { firstArg() }

        val result = dashboardService.updateWidget(1L, request)

        assertEquals("수정된 위젯", result.title)
        assertEquals(ChartType.BAR, result.chartType)
        assertEquals(3, result.width)
    }

    @Test
    fun `updateWidget - 존재하지 않는 위젯`() {
        every { dashboardWidgetRepository.findById(999L) } returns Optional.empty()

        assertThrows<NotFoundException> {
            dashboardService.updateWidget(999L, DashboardWidgetRequest(
                title = "test", chartType = ChartType.LINE, positionX = 0, positionY = 0
            ))
        }
    }

    @Test
    fun `deleteWidget - 정상 삭제`() {
        every { dashboardWidgetRepository.existsById(1L) } returns true
        every { dashboardWidgetRepository.deleteById(1L) } just runs

        dashboardService.deleteWidget(1L)

        verify { dashboardWidgetRepository.deleteById(1L) }
    }

    @Test
    fun `deleteWidget - 존재하지 않는 위젯`() {
        every { dashboardWidgetRepository.existsById(999L) } returns false

        assertThrows<NotFoundException> {
            dashboardService.deleteWidget(999L)
        }
    }

    @Test
    fun `saveWidgets - 일괄 저장`() {
        val requests = listOf(
            DashboardWidgetRequest(
                title = "위젯1",
                chartType = ChartType.LINE,
                positionX = 0,
                positionY = 0,
                indicatorId = 1L
            ),
            DashboardWidgetRequest(
                title = "위젯2",
                chartType = ChartType.BAR,
                positionX = 1,
                positionY = 0
            )
        )

        every { dashboardWidgetRepository.deleteAll() } just runs
        every { indicatorRepository.findById(1L) } returns Optional.of(indicator)
        every { dashboardWidgetRepository.saveAll(any<List<DashboardWidget>>()) } answers {
            (firstArg() as List<DashboardWidget>).mapIndexed { idx, w ->
                w.apply {
                    val idField = DashboardWidget::class.java.getDeclaredField("id")
                    idField.isAccessible = true
                    idField.set(this, (idx + 1).toLong())
                }
            }
        }

        val result = dashboardService.saveWidgets(requests)

        assertEquals(2, result.size)
        assertEquals("위젯1", result[0].title)
        assertEquals("위젯2", result[1].title)
        verify { dashboardWidgetRepository.deleteAll() }
    }
}
