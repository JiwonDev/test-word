package com.testword.config

import com.testword.aop.ApiTraceServletFilter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServletFilterConfig {

    @Bean
    fun apiTraceFilterRegistration(
        meterRegistry: MeterRegistry,
    ): FilterRegistrationBean<ApiTraceServletFilter> {
        val filter = ApiTraceServletFilter(meterRegistry)
        val registration = FilterRegistrationBean<ApiTraceServletFilter>()
        registration.filter = filter
        registration.addUrlPatterns("/*")
        registration.order = 1
        return registration
    }
}
