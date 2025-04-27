package com.testword.controller

import com.testword.common.Response
import com.testword.service.SampleService
import jakarta.persistence.OptimisticLockException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sample")
class SampleController(
    private val sampleService: SampleService,
) {
    @GetMapping("/hello")
    fun hello(
        @RequestParam("name", required = false, defaultValue = "test") name: String,
    ): Response<String> {
        val result: String = sampleService.hello()
        return Response(result)
    }


    @PostMapping("/hello")
    fun hello(
        @RequestBody body: Map<String, String>,
    ): Response<String> {
        val result: String = sampleService.hello()
        return Response(result + "\n$body")
    }

    @PostMapping("/slow")
    fun slow(
        @RequestBody body: Map<String, String>,
    ): Response<String> {
        val result: String = sampleService.hello()
        Thread.sleep((200 + (0..1800).random()).toLong())
        return Response(result + "\n$body")
    }

    @PostMapping("/error")
    fun error(
        @RequestBody body: Map<String, String>,
    ): Response<String> {
        val result: String = sampleService.hello()
        Thread.sleep((200 + (0..800).random()).toLong())
        throw OptimisticLockException("error occurred")
    }
}