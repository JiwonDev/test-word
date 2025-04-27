package com.testword.service.dto

/**
 * 금칙어 매칭 결과 (term, 시작위치, 끝위치)
 */
data class MatchedForbiddenTerm(
    val term: String,
    val startIndex: Int,
    val endIndex: Int,
)