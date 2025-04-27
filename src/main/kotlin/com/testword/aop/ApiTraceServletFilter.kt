package com.testword.aop

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.concurrent.TimeUnit

class ApiTraceServletFilter(
    private val meterRegistry: MeterRegistry,
) : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    companion object {
        private const val ACTUATOR_URL = "/actuator"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val wrappedRequest = getContentCachingRequestWrapper(request)
        val wrappedResponse = getContentCachingResponseWrapper(response)
        val startTime = System.currentTimeMillis()
        var exception: Throwable? = null

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } catch (ex: Throwable) {
            exception = ex
            throw ex
        } finally {
            val duration = System.currentTimeMillis() - startTime
            val requestUri = wrappedRequest.requestURI

            if (!requestUri.startsWith(ACTUATOR_URL)) {
                logAndRecordMetrics(
                    request = wrappedRequest,
                    response = wrappedResponse,
                    duration = duration,
                    exception = exception
                )
            }

            wrappedResponse.copyBodyToResponse()
        }
    }

    private fun logAndRecordMetrics(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        duration: Long,
        exception: Throwable?,
    ) {
        val method = request.method
        val requestUri = request.requestURI
        val queryString = request.queryString?.let { "?$it" } ?: ""
        val status = response.status

        val requestBody = if (method == "POST" || method == "PUT") {
            val content = request.contentAsByteArray
            if (content.isNotEmpty()) String(content, Charsets.UTF_8) else ""
        } else ""

        val responseBody = runCatching {
            val content = response.contentAsByteArray
            if (content.isNotEmpty()) String(content, Charsets.UTF_8) else ""
        }.getOrElse { "" }


        val resultLabel = when (status) {
            in 200..299 -> "success"
            in 400..499 -> "client_error"
            else -> "server_error"
        }

        val fullLog =
            "[${this::class.simpleName}] $method $requestUri$queryString " +
                    "| Status=$status " +
                    "| Duration=${duration}ms" +
//                    "| RequestBody=$requestBody " +
//                    "| ResponseBody=$responseBody" +
                    (exception?.let { " | Exception=${it.javaClass.simpleName}" } ?: "")

        if (status < 400)
            log.info { fullLog }
        else
            log.error(exception) { fullLog }


        val commonTags = Tags.of(
            "method", method,
            "uri", request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String
                ?: request.requestURI,
            "status", status.toString(),
            "result", resultLabel
        )


        if (exception != null)
            meterRegistry
                .counter("api_trace_requests", commonTags.and("exception", exception.javaClass.simpleName))
                .increment()
        else
            meterRegistry
                .counter("api_trace_requests", commonTags)
                .increment()

        Timer.builder("api_trace_request_duration")
            .publishPercentileHistogram()
            .publishPercentiles(0.5, 0.95, 0.99, 0.999)
            .tags(commonTags)
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS)
    }

    private fun getContentCachingRequestWrapper(request: HttpServletRequest): ContentCachingRequestWrapper {
        return if (request is ContentCachingRequestWrapper) request else ContentCachingRequestWrapper(request)
    }

    private fun getContentCachingResponseWrapper(response: HttpServletResponse): ContentCachingResponseWrapper {
        return if (response is ContentCachingResponseWrapper) response else ContentCachingResponseWrapper(response)
    }
}
