package com.testword.service.forbidden_term

import com.testword.entity.ForbiddenTerm
import com.testword.repository.ForbiddenTermRepository
import com.testword.service.dto.ForbiddenTermCheckResult
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import java.util.*

class ForbiddenTermServiceDbImpl(
    private val repo: ForbiddenTermRepository,
) : ForbiddenTermService {

    private val log = KotlinLogging.logger {}

    @PostConstruct
    override fun reload() {
        // noop - DB에서 직접 검색하므로 별도 캐시 없음
    }

    override fun registerForbiddenTerms(rawTokens: List<String>) {
        val inputTokens = rawTokens
            .map { it.lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .toSet()

        if (inputTokens.isEmpty()) return

        val existing = repo.findAllTerms().map { it.lowercase(Locale.ROOT) }.toSet()
        val needInsert = inputTokens - existing

        if (needInsert.isNotEmpty()) {
            repo.saveAll(needInsert.map { ForbiddenTerm(term = it) })
        }
    }

    override fun checkForbiddenTerms(content: String, earlyReturn: Boolean): ForbiddenTermCheckResult {
        val lowered = content.lowercase(Locale.ROOT)
        val matched = repo.findMatchingTerms(lowered, earlyReturn)

        return ForbiddenTermCheckResult(matched.isNotEmpty(), matched)
    }
}
