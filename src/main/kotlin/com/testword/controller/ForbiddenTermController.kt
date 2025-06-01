package com.testword.controller

import com.testword.common.EmptyResponse
import com.testword.common.Response
import com.testword.controller.dto.CheckContentReq
import com.testword.controller.dto.ForbiddenTermCheckRes
import com.testword.controller.dto.RegisterForbiddenTermsReq
import com.testword.service.dto.ForbiddenTermCheckResult
import com.testword.service.forbidden_term.ForbiddenTermService
import com.testword.service.forbidden_term.ForbiddenTermServiceTreeImpl
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/terms")
@Import(ForbiddenTermServiceTreeImpl::class)
class ForbiddenTermController(
    private val forbiddenTermService: ForbiddenTermService,
) {

    /**
     * 입력된 콘텐츠에서 금칙어를 검사한다.
     */
    @PostMapping("/check")
    fun checkForbiddenTerms(
        @RequestBody request: CheckContentReq,
    ): Response<ForbiddenTermCheckRes> {
        val result: ForbiddenTermCheckResult = runBlocking {
            forbiddenTermService.checkForbiddenTerms(
                contentId = request.contentId,
                earlyReturn = request.earlyReturn
            )
        }
        return Response(
            ForbiddenTermCheckRes(
                hasForbiddenTerm = result.hasForbiddenTerm,
                terms = result.matchedTerms,
                termCounts = result.termCounts
            )
        )
    }

    /**
     * 새로운 금칙어 리스트를 등록한다.
     */
    @PostMapping("/register")
    fun registerForbiddenTerms(
        @RequestBody request: RegisterForbiddenTermsReq,
    ): EmptyResponse {
        forbiddenTermService.registerForbiddenTerms(request.terms)
        return EmptyResponse()
    }

    /**
     * 금칙어 리스트를 재생성한다.
     */
    @PostMapping("/refresh")
    fun refreshForbiddenTerms(): EmptyResponse {
        forbiddenTermService.reload()
        return EmptyResponse()
    }
}
