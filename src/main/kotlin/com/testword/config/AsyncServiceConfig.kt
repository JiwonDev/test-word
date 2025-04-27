package com.testword.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.lang.reflect.Method
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncServiceConfig : AsyncConfigurer {

    override fun getAsyncExecutor(): Executor {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor()
        threadPoolTaskExecutor.corePoolSize = 10
        threadPoolTaskExecutor.queueCapacity = 0 // cached queue capacity
        threadPoolTaskExecutor.setThreadNamePrefix("thread-pool-task-")
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true)
        threadPoolTaskExecutor.setAwaitTerminationSeconds(10)
        threadPoolTaskExecutor.initialize()
        return threadPoolTaskExecutor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return CustomAsyncExceptionHandler()
    }

    private class CustomAsyncExceptionHandler : AsyncUncaughtExceptionHandler {
        private val logger = KotlinLogging.logger {}

        override fun handleUncaughtException(ex: Throwable, method: Method, vararg params: Any?) {
            logger.error(ex) { "Async method '${method.name}' failed with message: ${ex.message}" }
            params.forEach { param -> logger.info { "Parameter value: $param" } }
        }
    }
}