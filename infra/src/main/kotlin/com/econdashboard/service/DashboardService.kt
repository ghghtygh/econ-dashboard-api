package com.econdashboard.service

import com.econdashboard.domain.DashboardWidget
import com.econdashboard.dto.DashboardWidgetRequest
import com.econdashboard.dto.DashboardWidgetResponse
import com.econdashboard.exception.NotFoundException
import com.econdashboard.repository.DashboardWidgetRepository
import com.econdashboard.repository.IndicatorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DashboardService(
    private val dashboardWidgetRepository: DashboardWidgetRepository,
    private val indicatorRepository: IndicatorRepository
) {

    fun getAllWidgets(): List<DashboardWidgetResponse> {
        return dashboardWidgetRepository.findAllByOrderByPositionYAscPositionXAsc()
            .map { DashboardWidgetResponse.from(it) }
    }

    @Transactional
    fun createWidget(request: DashboardWidgetRequest): DashboardWidgetResponse {
        val indicator = request.indicatorId?.let {
            indicatorRepository.findById(it)
                .orElseThrow { NotFoundException("Indicator", it) }
        }

        val widget = DashboardWidget(
            title = request.title,
            chartType = request.chartType,
            positionX = request.positionX,
            positionY = request.positionY,
            width = request.width,
            height = request.height,
            config = request.config,
            indicator = indicator
        )

        return DashboardWidgetResponse.from(dashboardWidgetRepository.save(widget))
    }

    @Transactional
    fun saveWidgets(requests: List<DashboardWidgetRequest>): List<DashboardWidgetResponse> {
        dashboardWidgetRepository.deleteAll()

        val widgets = requests.map { request ->
            val indicator = request.indicatorId?.let {
                indicatorRepository.findById(it)
                    .orElseThrow { NotFoundException("Indicator", it) }
            }

            DashboardWidget(
                title = request.title,
                chartType = request.chartType,
                positionX = request.positionX,
                positionY = request.positionY,
                width = request.width,
                height = request.height,
                config = request.config,
                indicator = indicator
            )
        }

        return dashboardWidgetRepository.saveAll(widgets)
            .map { DashboardWidgetResponse.from(it) }
    }

    @Transactional
    fun updateWidget(id: Long, request: DashboardWidgetRequest): DashboardWidgetResponse {
        val widget = dashboardWidgetRepository.findById(id)
            .orElseThrow { NotFoundException("DashboardWidget", id) }

        val indicator = request.indicatorId?.let {
            indicatorRepository.findById(it)
                .orElseThrow { NotFoundException("Indicator", it) }
        }

        widget.title = request.title
        widget.chartType = request.chartType
        widget.positionX = request.positionX
        widget.positionY = request.positionY
        widget.width = request.width
        widget.height = request.height
        widget.config = request.config
        widget.indicator = indicator

        return DashboardWidgetResponse.from(dashboardWidgetRepository.save(widget))
    }

    @Transactional
    fun deleteWidget(id: Long) {
        if (!dashboardWidgetRepository.existsById(id)) {
            throw NotFoundException("DashboardWidget", id)
        }
        dashboardWidgetRepository.deleteById(id)
    }
}
