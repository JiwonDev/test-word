package com.testword.common


enum class ErrorCode(
    val code: Int,
    val status: ErrorStatus,
    var message: String,
) {

    ILLEGAL_REQUEST(1400, ErrorStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(1401, ErrorStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    FORBIDDEN(1402, ErrorStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(1403, ErrorStatus.NOT_FOUND, "요청한 자원을 찾을 수 없습니다."),
    NO_HANDLER(1404, ErrorStatus.NOT_FOUND, "해당 핸들러를 찾을 수 없습니다."),
    TOO_MANY_REQUESTS(1405, ErrorStatus.TOO_MANY_REQUESTS, "너무 많은 요청입니다."),
    INTERNAL_SERVER_ERROR(1500, ErrorStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    UNKNOWN_ERROR(9999, ErrorStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류입니다."),

}
