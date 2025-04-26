package com.testword.common


data class ErrorResponse(
    val code: Int,
    val message: String,
) {
    constructor(errorCode: ErrorCode) : this(errorCode.code, errorCode.message)
}
