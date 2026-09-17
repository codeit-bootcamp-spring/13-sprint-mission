# Security Filter Chain

## 1. 왜 Filter에서 동작하는가

Spring Security의 인증·인가는 특정 Controller 하나의 비즈니스 로직이 아니라 여러 HTTP 요청에 공통적으로 적용되는 보안 관심사다.

따라서 Controller에 도달하기 전 Filter 영역에서 공통으로 처리할 수 있다.

```text
HTTP Request
→ Servlet Filter
→ Spring Security Filter Chain
→ DispatcherServlet
→ Controller
```

각 Controller에서 매번 "로그인했는가?", "권한이 있는가?"를 직접 반복해서 확인하는 구조를 피할 수 있다.

---

## 2. 로그인 요청과 일반 요청은 다르다

HTTP 요청이 들어온다고 해서 모든 요청에서 username/password 인증을 새로 수행하는 것은 아니다.

### Form Login 요청

```text
POST /api/auth/login
→ 로그인 인증 Filter가 처리
→ 인증 전 Authentication
→ AuthenticationManager
→ Provider
→ 실제 인증
```

### 로그인 이후 일반 요청

```text
API 요청
→ 기존 Session에서 SecurityContext 복원
→ 기존 Authentication 사용
→ 인가
→ Controller
```

### 익명 사용자의 공개 요청

```text
GET /public
→ 정상 로그인 Authentication 없음
→ 익명 인증 기능이 활성화되어 있다면 AnonymousAuthenticationToken 사용 가능
→ permitAll
→ Controller
```

---

## 3. 현재 설정의 Filter 실행 순서

Spring Security는 Filter마다 고정된 등록 순서를 가지고 있고, 그중 현재 설정에서 활성화된 Filter만 실제 `SecurityFilterChain`에 들어간다.

현재 프로젝트는 Form Login, CSRF, Logout, 동시 Session 제한, Remember-Me와 요청 인가를 활성화한다. 이 설정을 기준으로 이해해야 할 실행 순서는 다음과 같다.

```text
1. DisableEncodeUrlFilter
2. WebAsyncManagerIntegrationFilter
3. SecurityContextHolderFilter
4. HeaderWriterFilter
5. CsrfFilter
6. LogoutFilter
7. UsernamePasswordAuthenticationFilter
8. DefaultLoginPageGeneratingFilter
9. DefaultLogoutPageGeneratingFilter
10. ConcurrentSessionFilter
11. RequestCacheAwareFilter
12. SecurityContextHolderAwareRequestFilter
13. RememberMeAuthenticationFilter
14. AnonymousAuthenticationFilter
15. SessionManagementFilter
16. ExceptionTranslationFilter
17. AuthorizationFilter
```

HTTP Basic, Bearer Token, OAuth2 Login, CORS 등을 설정하면 해당 기능의 Filter가 Spring Security의 정해진 위치에 추가된다. 반대로 설정하지 않은 Filter는 실행되지 않는다.

프레임워크 버전이나 설정 변경 뒤 실제 구성된 목록을 확인할 때는 다음 로그를 기준으로 검증한다.

```yaml
logging:
  level:
    org.springframework.security.web.FilterChainProxy: DEBUG
```

Filter 순서를 외울 때는 이름만 나열하기보다 현재 요청에서 실제로 필요한 관계를 함께 본다.

### 로그인 요청

```text
CsrfFilter
→ UsernamePasswordAuthenticationFilter
→ AuthenticationManager
→ 인증 성공/실패 Handler
```

CSRF Token이 필요한 POST 로그인 요청에서 Token이 없거나 올바르지 않으면 `CsrfFilter`에서 먼저 403이 발생하므로 username/password 인증까지 도달하지 않는다.

### 세션 사용자의 일반 요청

```text
SecurityContextHolderFilter
→ Session에서 기존 SecurityContext 사용
→ RememberMeAuthenticationFilter
→ AnonymousAuthenticationFilter
→ ExceptionTranslationFilter
→ AuthorizationFilter
→ Controller
```

### 예외 변환 관계

`ExceptionTranslationFilter`는 비밀번호를 비교하거나 권한을 직접 계산하지 않는다. 뒤에서 발생한 `AuthenticationException` 또는 `AccessDeniedException`을 `AuthenticationEntryPoint`나 `AccessDeniedHandler`에 연결한다.

```text
ExceptionTranslationFilter
  └─ 다음 Filter/Servlet 실행
       └─ AuthorizationFilter 또는 Method Security에서 예외
  ← 예외를 받아 401/403 처리와 연결
```

