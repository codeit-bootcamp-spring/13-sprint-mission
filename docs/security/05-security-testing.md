# Spring Security 테스트

## 1. 테스트의 목표부터 구분한다

Security 테스트를 작성할 때 가장 먼저 확인할 것은 "어떤 인증/인가 동작을 증명하려는가"다.

대표적인 최소 시나리오는 다음 세 가지다.

```text
1. 인증 없이 보호 API 호출
→ 401

2. 인증된 사용자로 보호 API 호출
→ 성공

3. 인증은 되었지만 권한 없이 관리자 API 호출
→ 403
```

각 테스트가 필요한 이유를 결과 코드 자체보다 먼저 설명할 수 있어야 한다.

---

## 2. 401 테스트가 확인하는 것

```text
인증되지 않은 요청
→ 보호 API
→ Controller에 도달하기 전에 Security에서 차단
→ 401
```

확인하려는 것은 단순히 응답 코드가 401이라는 사실만이 아니다.

- 해당 URL이 실제로 보호되고 있는가?
- 인증되지 않은 요청이 Controller 이전에 차단되는가?
- `AuthenticationEntryPoint`가 프로젝트 형식의 오류 응답을 만드는가?

현재 `SecurityAuthorizationIntegrationTest`의 `protectedApi_returnsUnauthorized_whenUnauthenticated`가 이 흐름을 검증한다.

---

## 3. 정상 인증 사용자 테스트

```text
인증된 사용자
→ 보호 API
→ Security 검증 통과
→ Controller
→ 정상 응답
```

이 테스트는 보호 규칙이 모든 사용자를 잘못 막고 있지는 않은지 확인한다.

---

## 4. 403 테스트는 차단 위치를 함께 확인한다

403은 인증된 사용자가 필요한 권한을 갖지 못했을 때 발생한다. 다만 어디에서 권한을 검사했는지에 따라 Controller 도달 여부가 달라진다.

### URL 인가에서 거부

```text
ROLE_USER
→ /actuator/health
→ AuthorizationFilter의 hasRole("ADMIN") 불충족
→ Controller 이전 차단
→ 403
```

### Service Method Security에서 거부

```text
ROLE_USER
→ POST /api/channels/public
→ URL의 authenticated() 통과
→ Controller 도달
→ Service 프록시의 @PreAuthorize 검사
→ Service 본문 실행 전 차단
→ 403
```

따라서 403 테스트에서는 상태 코드뿐 아니라 다음도 확인한다.

- URL 인가인지 Method Security 인가인지
- Service 메서드 본문이 실행됐는지
- 저장·삭제 같은 부수 효과가 발생하지 않았는지
- 프로젝트의 `AccessDeniedHandler`가 `AUTH_403` 응답을 만들었는지

현재 `SecurityAuthorizationIntegrationTest`는 다음을 검증한다.

| 시나리오 | 결과 |
| --- | --- |
| 미인증 사용자의 보호 API 요청 | 401 |
| `ROLE_USER`의 Actuator 접근 | 403 |
| `ROLE_ADMIN`의 Actuator 접근 | 성공 |
| `ROLE_USER`의 공개 채널 생성 | 403 |
| `ROLE_CHANNEL_MANAGER`의 공개 채널 생성 | 성공 |
| `ROLE_ADMIN`의 공개 채널 생성 | Role Hierarchy로 성공 |

`DiscodeitApiIntegrationTest#privateChannelAndReadStatus_flowCreatesAndUpdatesParticipantReadStatuses`는 `ROLE_USER`의 비공개 채널 생성과 참여자별 ReadStatus 저장을 실제 DB까지 검증한다.

---

## 5. @WithMockUser가 검증하지 않는 것

`@WithMockUser`는 실제 username/password 로그인을 수행하는 도구로 이해하면 안 된다.

```text
@WithMockUser
→ 테스트용 Authentication 구성
→ SecurityContext에 인증 사용자 제공
→ 인가 규칙 검증
```

따라서 다음 실제 인증 흐름까지 검증했다고 볼 수 없다.

```text
username/password
→ AuthenticationManager
→ AuthenticationProvider
→ UserDetailsService
→ PasswordEncoder
→ 로그인 성공
```

이 때문에 다음 두 테스트는 목적이 다르다.

```text
@WithMockUser(roles = "ADMIN")
→ /admin 200
→ 인가 규칙 검증

실제 로그인 요청 성공
→ 실제 인증 구성요소 연결 검증
```

