# Spring Security 학습 기록

이 디렉터리는 `정구영-sprint9`에서 실제로 적용한 Spring Security 코드와 구두 점검 과정에서 확인한 내용을 다시 설명할 수 있도록 정리한 문서다.

2026년 9월 16일 구두 학습에서 다룬 내용은 버리지 않고 모두 기록한다. 다만 실제 프로젝트를 설명하는 데 필요한 핵심 흐름과, 문제 상황을 가정해 확장한 Deep Dive를 구분한다.

단순히 클래스와 메서드 이름을 외우는 것보다 다음 흐름을 이해하는 것을 목표로 한다.

```text
HTTP 요청
→ 인증이 필요한가?
→ 사용자는 누구인가?
→ 어떤 권한을 가지고 있는가?
→ 요청한 자원에 접근할 수 있는가?
→ 실패했다면 401인가 403인가?
```

## 문서 구성

| 문서 | 내용 |
| --- | --- |
| [01-authentication-flow.md](01-authentication-flow.md) | Form Login 최초 인증, AuthenticationManager, Provider, UserDetailsService, PasswordEncoder |
| [02-security-context-and-session.md](02-security-context-and-session.md) | SecurityContext, SecurityContextHolder, Session, JSESSIONID, ThreadLocal, 로그아웃 |
| [03-authorization-and-exception.md](03-authorization-and-exception.md) | 인증/인가, authorities, role, 401/403, AuthenticationEntryPoint, AccessDeniedHandler |
| [04-security-filter-chain.md](04-security-filter-chain.md) | SecurityFilterChain, permitAll, requestMatchers, securityMatcher, 주요 Filter 관계 |
| [05-security-testing.md](05-security-testing.md) | Security 테스트 범위, Mock 사용자와 실제 인증 테스트의 차이 |
| [06-deep-dive-notes.md](06-deep-dive-notes.md) | Deep Dive 주제 안내와 현재 프로젝트 적용 여부 |
| [07-async-security-context.md](07-async-security-context.md) | 비동기 SecurityContext 전파와 권한 판단 시점 |
| [08-session-consistency-and-scaling.md](08-session-consistency-and-scaling.md) | 권한 회수·TOCTOU·동시 요청·분산 Session |
| [09-csrf-cors-xss.md](09-csrf-cors-xss.md) | CSRF·CORS·XSS와 Cookie 보안 |
| [10-jwt-and-refresh-token.md](10-jwt-and-refresh-token.md) | JWT, Refresh Token Rotation·동시성·응답 유실 |

## 현재 프로젝트와 연결되는 코드

현재 프로젝트에서는 다음 코드가 이 문서의 기준이 된다.

- `src/main/java/com/sprint/mission/discodeit/config/SecurityConfig.java`
  - Form Login: `/api/auth/login`
  - Logout: `/api/auth/logout`
  - 공개 경로와 보호 경로 설정
  - `AuthenticationEntryPoint`, `AccessDeniedHandler`
  - Session 동시 접속 제한
  - Role Hierarchy
- `src/main/java/com/sprint/mission/discodeit/security/DiscodeitUserDetailsService.java`
  - username을 기준으로 사용자 조회
- `src/main/java/com/sprint/mission/discodeit/security/DiscodeitUserDetails.java`
  - 사용자 정보와 `ROLE_` 권한을 Spring Security 형식으로 제공
- `src/main/java/com/sprint/mission/discodeit/config/PasswordEncoderConfig.java`
  - `BCryptPasswordEncoder` 사용
- `src/test/java/com/sprint/mission/discodeit/security/SecurityAuthorizationIntegrationTest.java`
  - 미인증 401, 권한 부족 403, 관리자 접근, Role Hierarchy 검증
- `src/test/java/com/sprint/mission/discodeit/integration/DiscodeitApiIntegrationTest.java`
  - 실제 username/password 로그인, Session 복원, 로그아웃, Remember-Me 검증
  - `ROLE_USER`의 비공개 채널 생성 성공과 ReadStatus 저장 검증
