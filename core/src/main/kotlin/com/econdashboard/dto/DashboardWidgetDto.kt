package com.econdashboard.dto

import com.econdashboard.domain.DashboardWidget
import com.econdashboard.enums.ChartType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class DashboardWidgetRequest(
    @field:NotBlank
    val title: String,

    @field:NotNull
    val chartType: ChartType,

    @field:Min(0)
    val positionX: Int,

    @field:Min(0)
    val positionY: Int,

    @field:Min(1)
    val width: Int = 1,

    @field:Min(1)
    val height: Int = 1,

    val config: String? = null,

    val indicatorId: Long? = null
)

data class DashboardWidgetResponse(
    val id: Long,
    val title: String,
    val chartType: ChartType,
    val positionX: Int,
    val positionY: Int,
    val width: Int,
    val height: Int,
    val config: String?,
    val indicatorId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(widget: DashboardWidget) = DashboardWidgetResponse(
            id = widget.id,
            title = widget.title,
            chartType = widget.chartType,
            positionX = widget.positionX,
            positionY = widget.positionY,
            width = widget.width,
            height = widget.height,
            config = widget.config,
            indicatorId = widget.indicator?.id,
            createdAt = widget.createdAt,
            updatedAt = widget.updatedAt
        )
    }
}
