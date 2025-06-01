package com.testword.service.forbidden_term

import com.testword.service.dto.ForbiddenTermCheckResult

interface ForbiddenTermService {
    fun registerForbiddenTerms(rawTokens: List<String>)

    fun checkForbiddenTerms(contentId: Long, earlyReturn: Boolean = false): ForbiddenTermCheckResult

    fun reload()
}