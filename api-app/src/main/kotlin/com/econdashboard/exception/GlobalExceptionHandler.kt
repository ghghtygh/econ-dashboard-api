package com.econdashboard.exception

import com.econdashboard.common.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler(
    private val environment: Environment
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val includeDebugDetail: Boolean
        get() {
            val profiles = environment.activeProfiles.toSet()
            return profiles.contains("qa") || profiles.contains("dev") || profiles.contains("local")
        }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(buildErrorResponse(ex.errorCode, ex.message, ex))
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(buildErrorResponse(ex.errorCode, ex.message, ex))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(buildErrorResponse("VALIDATION_ERROR", message, ex))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Nothing>> {
        val message = "잘못된 파라미터: ${ex.name} = ${ex.value}"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(buildErrorResponse("INVALID_PARAMETER", message, ex))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameterException(ex: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(buildErrorResponse("MISSING_PARAMETER", "필수 파라미터 누락: ${ex.parameterName}", ex))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(buildErrorResponse("INVALID_REQUEST_BODY", "요청 본문을 읽을 수 없습니다", ex))
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(buildErrorResponse("NOT_FOUND", "요청한 리소스를 찾을 수 없습니다: ${ex.resourcePath}", ex))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unexpected error occurred", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildErrorResponse("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다", ex))
    }

    private fun buildErrorResponse(code: String, message: String, ex: Exception): ApiResponse<Nothing> {
        if (!includeDebugDetail) {
            return ApiResponse.error(code, message)
        }

        val detail = ex.message?.takeIf { it != message }
        val stackTrace = ex.stackTrace
            .take(20)
            .map { it.toString() }

        return ApiResponse.error(code, message, detail, stackTrace)
    }
}
