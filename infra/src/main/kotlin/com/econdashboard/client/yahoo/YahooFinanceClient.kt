package com.econdashboard.client.yahoo

import com.econdashboard.client.common.DataSourceClient
import com.econdashboard.client.common.ExternalDataPoint
import com.econdashboard.client.common.RetryUtils
import com.econdashboard.enums.DataSource
import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class YahooFinanceClient(
    private val yahooFinanceWebClient: WebClient
) : DataSourceClient {

    private val log = LoggerFactory.getLogger(javaClass)

    override val source = DataSource.YAHOO

    override fun fetchLatestPrice(symbol: String): ExternalDataPoint? {
        return try {
            val data = fetchHistoricalData(symbol, LocalDate.now().minusDays(7), LocalDate.now())
            data.lastOrNull()
        } catch (e: Exception) {
            log.error("Yahoo fetchLatestPrice failed for {}: {}", symbol, e.message)
            null
        }
    }

    override fun fetchHistoricalData(symbol: String, from: LocalDate, to: LocalDate): List<ExternalDataPoint> {
        return try {
            val fromEpoch = from.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
            val toEpoch = to.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC)

            val response = yahooFinanceWebClient.get()
                .uri("/chart/{symbol}?period1={from}&period2={to}&interval=1d",
                    symbol, fromEpoch, toEpoch)
                .retrieve()
                .bodyToMono(JsonNode::class.java)
                .retryWhen(RetryUtils.exponentialBackoff())
                .block() ?: return emptyList()

            parseChartResponse(response)
        } catch (e: Exception) {
            log.error("Yahoo fetchHistoricalData failed for {}: {}", symbol, e.message)
            emptyList()
        }
    }

    private fun parseChartResponse(response: JsonNode): List<ExternalDataPoint> {
        val result = response.path("chart").path("result")
        if (result.isEmpty || !result.isArray) return emptyList()

        val chartData = result[0]
        val timestamps = chartData.path("timestamp")
        val quote = chartData.path("indicators").path("quote")[0]

        if (timestamps.isEmpty) return emptyList()

        val opens = quote.path("open")
        val highs = quote.path("high")
        val lows = quote.path("low")
        val closes = quote.path("close")
        val volumes = quote.path("volume")

        return (0 until timestamps.size()).mapNotNull { i ->
            val close = closes[i]?.asDouble()?.takeIf { it > 0.0 } ?: return@mapNotNull null
            val date = Instant.ofEpochSecond(timestamps[i].asLong())
                .atZone(ZoneOffset.UTC).toLocalDate()

            ExternalDataPoint(
                date = date,
                value = BigDecimal.valueOf(close).setScale(6, RoundingMode.HALF_UP),
                open = opens[i]?.asDouble()?.let { BigDecimal.valueOf(it).setScale(6, RoundingMode.HALF_UP) },
                high = highs[i]?.asDouble()?.let { BigDecimal.valueOf(it).setScale(6, RoundingMode.HALF_UP) },
                low = lows[i]?.asDouble()?.let { BigDecimal.valueOf(it).setScale(6, RoundingMode.HALF_UP) },
                close = BigDecimal.valueOf(close).setScale(6, RoundingMode.HALF_UP),
                volume = volumes[i]?.asLong()?.let { BigDecimal.valueOf(it) }
            )
        }
    }
}
