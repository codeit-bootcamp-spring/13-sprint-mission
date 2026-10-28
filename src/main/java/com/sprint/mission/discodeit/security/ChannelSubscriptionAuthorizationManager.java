package com.sprint.mission.discodeit.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 구독 목적지에서 추출한 channelId와 인증된 사용자로 채널 접근을 검사한다.
 * 잘못된 UUID나 인증 정보는 거부하고, 채널 관리자 권한은 역할 계층을 반영한다.
 */
@Component
@RequiredArgsConstructor
public class ChannelSubscriptionAuthorizationManager implements AuthorizationManager<MessageAuthorizationContext<?>> {

    private final ChannelGuard channelGuard;
    private final RoleHierarchy roleHierarchy;

    @Override
    public @Nullable AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, MessageAuthorizationContext<?> context) {
        // 기존 AuthorizationManager 계약의 진입점을 유지하고 실제 정책은 authorize 한 곳에서 관리한다.
        return authorize(authenticationSupplier, context);
    }

    @Override
    public @Nullable AuthorizationDecision authorize(Supplier<Authentication> authenticationSupplier, MessageAuthorizationContext<?> context) {
        Authentication authentication = authenticationSupplier.get();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof  DiscodeitUserDetails userDetails)){
            return new AuthorizationDecision(false);
        }

        String channelIdValue = context.getVariables().get("channelId");

        if (channelIdValue == null){
            return new AuthorizationDecision(false);
        }

        UUID channelId;
        try{
            channelId = UUID.fromString(channelIdValue);
        }catch (IllegalArgumentException e){
            return new AuthorizationDecision(false);
        }

        boolean channelManager = roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities())
                .stream()
                .anyMatch(authority -> "ROLE_CHANNEL_MANAGER".equals(authority.getAuthority()));
        boolean allowed = channelManager || channelGuard.isAccess(channelId, userDetails.getUserDto().id());

        return new AuthorizationDecision(allowed);
    }
}
