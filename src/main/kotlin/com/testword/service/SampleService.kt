package com.testword.service

import com.testword.repository.SampleRepository

class SampleService(
    private val sampleRepository: SampleRepository,
) {
    fun hello(): String {
        return "Hello, World! ${sampleRepository.findByName("test")?.name}"
    }
}