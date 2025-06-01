package com.testword.controller.dto

/**
 * 금칙어 검사 결과 응답 DTO
 */
data class ForbiddenTermCheckRes(
    val hasForbiddenTerm: Boolean,
    val terms: List<String>,
    val termCounts: Map<String, Int>,
)