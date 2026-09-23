# Spring Security 인증 흐름

## 1. 목표

Form Login을 기준으로 사용자가 username/password를 제출한 뒤 인증된 `Authentication`이 만들어질 때까지의 흐름을 설명한다.

현재 프로젝트의 로그인 처리 경로는 `SecurityConfig`의 다음 설정과 연결된다.

```java
.formLogin(form -> form
        .loginProcessingUrl("/api/auth/login")
        .successHandler(loginSuccessHandler)
        .failureHandler(loginFailureHandler)
)
```

실제 로그인 POST 요청은 별도의 Controller가 반드시 처리해야 하는 것이 아니라 Spring Security의 인증 Filter가 먼저 처리할 수 있다.

---

## 2. 최초 로그인 흐름

```text
POST /api/auth/login
        ↓
Security Filter Chain
        ↓
Form Login 인증 Filter
        ↓
인증 전 Authentication 생성
        ↓
AuthenticationManager
        ↓
ProviderManager
        ↓
AuthenticationProvider
        ↓
UserDetailsService
        ↓
사용자 조회
        ↓
PasswordEncoder.matches(...)
        ↓
인증 성공
        ↓
인증된 Authentication 생성
        ↓
SecurityContext에 저장
        ↓
LoginSuccessHandler
        ↓
200 응답
```

중요한 점은 로그인 요청이 들어왔다는 이유로 먼저 `AnonymousAuthenticationToken`을 만들어 인증하는 것이 아니라는 것이다.

Form Login에서는 username/password를 담은 인증 전 `Authentication`을 생성하고 실제 인증 절차를 진행한다.

---

## 3. AuthenticationManager와 AuthenticationProvider

`AuthenticationManager`는 인증 요청을 받는 인터페이스다.

일반적인 구현체인 `ProviderManager`는 등록된 `AuthenticationProvider`를 순서대로 확인하고, Provider의 `supports()`를 이용해 현재 `Authentication` 타입을 처리할 수 있는지 판단한다.

```text
AuthenticationManager
        ↓
ProviderManager
        ↓
provider.supports(authentication.getClass())
        ↓
처리 가능한 Provider
        ↓
provider.authenticate(authentication)
```

`supports()`는 로그인 방식의 문자열 이름을 비교하는 것이 아니라 전달된 `Authentication` 구현 타입을 기준으로 지원 여부를 판단한다.

여러 Provider가 같은 타입을 지원할 수도 있다. Provider가 인증을 성공하면 그 결과를 사용하고, 처리하지 못해 `null`을 반환하면 다음 Provider를 시도할 수 있다.

### 여러 Provider가 같은 타입을 지원할 때

`ProviderManager`의 진행 규칙은 다음처럼 구분한다.

```text
supports() == false
→ 해당 Provider를 건너뜀

authenticate()가 null 반환
→ 현재 Provider가 결과를 만들지 못함
→ 다음 지원 Provider 시도

authenticate()가 Authentication 반환
→ 인증 성공
→ 뒤 Provider는 실행하지 않음

authenticate()가 일반 AuthenticationException 발생
→ 실패 예외를 기억하고 다음 지원 Provider를 시도할 수 있음
→ 모두 실패하면 마지막 실패 예외를 다시 던짐

AccountStatusException 또는 InternalAuthenticationServiceException
→ 계정 상태나 내부 서비스 문제
→ 즉시 중단하고 예외 전파
```

따라서 “Provider에서 예외가 나면 항상 즉시 다음 Provider로 넘어간다”거나 “항상 AuthenticationEntryPoint가 처리한다”고 단정하지 않는다. Form Login 중 최종 인증 실패가 Filter로 돌아오면 현재 프로젝트의 `LoginFailureHandler`가 401 응답을 만든다.

현재 프로젝트는 별도의 커스텀 `AuthenticationProvider`를 구현하지 않고 `UserDetailsService`와 `PasswordEncoder`를 제공하여 username/password 인증 흐름과 연결한다.

---

## 4. UserDetailsService의 책임

현재 프로젝트의 `DiscodeitUserDetailsService`는 username으로 사용자를 조회한 뒤 `DiscodeitUserDetails`로 변환한다.

```java
@Override
public UserDetails loadUserByUsername(String username) {
    User user = userReader.getByUsername(username);
    return convertToUserDetails(user);
}
```

`UserDetailsService`의 책임은 **사용자 정보 조회**다.

직접 비밀번호를 비교해서 인증 성공/실패를 결정하는 객체로 이해하지 않는다.

