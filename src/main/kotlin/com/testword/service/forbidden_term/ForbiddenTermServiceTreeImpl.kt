package com.testword.service.forbidden_term

import com.testword.entity.ForbiddenTerm
import com.testword.repository.ForbiddenTermRepository
import com.testword.service.ForbiddenTermChecker
import com.testword.service.dto.ForbiddenTermCheckResult
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import java.util.*

class ForbiddenTermServiceTreeImpl(
    private val repo: ForbiddenTermRepository,
    private val checker: ForbiddenTermChecker,
) : ForbiddenTermService {
    private val log = KotlinLogging.logger {}

    private fun reloadForbiddenTerms() {
        val terms = repo.findAllTerms()
            .map { it.lowercase(Locale.ROOT) }
            .toSet()

        checker.reloadTerms(terms)
    }

    @PostConstruct
    override fun reload() {
        reloadForbiddenTerms()
    }

    /**
     * 새로운 금지어 등록 후 트리 리로드
     */

    override fun registerForbiddenTerms(rawTokens: List<String>) {
        val inputTokens = rawTokens
            .map { it.lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .toSet()

        if (inputTokens.isEmpty()) return

        val existing = repo.findAllTerms()
            .map { it.lowercase(Locale.ROOT) }
            .toSet()

        val needInsert = inputTokens - existing
        if (needInsert.isEmpty()) return

        repo.saveAll(needInsert.map { ForbiddenTerm(term = it) })
        reloadForbiddenTerms()
    }

    /**
     * 텍스트에서 금지어 검사
     */
    override fun checkForbiddenTerms(
        content: String,
        earlyReturn: Boolean,
    ): ForbiddenTermCheckResult {
        val matched = checker.findForbiddenTerms(content, earlyReturn)
        return ForbiddenTermCheckResult(matched.isNotEmpty(), matched)
    }
}