- `src/main/java/com/sprint/mission/discodeit/service/basic/BasicChannelService.java`
  - 공개 채널은 CHANNEL_MANAGER 이상, 비공개 채널은 인증된 USER도 생성 가능
- `src/main/java/com/sprint/mission/discodeit/service/basic/UserRoleManager.java`
  - 역할 변경 시 대상 사용자의 활성 Session 만료

## 현재 프로젝트의 채널 생성 권한

`BasicChannelService#save`의 실제 규칙은 다음과 같다.

```java
@PreAuthorize("hasRole('CHANNEL_MANAGER') or #command.isPrivate()")
```

| 요청 사용자 | 공개 채널 | 비공개 채널 |
| --- | --- | --- |
| `ROLE_USER` | 403 | 생성 가능 |
| `ROLE_CHANNEL_MANAGER` | 생성 가능 | 생성 가능 |
| `ROLE_ADMIN` | Role Hierarchy로 생성 가능 | 생성 가능 |

공개 채널 생성에서 `ROLE_USER`는 URL의 `authenticated()`까지는 통과한다. Controller가 Service를 호출하는 시점에 Method Security 프록시가 `@PreAuthorize`를 검사하고, 조건을 만족하지 못하면 Service 메서드 본문 실행 전에 403을 반환한다.

## 이번 학습에서 교정한 핵심 오해

### 인증 전 Authentication과 익명 Authentication은 다르다

Form Login을 시도할 때 만들어지는 인증 전 `Authentication`과, 로그인하지 않은 일반 사용자를 표현하는 `AnonymousAuthenticationToken`은 같은 개념이 아니다.

```text
로그인 요청
→ username/password를 담은 인증 전 Authentication

일반 익명 요청
→ 인증 정보가 없으면 AnonymousAuthenticationToken을 사용할 수 있음
```

### 세션 복원은 새로운 로그인이 아니다

한 번 로그인한 세션 사용자는 이후 요청마다 DB에서 사용자를 다시 조회하고 비밀번호를 다시 비교하지 않는다.

```text
최초 로그인
→ 실제 인증
→ SecurityContext 저장

이후 요청
→ JSESSIONID
→ 기존 Session 조회
→ SecurityContext 복원
→ 기존 Authentication 사용
```

### permitAll은 Security를 우회하는 설정이 아니다

`permitAll()`은 Security Filter Chain을 통과하되 해당 요청의 접근을 인증 여부와 관계없이 허용하는 인가 규칙이다.

### 401과 403은 다르다

```text
401 Unauthorized
→ 인증이 필요한데 정상적인 인증 상태가 없음

403 Forbidden
→ 인증은 되었지만 요청한 작업에 필요한 권한이 없음
```

## 핵심 학습과 Deep Dive의 구분

핵심 학습 범위는 다음 흐름이다.

```text
Form Login
→ AuthenticationManager / Provider
→ UserDetailsService / PasswordEncoder
→ SecurityContext / Session
→ Authorization
→ 401 / 403
→ URL 인가와 @PreAuthorize 구분
→ 실제 통합 테스트
```

구두 학습에서 시간을 들여 확인한 다음 내용도 각 주제 문서와 [Deep Dive 안내](06-deep-dive-notes.md)에 빠짐없이 기록한다.

- [여러 Provider의 `null`·예외 처리 순서](01-authentication-flow.md)
- [현재 Security Filter의 실행 순서](04-security-filter-chain.md)
- [비동기 SecurityContext 전파](07-async-security-context.md)
- [같은 Session의 동시 요청과 SecurityContext 공유](08-session-consistency-and-scaling.md)
- [권한 회수와 실행 중 요청의 TOCTOU](08-session-consistency-and-scaling.md)
- [CSRF·CORS·XSS의 관계](09-csrf-cors-xss.md)
- [Spring Session·Redis·스티키 세션](08-session-consistency-and-scaling.md)
- [Refresh Token Rotation, 동시성, 응답 유실](10-jwt-and-refresh-token.md)

이 항목들은 학습 기록으로 보존하되, 현재 프로젝트의 필수 구현과 추후 적용 후보를 섞지 않는다.
