package com.sprint.mission.discodeit.security;


import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CertificationChecker {

    private final JwtRegistry jwtRegistry;

    // jwt 방식 로그인 체크 매서드
    public boolean online(UUID id){
        return jwtRegistry.hasActiveJwtInformationByUserId(id);
    }
}
