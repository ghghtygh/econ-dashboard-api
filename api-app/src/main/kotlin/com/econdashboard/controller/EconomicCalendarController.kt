package com.econdashboard.controller

import com.econdashboard.common.ApiResponse
import com.econdashboard.dto.EconomicEventResponse
import com.econdashboard.enums.EventImportance
import com.econdashboard.service.EconomicCalendarService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Economic Calendar", description = "경제 캘린더 API")
@RestController
@RequestMapping("/api/calendar")
class EconomicCalendarController(
    private val calendarService: EconomicCalendarService
) {

    @Operation(summary = "경제 이벤트 조회", description = "날짜 범위, 국가, 중요도별 필터링 지원")
    @GetMapping
    fun getEvents(
        @Parameter(description = "시작일 (yyyy-MM-dd)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @Parameter(description = "종료일 (yyyy-MM-dd)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @Parameter(description = "국가 코드 (예: US, KR)")
        @RequestParam(required = false) country: String?,
        @Parameter(description = "중요도 필터 (HIGH, MEDIUM, LOW)")
        @RequestParam(required = false) importance: EventImportance?
    ): ApiResponse<List<EconomicEventResponse>> {
        return ApiResponse.success(calendarService.getEvents(from, to, country, importance))
    }

    @Operation(summary = "예정 이벤트 조회", description = "현재 시점 이후 예정된 경제 이벤트")
    @GetMapping("/upcoming")
    fun getUpcomingEvents(
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<EconomicEventResponse>> {
        return ApiResponse.success(calendarService.getUpcomingEvents(pageable))
    }

    @Operation(summary = "이벤트 상세 조회")
    @GetMapping("/{id}")
    fun getEvent(
        @Parameter(description = "이벤트 ID") @PathVariable id: Long
    ): ApiResponse<EconomicEventResponse> {
        return ApiResponse.success(calendarService.getEventById(id))
    }
}
