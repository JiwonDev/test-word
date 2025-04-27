package com.testword.common


import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class Response<T : Any>(data: T?, status: HttpStatus = HttpStatus.OK) :
    ResponseEntity<Any>(ResponseData(data), status) {

    class ResponseData<T>(val data: T?)
}
