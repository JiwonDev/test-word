package com.testword.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@Configuration
@EnableJpaAuditing
class JpaAuditConfig {

    @Bean
    @ConditionalOnMissingBean(AuditorAware::class)
    fun auditorProvider(): AuditorAware<String> = AuditorAware { Optional.of("anonymous") }
}
