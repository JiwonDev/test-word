package com.testword.controller.dto

/**
 * 금칙어 등록 요청 DTO
 */
data class RegisterForbiddenTermsReq(
    val terms: List<String>,
)