package com.econdashboard.client.coingecko

import com.econdashboard.client.common.DataSourceClient
import com.econdashboard.client.common.ExternalDataPoint
import com.econdashboard.client.common.RetryUtils
import com.econdashboard.enums.DataSource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class CoinGeckoClient(
    private val coinGeckoWebClient: WebClient
) : DataSourceClient {

    private val log = LoggerFactory.getLogger(javaClass)

    override val source = DataSource.COINGECKO

    override fun fetchLatestPrice(symbol: String): ExternalDataPoint? {
        return try {
            val response = coinGeckoWebClient.get()
                .uri("/simple/price?ids={id}&vs_currencies=usd&include_24hr_change=true", symbol)
                .retrieve()
                .bodyToMono(Map::class.java)
                .retryWhen(RetryUtils.exponentialBackoff())
                .block()

            val data = response?.get(symbol) as? Map<*, *> ?: return null
            val price = toBigDecimal(data["usd"]) ?: return null
            val change = toBigDecimal(data["usd_24h_change"])

            ExternalDataPoint(
                date = LocalDate.now(),
                value = price,
                change = change?.setScale(4, RoundingMode.HALF_UP)
            )
        } catch (e: Exception) {
            log.error("CoinGecko fetchLatestPrice failed for {}: {}", symbol, e.message)
            null
        }
    }

    override fun fetchHistoricalData(symbol: String, from: LocalDate, to: LocalDate): List<ExternalDataPoint> {
        return try {
            val fromEpoch = from.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
            val toEpoch = to.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC)

            val response = coinGeckoWebClient.get()
                .uri("/coins/{id}/market_chart/range?vs_currency=usd&from={from}&to={to}",
                    symbol, fromEpoch, toEpoch)
                .retrieve()
                .bodyToMono(CoinGeckoMarketChartResponse::class.java)
                .retryWhen(RetryUtils.exponentialBackoff())
                .block() ?: return emptyList()

            response.prices
                .groupBy { entry ->
                    LocalDate.ofEpochDay(entry[0].toLong() / 1000 / 86400)
                }
                .map { (date, entries) ->
                    val lastPrice = toBigDecimal(entries.last()[1]) ?: BigDecimal.ZERO
                    ExternalDataPoint(
                        date = date,
                        value = lastPrice
                    )
                }
                .sortedBy { it.date }
        } catch (e: Exception) {
            log.error("CoinGecko fetchHistoricalData failed for {}: {}", symbol, e.message)
            emptyList()
        }
    }

    private fun toBigDecimal(value: Any?): BigDecimal? {
        return when (value) {
            is Number -> BigDecimal.valueOf(value.toDouble())
            is String -> value.toBigDecimalOrNull()
            else -> null
        }
    }
}

data class CoinGeckoMarketChartResponse(
    val prices: List<List<Double>> = emptyList()
)
