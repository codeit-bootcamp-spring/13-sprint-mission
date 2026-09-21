package com.sprint.mission.discodeit.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionManager {

    private final SessionRegistry sessionRegistry;

    public boolean isOnline(UUID userId) {
        return !getActiveSessions(userId).isEmpty();
    }

    public void invalidateSessions(UUID userId) {
        getActiveSessions(userId).forEach(SessionInformation::expireNow);
    }

    private List<SessionInformation> getActiveSessions(UUID userId) {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof DiscodeitUserDetails details
                        && details.getUserDto().id().equals(userId))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .toList();
    }
}
