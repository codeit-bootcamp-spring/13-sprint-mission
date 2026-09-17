package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserOnlineMapper {

    private final SessionRegistry sessionRegistry;

    @Named("isOnline")
    public boolean isOnline(UUID userId) {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(DiscodeitUserDetails.class::isInstance)
                .map(DiscodeitUserDetails.class::cast)
                .filter(principal -> principal.getUserDto().id().equals(userId))
                .anyMatch(principal -> !sessionRegistry.getAllSessions(principal, false).isEmpty());
    }
}
