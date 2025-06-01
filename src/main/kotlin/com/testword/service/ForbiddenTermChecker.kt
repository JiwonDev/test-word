package com.testword.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Weigher
import com.ibm.icu.text.Normalizer2
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openkoreantext.processor.OpenKoreanTextProcessorJava
import org.springframework.stereotype.Component

@Component
class ForbiddenTermChecker {

    private val log = KotlinLogging.logger {}

    companion object {
        private val CLEAN_TEXT_REGEX = Regex("[^\\p{L}\\p{N}]+")
        private val meaningfulMorphemes = setOf("Noun", "Verb", "Adjective", "Adverb", "Exclamation", "Determiner")
    }

    private var forbiddenTermTree = AhoCorasickTree()

    private val cache = Caffeine.newBuilder()
        .maximumWeight(100_000_000)
        .weigher(Weigher<String, List<String>> { key, value ->
            val keyBytes = key.toByteArray(Charsets.UTF_8).size
            val valueBytes = value.sumOf { it.toByteArray(Charsets.UTF_8).size }
            keyBytes + valueBytes
        })
        .build<String, List<String>>()

    fun getAllTerms(): Set<String> = forbiddenTermTree.getPatterns().toSet()

    fun isTermListEmpty(): Boolean = forbiddenTermTree.isEmpty()

    fun reloadTerms(terms: Collection<String>) {
        val normalizer = Normalizer2.getNFCInstance()

        val uniqueTerms = terms
            .map { normalizer.normalize(it).lowercase() }
            .filter { it.isNotBlank() }
            .toSet()

        forbiddenTermTree = AhoCorasickTree().apply {
            uniqueTerms.forEach { insert(it) }
            build()
        }

        cache.invalidateAll()
        log.info { "현재 트리에 등록된 금칙어: ${forbiddenTermTree.getPatterns().count()}" }
    }

    fun findForbiddenTerms(content: CharSequence, earlyReturn: Boolean = false): List<String> {
        if (isTermListEmpty())
            throw IllegalStateException("ForbiddenTermChecker: Tree is not initialized. Call reloadTerms() first.")

        val rawText = content.toString()
        val cleanedText = CLEAN_TEXT_REGEX.replace(rawText, "").lowercase()

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

    private fun searchForbiddenTerm(content: String, earlyReturn: Boolean = false): List<String> {
        val tokens = extractMeaningfulTokens(content)

        val individualMatches = mutableSetOf<String>()
        val maxCombinationLength = 8

        for (token in tokens) {
            val matches = forbiddenTermTree.search(token)
            if (matches.isNotEmpty()) {
                individualMatches += matches
                if (earlyReturn) return matches.toList()
            }
        }

        for (start in tokens.indices) {
            for (end in start + 1..minOf(start + maxCombinationLength, tokens.size)) {
                val candidate = tokens.subList(start, end).joinToString("")
                val matches = forbiddenTermTree.search(candidate)
                if (matches.isNotEmpty()) {
                    individualMatches += matches
                    if (earlyReturn) return matches.toList()
                }
            }
        }

        val joinedText = tokens.joinToString("")
        val joinedTextMatches = forbiddenTermTree.search(joinedText)

        val allMatches = (joinedTextMatches + individualMatches).toSet()

        log.info {
            "tokens=${tokens.joinToString()}, joinedText=$joinedText, " +
                    "joinedTextMatches=${joinedTextMatches.joinToString()}, individualMatches=${individualMatches.joinToString()}"
        }

        return allMatches.toList()
    }

    fun findForbiddenTermWithCounts(content: CharSequence): Map<String, Int> {
        if (isTermListEmpty())
            throw IllegalStateException("ForbiddenTermChecker: Tree is not initialized. Call reloadTerms() first.")

        val tokens = extractMeaningfulTokens(content.toString())
        val matchCounts = mutableMapOf<String, Int>()
        val maxCombinationLength = 8

        for (token in tokens) {
            val matches = forbiddenTermTree.search(token)
            matches.forEach { match -> matchCounts[match] = matchCounts.getOrDefault(match, 0) + 1 }
        }

        for (start in tokens.indices) {
            for (end in start + 1..minOf(start + maxCombinationLength, tokens.size)) {
                val candidate = tokens.subList(start, end).joinToString("")
                val matches = forbiddenTermTree.search(candidate)
                matches.forEach { match -> matchCounts[match] = matchCounts.getOrDefault(match, 0) + 1 }
            }
        }

        val joinedText = tokens.joinToString("")
        val joinedMatches = forbiddenTermTree.search(joinedText)
        joinedMatches.forEach { match -> matchCounts[match] = matchCounts.getOrDefault(match, 0) + 1 }

        return matchCounts
    }

    /**
     * 텍스트 → 정규화 + 형태소 분석 + 의미 있는 토큰 추출
     */
    private fun extractMeaningfulTokens(text: String): List<String> {
        val normalized = Normalizer2.getNFCInstance().normalize(text)
        val tokens = buildList {
            val scalaTokens = OpenKoreanTextProcessorJava.tokenize(normalized).iterator()
            while (scalaTokens.hasNext()) {
                add(scalaTokens.next())
            }
        }

        return tokens
            .filter { it.pos().toString() in meaningfulMorphemes }
            .map { it.text().lowercase() }
    }
}
