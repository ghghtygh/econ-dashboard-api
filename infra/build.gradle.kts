plugins {
    kotlin("plugin.jpa")
}

dependencies {
    api(project(":core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.rometools:rome:2.1.0")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")
}
