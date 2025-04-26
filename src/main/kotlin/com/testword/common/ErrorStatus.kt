package com.testword.common

import org.springframework.http.HttpStatus

enum class ErrorStatus {
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    TOO_MANY_REQUESTS,
    INTERNAL_SERVER_ERROR;

    fun toHttpStatus(): HttpStatus =
        when (this) {
            BAD_REQUEST -> HttpStatus.BAD_REQUEST
            UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
            FORBIDDEN -> HttpStatus.FORBIDDEN
            NOT_FOUND -> HttpStatus.NOT_FOUND
            TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS
            INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
        }

}