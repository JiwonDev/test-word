package com.testword

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

object TestWordApplicationDefault {
    val properties = mapOf(
        "spring.application.name" to "api",
        "spring.profiles.active" to "local",
        "logging.level.root" to "info"
    )
}

@SpringBootApplication
@ConfigurationPropertiesScan
class TestWordApplication : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder =
        builder
            .properties(TestWordApplicationDefault.properties)
            .sources(TestWordApplication::class.java)
}

fun main(args: Array<String>) {
    SpringApplicationBuilder()
        .properties(TestWordApplicationDefault.properties)
        .sources(TestWordApplication::class.java)
        .run(*args)
}
