package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public class CsrfTokenHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler(); // SPA(React) 헤더용
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler(); // 기본 폼 전송용


    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        // SPA 방식은 헤더에 raw 토큰을 저장한다.
        // 때문에 요청 헤더에 실제 토큰값이 있는지 확인한다.
        // 헤더 네임은 sprint security 에서 지정한 헤더이름. 보통은 "_crsf"?
        String tokenValue = request.getHeader(csrfToken.getHeaderName());

        // 헤더에 값이 있다면, plain 방식 토큰파싱
        // 없다면 xor 방식 토큰파싱
        return (StringUtils.hasText(tokenValue) ? this.plain : this.xor)
                .resolveCsrfTokenValue(request,csrfToken);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        this.xor.handle(request, response, csrfToken);
        // deferred 토큰을 '지금' 로드시켜 CookieCsrfTokenRepository 가 쿠키(XSRF-TOKEN)를 세팅하도록 강제한다.
        csrfToken.get();
    }
}
