package com.testword.aop

import com.testword.common.ErrorCode
import com.testword.common.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.TypeMismatchException
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

private val log = KotlinLogging.logger { }

@RestControllerAdvice
class ControllerExceptionAdvice(
    private val messageSourceAccessor: MessageSourceAccessor,
) : ResponseEntityExceptionHandler() {

    private fun makeErrorMessage(e: Exception): String {
        if (e is IllegalArgumentException)
            return messageSourceAccessor.getMessage(e.javaClass.simpleName, "잘못된 요청입니다.")
        if (e is MethodArgumentTypeMismatchException)
            return "잘못된 요청입니다. (${e.name}=${e.value})"

        return messageSourceAccessor.getMessage(e.javaClass.simpleName, "서비스에 문제가 발생했습니다.")
    }

    @ExceptionHandler(Exception::class)
    fun unknownException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(ErrorCode.UNKNOWN_ERROR.status.toHttpStatus())
            .body(ErrorResponse(ErrorCode.ILLEGAL_REQUEST.code, makeErrorMessage(e)))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(ErrorCode.ILLEGAL_REQUEST.status.toHttpStatus())
            .body(ErrorResponse(ErrorCode.ILLEGAL_REQUEST.code, makeErrorMessage(e)))
    }

    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        return ResponseEntity.status(status).body(
            ErrorResponse(ErrorCode.ILLEGAL_REQUEST.code, makeErrorMessage(ex))
        )
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        return ResponseEntity.status(status).body(
            ErrorResponse(ErrorCode.ILLEGAL_REQUEST.code, "JSON parse error, Invalid Request Body")
        )
    }

    override fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ErrorCode.NO_HANDLER.code, makeErrorMessage(ex)))
    }

    override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        return ResponseEntity.status(status).body(
            ErrorResponse(ErrorCode.ILLEGAL_REQUEST.code, makeErrorMessage(ex))
        )
    }

    override fun handleExceptionInternal(
        ex: java.lang.Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        log.warn(ex) { "[handleExceptionInternal] $body" }
        return super.handleExceptionInternal(
            ex,
            ErrorResponse(statusCode.value(), makeErrorMessage(ex)),
            headers,
            statusCode,
            request
        )
    }
}
