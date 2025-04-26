package com.testword.controller

import com.testword.common.Response
import com.testword.service.SampleService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class SampleController(
    private val sampleService: SampleService,
) {
    @RequestMapping("/hello")
    fun hello(
        @RequestParam("name", required = false, defaultValue = "test") name: String,
    ): Response<String> {
        val result: String = sampleService.hello()
        return Response(result)
    }
}