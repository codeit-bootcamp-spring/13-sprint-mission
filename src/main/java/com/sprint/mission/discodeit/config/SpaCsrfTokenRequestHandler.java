package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/**
 * CSR + SPA 환경에 적합한 {@link CsrfTokenRequestHandler} 구현체.
 * <p>
 * Spring Security 공식 문서에서 권장하는 방식으로, 응답 시에는 BREACH 공격 방어를 위해
 * XOR 처리된 토큰을 사용하고, 요청 검증 시에는 헤더로 전달된 원본 토큰을 그대로 비교한다.
 */
public final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       Supplier<CsrfToken> csrfToken) {
        // 응답 본문에 렌더링될 토큰은 BREACH 방어를 위해 항상 XOR 처리.
        this.xor.handle(request, response, csrfToken);
        // 지연 로딩된 토큰을 실제로 로드시켜 쿠키에 기록되도록 한다.
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        return (StringUtils.hasText(headerValue) ? this.plain : this.xor)
                .resolveCsrfTokenValue(request, csrfToken);
    }
}