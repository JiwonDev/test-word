package com.testword.service

import com.testword.entity.ForbiddenTerm
import com.testword.repository.ForbiddenTermRepository
import com.testword.service.dto.ForbiddenTermCheckResult
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class ForbiddenTermService(
    private val repo: ForbiddenTermRepository,
    private val checker: ForbiddenTermChecker,
) {
    private val log = KotlinLogging.logger {}

    private fun reloadForbiddenTerms() {
        val terms = repo.findAllTerms()
            .map(String::lowercase)
            .toSet()

        checker.reloadTerms(terms)
        log.info { "Reloaded forbidden terms: \n$terms" }
    }

    @PostConstruct
    fun load() {
        reloadForbiddenTerms()
    }

    /**
     * 새로운 금지어 등록 후 트리 리로드
     */
    fun registerForbiddenTerms(rawTokens: List<String>) {
        val inputTokens = rawTokens
            .map(String::lowercase)
            .filter { it.isNotBlank() }
            .toSet()

        if (inputTokens.isEmpty()) return

        val existing = repo.findAllTerms().toSet()
        val needInsert = inputTokens - existing
        if (needInsert.isEmpty()) return

        repo.saveAll(needInsert.map { ForbiddenTerm(term = it) })
        reloadForbiddenTerms()
    }

    /**
     * 텍스트에서 금지어 검사
     */
    fun checkForbiddenTerms(
        content: String,
        earlyReturn: Boolean = false,
    ): ForbiddenTermCheckResult {
        val matched = checker.findForbiddenTerms(content, earlyReturn)
        return ForbiddenTermCheckResult(
            hasForbiddenTerm = matched.isNotEmpty(),
            matchedTerms = matched
        )
    }
}