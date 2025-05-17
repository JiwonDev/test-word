package com.testword

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class TestWordApplication

fun main(args: Array<String>) {
    val log = KotlinLogging.logger {}
    log.info { "Starting TestWordApplication\nGrafana http://localhost:3000/" }
    runApplication<TestWordApplication>(*args)
}