`ExceptionTranslationFilter`가 `AuthorizationFilter`보다 앞에 있는 이유는 뒤쪽 호출을 감싸고, 되돌아오는 예외를 처리해야 하기 때문이다.

---

## 4. permitAll은 Filter Chain 제외가 아니다

현재 `SecurityConfig`는 로그인, 로그아웃, 회원가입, 정적 리소스 등에 `permitAll()`을 설정한다.

```text
permitAll
→ Security Filter Chain은 통과
→ 인가 단계에서 인증 여부와 관계없이 접근 허용
```

반대로 어떤 요청이 아예 어떤 `SecurityFilterChain`에도 매칭되지 않으면 Spring Security의 보호를 받지 않을 수 있다.

공개 API라고 해서 곧바로 Security Filter Chain 전체에서 제외하는 것과 `permitAll()`은 같은 의미가 아니다.

---

## 5. requestMatchers

현재 프로젝트처럼 하나의 `SecurityFilterChain` 안에서 `requestMatchers`를 사용하면 요청 경로별 인가 정책을 나눌 수 있다.

```java
.requestMatchers("/api/auth/login").permitAll()
.requestMatchers("/actuator", "/actuator/**").hasRole("ADMIN")
.anyRequest().authenticated()
```

개념적으로는 다음 역할이다.

```text
이미 선택된 SecurityFilterChain 안에서
→ 이 요청에 어떤 인가 규칙을 적용할 것인가?
```

더 구체적인 규칙을 앞에 두고 넓은 규칙을 뒤에 두어 의도하지 않은 선행 매칭을 피한다.

---

## 6. securityMatcher와 여러 SecurityFilterChain

현재 프로젝트는 하나의 `SecurityFilterChain`을 사용하지만, 여러 체인을 구성한다면 `securityMatcher`는 **해당 SecurityFilterChain 자체가 어떤 요청을 담당할지** 결정한다.

```text
securityMatcher
→ 어떤 SecurityFilterChain을 사용할 것인가?

requestMatchers
→ 선택된 SecurityFilterChain 안에서 어떤 인가 규칙을 사용할 것인가?
```

여러 `SecurityFilterChain`이 있다면 `@Order` 순서대로 확인하고 처음 매칭된 체인 하나를 사용한다.

예를 들어:

```text
@Order(1) /api/**
@Order(2) /**
```

`/api/users`는 첫 번째 체인에 매칭되므로 두 번째 체인까지 모두 거치는 것이 아니다.

기본 체인이 없이 어떤 `securityMatcher`에도 해당하지 않는 URL이 생기면 Spring Security 보호에서 빠질 수 있으므로 구성 시 주의해야 한다.

---

## 7. deny-by-default

보안 기본값은 필요한 것만 명시적으로 허용하는 방식이 안전하다.

```text
새 API 추가
→ 별도 인가 규칙 누락

기본 permitAll
→ 의도치 않게 외부 공개될 수 있음

기본 denyAll
→ 명시적으로 허용하기 전까지 차단
```

현재 프로젝트는 공개 경로를 먼저 지정하고 나머지 요청을 `.anyRequest().authenticated()`로 보호한다.

---

## 8. 현재 프로젝트의 흐름

현재 `SecurityConfig`를 단순화하면 다음과 같다.

```text
공개
- /api/auth/csrf-token
- /api/auth/login
- /api/auth/logout
- POST /api/users
- 정적 리소스 / Swagger

ADMIN 필요
- /actuator/**
- /api/auth/role

나머지
- authenticated()
```

추가로 Method Security가 활성화되어 있으므로 일부 세부 권한은 Controller 진입 이후 Service 메서드 수준에서도 검사할 수 있다.


---

## 9. URL 인가와 Method Security의 실행 위치

현재 채널 생성 URL은 `.anyRequest().authenticated()`의 적용을 받는다. 따라서 로그인한 `ROLE_USER`는 Filter Chain의 URL 인가를 통과할 수 있다.

```text
POST /api/channels/public
→ Filter Chain의 authenticated() 통과
→ DispatcherServlet
→ ChannelController#createPublic
→ BasicChannelService 프록시
→ @PreAuthorize 검사
→ ROLE_USER + 공개 채널이므로 거부
→ Service 본문과 Repository 저장은 실행되지 않음
→ 403
```

반대로 같은 `ROLE_USER`가 비공개 채널 생성 Command를 전달하면 `#command.isPrivate()`가 참이므로 Service 본문이 실행된다.

따라서 “Security에서 403이 발생했다”는 결과만으로 Controller 이전에서 차단됐다고 단정하지 않는다. URL 규칙인지, Method Security 규칙인지 먼저 구분한다.
