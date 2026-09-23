# 인가와 예외 처리

## 1. 인증과 인가

가장 먼저 두 개념을 분리한다.

```text
Authentication
→ 사용자가 누구인가?

Authorization
→ 인증된 사용자가 무엇을 할 수 있는가?
```

로그인은 성공했더라도 특정 API에 접근할 권한이 없다면 인가에는 실패할 수 있다.

---

## 2. Authentication의 authorities

Spring Security는 인가를 수행할 때 `Authentication`의 `authorities`를 이용한다.

현재 프로젝트의 `DiscodeitUserDetails#getAuthorities()`는 Role 앞에 `ROLE_` 접두사를 붙인다.

```text
USER
→ ROLE_USER

CHANNEL_MANAGER
→ ROLE_CHANNEL_MANAGER

ADMIN
→ ROLE_ADMIN
```

### hasRole과 hasAuthority

```java
hasRole("ADMIN")
```

기본적인 Role 표현에서는 `ROLE_ADMIN`을 확인하는 방식으로 이해한다.

반면 다음은 문자열을 그대로 사용한다.

```java
hasAuthority("ROLE_ADMIN")
```

따라서 실제 `Authentication.authorities`에 어떤 문자열이 들어 있는지를 기준으로 설정해야 한다.

---

## 3. 현재 프로젝트의 Role Hierarchy

`SecurityConfig`에는 다음 계층이 있다.

```text
ROLE_ADMIN
    ↓
ROLE_CHANNEL_MANAGER
    ↓
ROLE_USER
```

따라서 ADMIN은 CHANNEL_MANAGER와 USER의 권한을 상속하고, CHANNEL_MANAGER는 USER의 권한을 상속한다.

이 동작은 `SecurityAuthorizationIntegrationTest`에서 실제로 검증한다.

---

## 3-1. 현재 프로젝트의 채널 생성 권한

채널 생성은 URL 단계에서 세부 Role을 판단하지 않는다. `SecurityConfig`의 `.anyRequest().authenticated()`로 로그인 여부를 먼저 확인하고, 실제 공개·비공개 생성 조건은 Service의 Method Security에서 판단한다.

```java
@PreAuthorize("hasRole('CHANNEL_MANAGER') or #command.isPrivate()")
public ChannelDto save(ChannelCreateCommand command)
```

| 사용자 | 요청 | 결과 |
| --- | --- | --- |
| `ROLE_USER` | 공개 채널 생성 | 403 |
| `ROLE_USER` | 비공개 채널 생성 | 성공 |
| `ROLE_CHANNEL_MANAGER` | 공개 채널 생성 | 성공 |
| `ROLE_ADMIN` | 공개 채널 생성 | Role Hierarchy로 성공 |

`ROLE_USER`의 공개 채널 생성 흐름은 다음과 같다.

```text
SecurityFilterChain
→ authenticated() 통과
→ Controller 도달
→ Service 프록시의 @PreAuthorize
→ 조건 불충족
→ Service 메서드 본문 실행 전 AccessDeniedException
→ AccessDeniedHandler
→ 403
```

따라서 모든 403을 “Controller 이전 차단”으로 설명하면 안 된다. URL 인가에서 발생한 403과 Method Security에서 발생한 403은 차단 위치가 다르다.

---

## 4. authenticated와 권한 검사의 차이

```java
.authenticated()
```

은 정상적으로 인증된 사용자임을 요구한다.

```java
.hasRole("ADMIN")
```

은 인증뿐 아니라 ADMIN 권한까지 요구한다.

예를 들어 인증된 사용자의 authorities가 비어 있다고 가정하면 다음처럼 볼 수 있다.

```text
/mypage → authenticated()
→ 인증 상태만 충족하면 접근 가능

/admin → hasRole("ADMIN")
→ ROLE_ADMIN이 없으면 접근 불가
```

---

## 5. 401과 403

### 401 Unauthorized

인증이 필요한데 정상적인 인증 상태가 없는 경우다.

```text
익명 사용자
→ 보호 API 요청
→ 인증 필요
→ 401
```

현재 프로젝트에서는 `AuthenticationEntryPoint`가 `AUTH_401` JSON 응답을 작성한다.

### 403 Forbidden

사용자는 인증되어 있지만 요청한 작업에 필요한 권한이 없는 경우다.

```text
ROLE_USER로 로그인
→ ADMIN API 요청
→ 인증 O
→ ADMIN 권한 X
→ 403
```

현재 프로젝트에서는 `AccessDeniedHandler`가 `AUTH_403` JSON 응답을 작성한다.

---

## 6. denyAll에서도 사용자 상태가 중요하다

`denyAll()`은 누구에게도 해당 요청을 허용하지 않는 인가 규칙이다.

다만 최종 응답은 현재 사용자의 인증 상태에 따라 달라질 수 있다.

