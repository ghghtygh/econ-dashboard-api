package com.econdashboard.client.fred

import com.econdashboard.client.common.DataSourceClient
import com.econdashboard.client.common.ExternalDataPoint
import com.econdashboard.client.common.RetryUtils
import com.econdashboard.enums.DataSource
import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class FredClient(
    private val fredWebClient: WebClient,
    @Value("\${fred.api-key:}") private val apiKey: String
) : DataSourceClient {

    private val log = LoggerFactory.getLogger(javaClass)

    override val source = DataSource.FRED

    override fun fetchLatestPrice(symbol: String): ExternalDataPoint? {
        return try {
            val data = fetchHistoricalData(symbol, LocalDate.now().minusMonths(3), LocalDate.now())
            data.lastOrNull()
        } catch (e: Exception) {
            log.error("FRED fetchLatestPrice failed for {}: {}", symbol, e.message)
            null
        }
    }

    override fun fetchHistoricalData(symbol: String, from: LocalDate, to: LocalDate): List<ExternalDataPoint> {
        if (apiKey.isBlank()) {
            log.warn("FRED API key is not configured. Skipping data collection for {}", symbol)
            return emptyList()
        }

        return try {
            val response = fredWebClient.get()
                .uri { builder ->
                    builder.path("/series/observations")
                        .queryParam("series_id", symbol)
                        .queryParam("api_key", apiKey)
                        .queryParam("file_type", "json")
                        .queryParam("observation_start", from.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .queryParam("observation_end", to.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .queryParam("sort_order", "asc")
                        .build()
                }
                .retrieve()
                .bodyToMono(JsonNode::class.java)
                .retryWhen(RetryUtils.exponentialBackoff())
                .block() ?: return emptyList()

            parseObservations(response)
        } catch (e: Exception) {
            log.error("FRED fetchHistoricalData failed for {}: {}", symbol, e.message)
            emptyList()
        }
    }

    private fun parseObservations(response: JsonNode): List<ExternalDataPoint> {
        val observations = response.path("observations")
        if (observations.isEmpty || !observations.isArray) return emptyList()

        val dataPoints = mutableListOf<ExternalDataPoint>()

        for (obs in observations) {
            val dateStr = obs.path("date").asText()
            val valueStr = obs.path("value").asText()

            // FRED returns "." for missing values
            if (valueStr == "." || valueStr.isBlank()) continue

            try {
                val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                val value = BigDecimal(valueStr).setScale(6, RoundingMode.HALF_UP)
                dataPoints.add(ExternalDataPoint(date = date, value = value))
            } catch (e: Exception) {
                log.debug("Skipping invalid FRED observation: date={}, value={}", dateStr, valueStr)
            }
        }

        // Calculate change percentages
        return dataPoints.mapIndexed { index, point ->
            if (index > 0) {
                val prev = dataPoints[index - 1].value
                val change = if (prev.compareTo(BigDecimal.ZERO) != 0) {
                    point.value.subtract(prev)
                        .divide(prev, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal(100))
                } else null
                point.copy(change = change)
            } else {
                point
            }
        }
    }
}
