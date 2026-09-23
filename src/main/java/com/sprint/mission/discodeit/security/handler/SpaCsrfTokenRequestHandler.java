package com.sprint.mission.discodeit.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        /*
         * 응답 본문에 CSRF 토큰을 렌더링할 때 BREACH 공격을 방지하기 위해
         * 항상 XorCsrfTokenRequestAttributeHandler를 사용한다.
         */
        this.xor.handle(request, response, csrfToken);
        /*
         * 지연된 토큰을 로드하여 토큰값을 쿠키에 저장한다.
         */
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        /*
         * 요청 헤더가 있으면 CsrfTokenRequestAttributeHandler를 사용해 CSRF 토큰을 해석한다.
         * SPA가 쿠키에서 얻은 원본 CSRF 토큰값을 요청 헤더에 자동으로 포함하는 경우에 해당한다.
         *
         * 그 외의 경우(예: 요청 파라미터가 있는 경우)에는
         * XorCsrfTokenRequestAttributeHandler를 사용해 CSRF 토큰을 해석한다.
         * 서버 사이드 렌더링 폼이 _csrf 요청 파라미터를 hidden input으로 포함하는 경우에 해당한다.
         */
        return (StringUtils.hasText(headerValue) ? this.plain : this.xor).resolveCsrfTokenValue(request, csrfToken);
    }
}