```text
익명 사용자
→ 접근 거부
→ 먼저 인증이 필요하다고 판단
→ AuthenticationEntryPoint
→ 보통 401

인증된 사용자
→ 접근 거부
→ AccessDeniedHandler
→ 403
```

즉 `denyAll()` 자체를 단순히 "무조건 403"이라고 이해하면 안 된다.

---

## 7. ExceptionTranslationFilter의 역할

`ExceptionTranslationFilter`는 비밀번호를 비교하거나 권한을 직접 계산하는 Filter가 아니다.

인증·인가 처리 중 발생한 Spring Security 예외를 웹 응답 처리로 연결하는 역할로 이해한다.

```text
AuthenticationException
→ AuthenticationEntryPoint

AccessDeniedException
+ 익명/미인증 사용자
→ AuthenticationEntryPoint

AccessDeniedException
+ 인증된 사용자
→ AccessDeniedHandler
```

여기서 401과 403은 Java 예외 클래스의 이름이 아니라 최종 HTTP 응답 상태다.

---

## 8. Form Login 인증 실패와 구분

Form Login 요청 자체에서 username/password 인증이 실패한 경우에는 인증 Filter의 실패 처리 흐름이 동작하고 현재 프로젝트의 `LoginFailureHandler`가 응답을 담당한다.

따라서 다음을 구분한다.

```text
POST /api/auth/login 인증 실패
→ LoginFailureHandler

보호된 API에 인증 없이 접근
→ AuthenticationEntryPoint

인증은 됐지만 권한 부족
→ AccessDeniedHandler
```

세 가지를 모두 "401/403 Handler"라는 하나의 개념으로 섞지 않는다.

---

## 9. 권한 변경과 기존 세션

사용자가 로그인한 뒤 DB의 Role이 변경되어도 이미 만들어진 `Authentication.authorities`에는 이전 권한이 남아 있을 수 있다.

```text
로그인 시점
→ ROLE_ADMIN
→ Session의 Authentication에 ROLE_ADMIN 존재

DB에서 ADMIN 권한 회수
→ 기존 Session의 Authentication은 자동으로 최신화된다고 가정하면 안 됨
```

권한 회수를 즉시 반영해야 한다면 서비스 요구사항에 따라 다음을 비교할 수 있다.

- 사용자의 기존 Session 무효화 후 재로그인
- 보안상 중요한 요청에서 최신 권한 재확인
- 권한 Version 같은 별도 전략

일반적인 요청마다 항상 DB의 최신 권한을 다시 조회하면 즉시 반영은 쉽지만 모든 요청의 DB 의존성과 비용이 증가한다.

현재 프로젝트는 역할 변경 시 `UserRoleManager`가 `SessionRegistry`에서 대상 사용자의 활성 Session을 찾고 `expireNow()`를 호출하는 방식을 선택했다. 다른 사용자의 Session은 유지하며, 이 동작은 `DiscodeitApiIntegrationTest#updateUserRole_expiresOnlyTargetUserSession`에서 검증한다.

---

## 10. PreAuthorize와 실행 중 권한 변경

`@PreAuthorize`는 메서드 실행 전에 권한을 확인한다.

```text
T1: @PreAuthorize 통과
T2: 다른 관리자가 권한 회수
T3: 이미 통과한 메서드의 실제 변경 작업 진행
```

세션을 T2에서 만료하더라도 T1에서 이미 인가를 통과해 실행 중인 요청이 자동으로 중간 취소된다고 볼 수 없다.

정말로 권한 회수 즉시 중요한 쓰기 작업까지 차단해야 한다면 변경 직전에 최신 권한을 다시 확인하는 등 별도의 정합성 전략이 필요할 수 있다.

이 문제는 검사 시점과 사용 시점 사이에 상태가 달라지는 TOCTOU 문제로 볼 수 있다. 업무상 즉시 차단이 필요하다면 다음 후보를 비교한다.

- 쓰기 직전에 DB의 최신 권한을 다시 조회
- 권한 조건을 포함한 조건부 UPDATE를 실행하고 변경 행 수로 성공 여부 판단
- 대상 Row를 비관적 락으로 조회한 뒤 같은 Transaction 안에서 권한과 상태를 확인
- 이후 요청 차단을 위해 기존 Session 만료

어떤 방법을 선택해도 이미 실행을 시작한 Java 메서드가 자동으로 중간 취소되는 것은 아니다.

동일 Session의 동시 요청과 다중 인스턴스까지 포함한 상세 내용은 [08-session-consistency-and-scaling.md](08-session-consistency-and-scaling.md)에 정리한다.

`@PostAuthorize`는 이미 메서드가 실행된 이후 검사하므로 삭제 같은 파괴적인 쓰기 작업의 해결책으로 단순 적용하면 안 된다.
