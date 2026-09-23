package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userAuthorization")
@RequiredArgsConstructor
public class UserAuthorization {

    private final UserRepository userRepository;

    public boolean isOwner(
            UUID userId,
            Authentication authentication
    ) {

        /*
         * null에 대한 유효성 검사는 Service에서 처리한다.
         */
        if (userId == null) {
            return true;
        }

        /*
         * 존재하지 않는 사용자라면 권한 검사에서 403을 발생시키지 않는다.
         *
         * Service의 findUserById()까지 요청을 전달해서
         * 기존 UserNotFoundException -> 404 동작을 유지한다.
         */
        if (!userRepository.existsById(userId)) {
            return true;
        }

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        if (!(authentication.getPrincipal()
                instanceof DiscodeitUserDetails userDetails)) {

            return false;
        }

        UUID authenticatedUserId =
                userDetails
                        .getUserDto()
                        .getId();

        return userId.equals(
                authenticatedUserId
        );
    }
}