첫 번째가 성공해도 두 번째가 실패할 수 있다.

예를 들어 `UserDetailsService` 사용자 조회, PasswordEncoder 설정, 비밀번호 값, Provider 구성에 문제가 있어도 `@WithMockUser` 인가 테스트는 통과할 수 있다.

현재 프로젝트는 실제 인증 흐름도 `DiscodeitApiIntegrationTest`에서 별도로 검증한다.

- 실제 사용자 생성 후 username/password 로그인
- 인증 성공 후 HTTP Session 생성
- 같은 Session으로 `/api/auth/me` 접근
- 로그아웃 후 Session과 Cookie 만료
- 잘못된 비밀번호와 존재하지 않는 사용자에 동일한 401 응답
- 같은 계정의 두 번째 로그인 시 이전 Session 만료
- Remember-Me 자동 로그인

따라서 이 저장소에서는 `@WithMockUser` 테스트와 실제 로그인 테스트가 서로 다른 책임을 맡는다.

---

## 6. 테스트 종류별 선택

### 단위 테스트

개별 Security 컴포넌트의 자체 로직이 관심사일 때 사용한다.

예:

- `DiscodeitUserDetailsService`의 사용자 조회/예외 변환
- `DiscodeitUserDetails`의 authority 생성
- `LoginSuccessHandler`, `LoginFailureHandler`
- Guard 클래스

현재 저장소에도 위 컴포넌트들의 단위 테스트가 별도로 존재한다.

### Web Slice 테스트

Web MVC와 Security 설정의 일부를 좁은 범위에서 확인하고 싶을 때 선택할 수 있다.

인가 규칙만 검증하려는 경우 실제 DB, 전체 Service, 모든 Bean을 올릴 필요가 없다면 좋은 후보가 될 수 있다.

다만 `@WebMvcTest`가 자동으로 "Security만" 올리는 것은 아니다. Web MVC 계층 중심의 Slice이며 테스트 목적에 필요한 Security 설정을 함께 구성해야 한다.

현재 `ChannelControllerTest`는 `@AutoConfigureMockMvc(addFilters = false)`를 사용하고 `ChannelService`도 Mock으로 대체한다. 이 테스트는 요청 검증, DTO 변환과 Controller 응답을 확인하지만, Filter 인가나 실제 Service 프록시의 `@PreAuthorize`를 증명하지 않는다.

### 통합 테스트

실제 Spring Bean과 Security 설정, DB까지 함께 연결되는 흐름이 관심사라면 `@SpringBootTest` 범위가 필요할 수 있다.

현재 `SecurityAuthorizationIntegrationTest`는 다음 구성을 사용한다.

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
```

따라서 단순 MockMvc Controller 테스트보다 넓은 범위에서 현재 애플리케이션의 Security 설정과 권한 정책을 검증한다.

Service의 `@PreAuthorize`가 실제 Spring AOP 프록시를 통해 적용되는지 확인하려면 현재 프로젝트처럼 통합 테스트에서 Controller부터 Service 프록시까지 연결해 검증하는 것이 적절하다.

---

## 7. A는 성공하고 B는 실패할 수 있다

다음 상황은 충분히 가능하다.

```text
A. @WithMockUser(roles = "ADMIN")
   → GET /admin
   → 200

B. 실제 username/password 로그인
   → 실패
```

A에서는 실제 사용자 조회와 비밀번호 검증을 건너뛰기 때문이다.

반대로 실제 로그인은 성공했지만 `/admin`에서 403이 날 수도 있다.

이 경우 다음 흐름을 순서대로 확인한다.

```text
DB의 Role
→ UserDetails.getAuthorities()
→ AuthenticationProvider가 만든 Authentication.authorities
→ hasRole / hasAuthority 설정
```

현재 프로젝트의 `DiscodeitUserDetails`는 `ROLE_` 접두사를 명시적으로 붙인다.

---

## 8. 테스트 선택 기준

테스트를 작성하기 전에 다음 질문에 답한다.

1. 실제 username/password 인증까지 검증해야 하는가?
2. 이미 인증된 사용자라고 가정하고 인가만 검증하면 되는가?
3. 실제 DB 사용자가 필요한가?
4. Security Filter Chain의 실제 설정이 관심사인가?
5. 특정 Handler나 UserDetailsService 하나의 로직만 보면 되는가?

테스트 범위를 무조건 크게 잡지 않고, **테스트가 증명해야 하는 책임에 맞는 가장 작은 범위**를 선택한다.
