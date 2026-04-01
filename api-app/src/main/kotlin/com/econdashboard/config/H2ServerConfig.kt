package com.econdashboard.config

import org.h2.tools.Server
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

/**
 * H2 TCP 서버 설정 (dev 프로필 전용)
 *
 * API 서버가 H2 인메모리 DB를 TCP 서버로 노출하면,
 * Batch 서버가 TCP로 같은 DB에 접속하여 데이터를 공유할 수 있습니다.
 *
 * 흐름:
 *   API 서버 → H2 TCP 서버 (port 9092) → 인메모리 DB "econdashboard"
 *   Batch 서버 → jdbc:h2:tcp://localhost:9092/mem:econdashboard 로 접속
 */
@Configuration
@Profile("dev")
class H2ServerConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun h2TcpServer(): Server {
        log.info("H2 TCP 서버 시작 (port 9092) — Batch 서버가 같은 DB에 접속할 수 있습니다")
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092")
    }
}