```text
UserDetailsService
→ 사용자 조회
→ UserDetails 반환

AuthenticationProvider
→ UserDetails와 요청의 인증정보를 이용
→ 실제 인증 판단
```

현재 `DiscodeitUserDetails`는 다음 정보를 제공한다.

- username
- 해시된 password
- authorities
- 계정 만료 여부
- 계정 잠금 여부
- credentials 만료 여부
- enabled 여부

현재 구현에서는 계정 상태 관련 네 메서드가 모두 `true`를 반환한다.

---

## 5. PasswordEncoder

현재 프로젝트는 `BCryptPasswordEncoder`를 Bean으로 사용한다.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

비밀번호 검증은 저장된 값을 복호화해서 원문을 확인하는 방식이 아니다.

```text
사용자가 입력한 평문 비밀번호
+
DB에 저장된 BCrypt 해시
        ↓
PasswordEncoder.matches(...)
        ↓
일치 여부 확인
```

BCrypt는 salt를 사용하므로 같은 비밀번호를 여러 번 encode해도 결과 해시가 달라질 수 있다.

Salt의 목적은 숨겨진 비밀값을 추가하는 것이 아니라, 같은 비밀번호라도 다른 해시가 나오도록 해서 하나의 미리 계산된 결과를 여러 사용자에게 그대로 재사용하기 어렵게 만드는 것이다.

BCrypt의 cost는 높을수록 대입 공격 비용을 키울 수 있지만 정상 로그인과 회원가입의 CPU 비용도 함께 증가하므로 보안성과 성능 사이의 절충이 필요하다.

---

## 6. 인증 전과 인증 후 Authentication

인증 전에는 아직 사용자가 검증되지 않은 상태다.

```text
principal
→ username 같은 사용자 식별 정보

credentials
→ password 같은 자격 증명

authorities
→ 아직 최종 권한이 확정되지 않음

authenticated
→ false
```

인증이 성공하면 다음과 같이 이해할 수 있다.

```text
principal
→ 인증된 사용자 정보(UserDetails 등)

credentials
→ 인증 이후 보안을 위해 제거될 수 있음

authorities
→ ROLE_USER, ROLE_ADMIN 등의 실제 권한

authenticated
→ true
```

인증 성공 뒤에는 자격 증명을 계속 보관할 이유가 없다. `ProviderManager`는 성공 결과가 `CredentialsContainer`를 구현한 경우 `eraseCredentials()`를 호출해 비밀번호 같은 민감정보를 제거할 수 있다. 목적은 민감정보가 SecurityContext, Session, 로그나 디버깅 정보에 오래 남는 범위를 줄이는 것이다.

현재 `DiscodeitUserDetails#getAuthorities()`는 사용자 Role 앞에 `ROLE_`을 붙여 `SimpleGrantedAuthority`를 반환한다.

---

## 7. 사용자 없음과 비밀번호 불일치

사용자를 찾지 못하면 `UsernameNotFoundException`이 발생할 수 있고, 비밀번호가 맞지 않으면 일반적으로 잘못된 자격 증명으로 인증에 실패한다.

클라이언트에게 "사용자 없음"과 "비밀번호 틀림"을 지나치게 구분해서 알려주면 어떤 계정이 실제로 존재하는지 공격자가 알아낼 수 있다.

따라서 인증 실패 응답에서는 사용자 존재 여부가 불필요하게 노출되지 않도록 고려해야 한다.

현재 프로젝트에서는 `LoginFailureHandler`가 Form Login 인증 실패 응답을 담당한다.

현재 구현에서 `LoginFailureHandler`는 `UserLoginFailedException` 형식의 401 JSON을 직접 작성한다. 보호 API의 미인증 요청을 처리하는 `AuthenticationEntryPoint`에 다시 위임하는 구조가 아니다.

```text
POST /api/auth/login에서 인증 실패
→ LoginFailureHandler

로그인하지 않은 사용자의 보호 API 요청
→ AuthenticationEntryPoint

로그인했지만 권한 부족
→ AccessDeniedHandler
```

---

## 8. 현재 설명할 수 있어야 하는 핵심

```text
Form Login 요청
→ 인증 전 Authentication
→ AuthenticationManager
→ ProviderManager
→ AuthenticationProvider
→ UserDetailsService
→ PasswordEncoder
→ 인증된 Authentication
→ SecurityContext
```

이 흐름을 설명할 때 `AnonymousAuthenticationToken`을 인증 전 Authentication과 혼동하지 않는 것이 중요하다.
