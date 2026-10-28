package com.sprint.mission.discodeit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class RoleHierarchyConfig {

  // 관리자 > 채널 매니저 > 일반 사용자
  // 관리자 권한은 채널 매니저 권한과 일반 사용자 권한을 모두 포함하고,
  // 채널 매니저 권한은 일반 사용자 권한을 포함한다.
  @Bean
  public RoleHierarchy roleHierarchy() {
    String hierarchy = """
        ROLE_ADMIN > ROLE_CHANNEL_MANAGER
        ROLE_CHANNEL_MANAGER > ROLE_USER
        """;
    return RoleHierarchyImpl.fromHierarchy(hierarchy);
  }

  // Method Security에서 권한 계층을 적용하기 위해 MethodSecurityExpressionHandler를 설정
  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }
}
