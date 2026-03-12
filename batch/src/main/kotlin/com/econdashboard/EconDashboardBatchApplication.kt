package com.econdashboard

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class EconDashboardBatchApplication

fun main(args: Array<String>) {
    runApplication<EconDashboardBatchApplication>(*args)
}
