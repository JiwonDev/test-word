package com.testword.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Weigher
import com.ibm.icu.text.Normalizer2
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openkoreantext.processor.OpenKoreanTextProcessorJava
import org.openkoreantext.processor.tokenizer.KoreanTokenizer
import org.springframework.stereotype.Component

@Component
class ForbiddenTermChecker {

    private val log = KotlinLogging.logger {}

    companion object {
        // 특수문자 제거용 정규식
        private val CLEAN_TEXT_REGEX = Regex("[^\\p{L}\\p{N}]+")
    }

    private var forbiddenTermTree = AhoCorasickTree()

    private val cache = Caffeine.newBuilder()
        .maximumWeight(100_000_000) // 100MB
        .weigher(Weigher<String, List<String>> { key, value ->
            // 가중치 기반 LRU (Least Recently Used)
            val keyBytes = key.toByteArray(Charsets.UTF_8).size
            val valueBytes = value.sumOf { it.toByteArray(Charsets.UTF_8).size }
            keyBytes + valueBytes
        })
        .build<String, List<String>>()

    fun getAllTerms(): Set<String> = forbiddenTermTree.getPatterns().toSet()

    fun isTermListEmpty(): Boolean = forbiddenTermTree.isEmpty()

    /**
     * 금지어 목록을 다시 로드하고 트리를 재구성함
     */
    fun reloadTerms(terms: Collection<String>) {
        val normalizer = Normalizer2.getNFCInstance()

        val uniqueTerms: Set<String> = terms
            .map { normalizer.normalize(it).lowercase() }
            .filter { it.isNotBlank() }
            .toSet()

        forbiddenTermTree = AhoCorasickTree().apply {
            uniqueTerms.forEach { insert(it) }
            build()
        }

        cache.invalidateAll()
        log.info { "현재 트리에 등록된 금칙어: ${forbiddenTermTree.getPatterns()}" }
    }

    /**
     * 금지어 검색
     * @param content     입력 텍스트
     * @param earlyReturn 첫 매칭만 필요 시 true
     */
    fun findForbiddenTerms(
        content: CharSequence,
        earlyReturn: Boolean = false,
    ): List<String> {
        if (isTermListEmpty())
            throw IllegalStateException("ForbiddenTermChecker: Tree is not initialized. Call reloadTerms() first.")

        val rawText = content.toString()
        val cleanedText = CLEAN_TEXT_REGEX.replace(rawText, "").lowercase()

        // 캐시 확인
        cache.getIfPresent(cleanedText)?.let { return it }

        val matches = searchForbiddenTerm(rawText, earlyReturn)
        if (matches.isNotEmpty()) {
            cache.put(cleanedText, matches)
            return matches
        }

        val fallbackMatched = forbiddenTermTree.search(cleanedText)
        if (fallbackMatched.isNotEmpty()) {
            cache.put(cleanedText, fallbackMatched.toList())
            return fallbackMatched.toList()
        }

        return emptyList()
    }

    /**
     * 금지어 검색 로직
     */
    private fun searchForbiddenTerm(
        content: String,
        earlyReturn: Boolean = false,
    ): List<String> {
        // 1. 입력 텍스트 정규화 및 형태소 분석
        val normalized = Normalizer2.getNFCInstance().normalize(content)
        val tokens: List<KoreanTokenizer.KoreanToken> = buildList {
            val scalaTokens = OpenKoreanTextProcessorJava.tokenize(normalized).iterator()
            while (scalaTokens.hasNext()) add(scalaTokens.next())
        }

        // 2. 의미 있는 형태소만 추출 (나qqdqj쁜 욕1설 -> 나 쁜 욕 설)
        val meaningfulMorphemes = setOf("Noun", "Verb", "Adjective", "Adverb", "Exclamation", "Determiner")
        val meaningfulTokens = tokens
            .filter { it.pos().toString() in meaningfulMorphemes }
            .map { it.text().lowercase() }


        val individualMatches = mutableSetOf<String>()
        val maxCombinationLength = 8

        // 2-1 단일 형태소도 검사 ( 나, 쁜, 욕, 설)
        for (token in meaningfulTokens) {
            val matches = forbiddenTermTree.search(token)
            if (matches.isNotEmpty()) {
                individualMatches += matches
                if (earlyReturn) return matches.toList()
            }
        }

        // 2-2 형태소 조합 후 검사 ("나 쁜 욕 설" -> 나쁜, 나쁜욕, 나쁜욕설)
        for (start in meaningfulTokens.indices) {
            for (end in start + 1..minOf(start + maxCombinationLength, meaningfulTokens.size)) {
                val candidate = meaningfulTokens.subList(start, end).joinToString("")
                val matches = forbiddenTermTree.search(candidate)

                if (matches.isNotEmpty()) {
                    individualMatches += matches
                    if (earlyReturn) return matches.toList()
                }
            }
        }

        // 3. 전체 형태소를 한 번에 붙인 문자열도 검사
        val joinedText = meaningfulTokens.joinToString("")
        val joinedTextMatches = forbiddenTermTree.search(joinedText)

        // 결과 통합
        val allMatches = (joinedTextMatches + individualMatches).toSet()

        log.info {
            "tokens=${meaningfulTokens.joinToString()}, " +
                    "joinedText=$joinedText, " +
                    "joinedTextMatches=${joinedTextMatches.joinToString()}, " +
                    "individualMatches=${individualMatches.joinToString()}"
        }

        return allMatches.toList()
    }
}
