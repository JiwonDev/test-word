package com.testword.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class EmptyResponse(status: HttpStatus = HttpStatus.OK) :
    ResponseEntity<Any>(ResponseData(null), status) {

    class ResponseData(val data: String?)
}