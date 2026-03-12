plugins {
    kotlin("plugin.jpa")
}

dependencies {
    api(project(":core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")
}
