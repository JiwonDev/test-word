package com.testword.service.dto

data class ForbiddenTermCheckResult(
    val hasForbiddenTerm: Boolean,
    val matchedTerms: List<String>,
)
