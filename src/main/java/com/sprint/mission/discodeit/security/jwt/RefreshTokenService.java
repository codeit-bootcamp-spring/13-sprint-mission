package com.sprint.mission.discodeit.security.jwt;


import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final String SERVICE_NAME = "REFRESH";

    private final RefreshTokenRepository tokenRepository;
    private final UserRepository userRepository;

    private final JwtProperties properties;
    private final Clock clock;

    private final SecureRandom random = new SecureRandom();


    /**
     * Refresh 토큰을 저장소에 저장, 발급하는 매서드
     * @param userId 변경 가능.
     * @return
     */
    @Transactional
    public String grant(UUID userId){

        byte[] salt = new byte[32];
        random.nextBytes(salt);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(salt);

        Instant expire = clock.instant().plus(properties.getRefreshTokenValidity());

        RefreshToken newRefreshToken = new RefreshToken(hash(token),expire,user);

        tokenRepository.save(newRefreshToken);

        return token;
    }

    /**
     *
     * @param key
     * @return
     */
    @Transactional
    public void rotate(String key){
        /*
        로테이팅 시 가능한 이벤트 목록
        1. 알 수 없는 토큰
        2. 재사용 감지
        3. 토큰 삭제됨
        4. 만료됨
         */

        RefreshToken token = tokenRepository.findByHash(hash(key)).orElse(null);

        // 1.
        if (token == null) return;
        // 2.
        switch (token.getState()){
            case ROTATED -> rotated(token);
            case REVOKED -> revoked(token);
            default -> checkExpire(token);
        };
    }

    @Transactional
    public void revoke(String key){
            tokenRepository.findByHash(hash(key))
                    .ifPresent(RefreshToken::revoke);
    }

    @Transactional
    public void revokeAll(UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Integer killCount = tokenRepository.revokeAllByUser(user);
        log.info("[{}] - 토큰 전체 무효화. user = {}, {}개",SERVICE_NAME,user.getUsername(),killCount);
    }





    /*
    유틸리티 매서드
     */

    private String hash(String token){
        String ALGORITHM = "SHA-256";
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(ALGORITHM + "은 지원하지 않는 알고리즘 입니다.",e);
        }
    }


    private void revoked(RefreshToken token){
        User currnetUser = token.getUser();
        log.info("[{}] - 제거된 토큰 재 발급. user = {}",
                SERVICE_NAME,
                currnetUser.getUsername()
        );
    }

    private void rotated(RefreshToken token){
        // 토큰 탈취 의심.
        // 1. 현재 사용자 기준 모든 토큰 삭제.
        User currnetUser = token.getUser();

        Integer revokeCount = tokenRepository.revokeAllByUser(currnetUser);
        log.warn("[{}] - 재사용 감지됨. user = {}, 무효화 한 토큰 = {}",
                SERVICE_NAME,
                token.getUser().getUsername(),
                revokeCount
        );
    }

    private void expired(RefreshToken token){
        User currnetUser = token.getUser();
        log.info("[{}] - 만료된 토큰 재 발급. user = {}",
                SERVICE_NAME,
                currnetUser.getUsername()
        );
    }

    private void checkExpire(RefreshToken token){
        Instant now = clock.instant();
        if (token.getExpire().isBefore(now))
            expired(token);
        else
            token.revoke();
    }

}
