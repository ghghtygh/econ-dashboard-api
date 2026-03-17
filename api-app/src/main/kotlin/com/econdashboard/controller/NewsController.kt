package com.econdashboard.controller

import com.econdashboard.common.ApiResponse
import com.econdashboard.dto.NewsArticleResponse
import com.econdashboard.enums.NewsCategory
import com.econdashboard.service.NewsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@Tag(name = "News", description = "경제 뉴스 API")
@RestController
@RequestMapping("/api/news")
class NewsController(
    private val newsService: NewsService
) {

    @Operation(summary = "뉴스 목록 조회", description = "카테고리별 필터링 및 페이징 지원")
    @GetMapping
    fun getNewsList(
        @Parameter(description = "카테고리 필터")
        @RequestParam(required = false) category: NewsCategory?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<NewsArticleResponse>> {
        return ApiResponse.success(newsService.getNewsList(category, pageable))
    }

    @Operation(summary = "뉴스 상세 조회")
    @GetMapping("/{id}")
    fun getNews(
        @Parameter(description = "뉴스 ID") @PathVariable id: Long
    ): ApiResponse<NewsArticleResponse> {
        return ApiResponse.success(newsService.getNewsById(id))
    }
}
