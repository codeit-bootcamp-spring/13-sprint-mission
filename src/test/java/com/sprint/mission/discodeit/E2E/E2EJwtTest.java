package com.sprint.mission.discodeit.E2E;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.role.Role;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("E2E Jwt login Test")
@Slf4j
public class E2EJwtTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    JwtTokenProvider jwtTokenProvider;


    @Autowired
    EntityMapper entityMapper;

    @Autowired
    JwtRegistry jwtRegistry;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    /** Authorization: Bearer 헤더를 단 (본문 없는) 요청 엔티티. */
    private HttpEntity<Void> bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    /** GET 요청 + Bearer 토큰 → 응답. */
    private ResponseEntity<String> getWithToken(String url, String token) {
        return rest.exchange("http://localhost:" + port + url, HttpMethod.GET, bearer(token), String.class);
    }

    private User user(String name, String email, String password){
        return new User(
                name,
                email,
                passwordEncoder.encode(password),
                null,
                Role.USER
        );
    }

    @BeforeEach
    void setup(){
        userRepository.save(user("김숙희","ksk@email.com","password"));
        userRepository.save(user("최둘리","cdr@email.com","password"));
    }


    @Test
    @DisplayName("엑세스 토큰 작성 -> 정상 결과 반환")
    void accessibleTokenTest(){
        String token = jwtTokenProvider.createAccessToken("admin@admin.com", Role.ADMIN);

        User admin = userRepository.findByEmail("admin@admin.com").orElse(null);

        String refreshToken = jwtTokenProvider.createRefreshToken(admin.getId().toString());

        JwtInformation jwtInfo = new JwtInformation(
                entityMapper.toDto(admin,true),
                token,
                refreshToken
        );

        jwtRegistry.registerJwtInformation(jwtInfo);

        ResponseEntity<String> res = getWithToken("/api/users",token);

        log.debug("[E2E_TEST] 결과 반환 - {}",res.getBody());

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("admin@admin.com");
        assertThat(res.getBody()).contains("ADMIN");

    }

    @Test
    @DisplayName("토큰 없이 접근시, 403 반환 에러.")
    void invalidAccessReturnWithNoToken(){
        ResponseEntity<String> res = rest.getForEntity("/api/users",String.class);

        log.debug("[E2E_TEST] 토큰 없이 접근 테스트 결과 - {}", res.getBody());

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

}
