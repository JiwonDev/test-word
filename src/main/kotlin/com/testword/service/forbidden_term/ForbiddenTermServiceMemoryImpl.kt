package com.testword.service.forbidden_term

import com.testword.entity.ForbiddenTerm
import com.testword.repository.ForbiddenTermRepository
import com.testword.service.dto.ForbiddenTermCheckResult
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import java.util.*

class ForbiddenTermServiceMemoryImpl(
    private val repo: ForbiddenTermRepository,
) : ForbiddenTermService {

    private val log = KotlinLogging.logger {}

    private var memoryTerms: Set<String> = emptySet()

    @PostConstruct
    override fun reload() {
        memoryTerms = repo.findAllTerms()
            .map { it.lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .toSet()

        log.info { "MemoryService loaded ${memoryTerms.size} terms." }
    }

    override fun registerForbiddenTerms(rawTokens: List<String>) {
        val normalized = rawTokens
            .map { it.lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .toSet()

        if (normalized.isEmpty()) return

        val existing = memoryTerms
        val newTerms = normalized - existing
        if (newTerms.isEmpty()) return

        repo.saveAll(newTerms.map { ForbiddenTerm(term = it) })
        reload()
    }

    override fun checkForbiddenTerms(content: String, earlyReturn: Boolean): ForbiddenTermCheckResult {
        val lowered = content.lowercase(Locale.ROOT)

        val matches = buildList {
            for (term in memoryTerms) {
                if (lowered.contains(term)) {
                    add(term)
                    if (earlyReturn) break
                }
            }
        }

        return ForbiddenTermCheckResult(matches.isNotEmpty(), matches)
    }
}
