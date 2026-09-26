package com.sprint.mission.discodeit.security;


import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.security.role.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    public static final String CLAIM_ROLE = "role";

    private final JwtProperties properties;
    // 발급 시같을 체크하기 위한 시간 등록 객체
    private final Clock clock;
    // 시그니쳐 발급용 객체.
    private final SecretKey secretKey;

    public JwtTokenProvider(
            JwtProperties properties,
            Clock clock
    ){
        this.properties = properties;
        this.clock = clock;
        // 설정에 작성한 salt 를 해시 함수 암호화
        this.secretKey = Keys.hmacShaKeyFor(
                properties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }


    // 토큰 발급, 갱신, 유효성 검사.

    /**
     * 서버의 설정을 이용해서 jwt 토큰을 발급하는 매서드.
     * @param username
     * @param role
     * @return
     */
    public String createAccessToken(String username, Role role){

        Instant now = clock.instant();
        Instant expiry = now.plus(properties.getAccessTokenValidate());

        // Jwt 클래스 (JJWT 라이브러리 -> Json 구조 문자열 반환)
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROLE,role.name())

                // 발급자 및 발급 시간, 만료 시간.
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))

                // 시그니쳐 - 시크릿 키를 대칭키 256비트 암호화 해시.
                .signWith(secretKey,Jwts.SIG.HS256)
                .compact();

    }

    /**
     * 토큰을 현재 서버 설정으로 확인하고, 페이로드를 반환한다.
     * @param token JWT
     * @return Claims 라는 페이로드 객체.
     * 토큰의 기한에 따라
     * - ExpiredJwtException : 토큰 만료 에러
     * - JwtException | IllegalArgumentException
     * 을 반환한다.
     */

    public Claims parseClaims(String token){
        return Jwts.parser()
                // 서버 개인 salt 로 시그니쳐 파싱
                .verifyWith(secretKey)
                // 토큰 발급자 검사
                .requireIssuer(properties.getIssuer())
                // 현재 시간 파서에 세팅 -> 만료 시간 검사 기준.
                .clock(() -> Date.from(clock.instant()))
                .build()

                // 제작된 파서 객체로 실제 토큰 파싱(검사)
                .parseEncryptedClaims(token)
                .getPayload();
    }

    /*
    별도 페이로드 정보 추출 매서드.
     */


}
