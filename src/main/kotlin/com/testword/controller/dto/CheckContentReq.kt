package com.testword.controller.dto

/**
 * 콘텐츠 금칙어 검사용 요청 DTO
 */
data class CheckContentReq(
    val content: String,
    val earlyReturn: Boolean = true,
)