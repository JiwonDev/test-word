package com.testword

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("com.testword.service")
class MyApplication

fun main(args: Array<String>) {
    runApplication<MyApplication>(*args)
}