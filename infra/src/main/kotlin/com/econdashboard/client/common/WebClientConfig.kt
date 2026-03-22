package com.econdashboard.client.common

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun coinGeckoWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://api.coingecko.com/api/v3")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    @Bean
    fun yahooFinanceWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://query1.finance.yahoo.com/v8/finance")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0")
            .build()
    }

    @Bean
    fun fredWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://api.stlouisfed.org/fred")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}
