package com.testword

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class TestWordApplication

fun main(args: Array<String>) {
    runApplication<TestWordApplication>(*args)
}