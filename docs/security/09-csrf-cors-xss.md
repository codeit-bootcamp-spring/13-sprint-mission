# CSRF·CORS·XSS와 Cookie 보안

이 문서는 세 보안 개념을 각각 구분하고, Session Cookie 및 현재 프로젝트의 CSRF 설정과 연결해 설명한다.

## 1. CSRF·CORS·XSS의 관계

### CSRF

세션 쿠키는 브라우저가 대상 서버 요청에 자동으로 첨부한다. 공격자는 피해자의 비밀번호나 Session ID를 몰라도 피해자의 브라우저가 인증 쿠키를 보내게 만들 수 있으므로, 상태 변경 요청에는 쿠키만으로는 알 수 없는 CSRF Token을 추가로 검증한다.

현재 프로젝트는 `CookieCsrfTokenRepository.withHttpOnlyFalse()`를 사용한다. 클라이언트가 `XSRF-TOKEN` 쿠키 값을 읽어 `X-XSRF-TOKEN` 요청 Header로 보내는 방식이다. Token이 없거나 서버가 기대한 값과 다르면 `CsrfFilter`에서 403으로 거부되며, 로그인 요청이라면 `UsernamePasswordAuthenticationFilter`까지 도달하지 않는다.

쿠키 값은 클라이언트가 바꿀 수 있으므로, 쿠키에 값이 존재한다는 사실만으로 검증이 끝나는 것이 아니다. 현재의 Cookie 기반 Repository는 CSRF Cookie에서 불러온 기대값과 Header의 요청값을 비교한다. Session에 기대값을 저장하는 `HttpSessionCsrfTokenRepository`와 저장 위치를 혼동하지 않는다.

### CORS

CORS는 브라우저에서 다른 Origin의 Script가 요청과 응답을 다루는 범위를 제한한다. 특히 응답 읽기와 Preflight 승인에 관여한다.

CORS가 거부됐다는 사실만으로 CSRF 공격이 막혔다고 판단하면 안 된다. 일부 Cross-Origin 요청은 전송될 수 있고 브라우저가 응답만 Script에 공개하지 않을 수 있기 때문이다. CSRF 방어는 CSRF Token과 SameSite 등 별도 수단으로 설계한다.

### XSS

XSS로 신뢰하는 Origin에서 공격자 Script가 실행되면 그 Script는 페이지의 CSRF Token을 읽거나 정상 코드처럼 API를 호출할 수 있다. 현재처럼 `XSRF-TOKEN`을 JavaScript가 읽어야 해서 HttpOnly를 끈 구조에서는 XSS가 Token까지 읽을 수 있다.

Session Cookie에 HttpOnly를 설정하면 JavaScript가 Cookie 값을 직접 훔치는 것은 어렵게 하지만, 브라우저는 같은 사이트 요청에 해당 Cookie를 자동 첨부할 수 있다. 따라서 HttpOnly만으로 XSS가 피해자 권한의 요청을 실행하는 것까지 막지는 못한다.

### Cookie 속성 구분

| 속성 | 핵심 역할 |
|---|---|
| `HttpOnly` | JavaScript의 Cookie 값 직접 읽기를 제한한다. |
| `Secure` | HTTPS 연결에서만 Cookie를 전송하도록 한다. |
| `SameSite` | Cross-Site 상황에서 Cookie 전송 범위를 제한한다. `Strict`, `Lax`, `None`의 동작이 다르다. |

`SameSite`의 Site 개념은 CORS의 Origin과 같지 않다. 세 속성은 서로 대체 관계가 아니며 HTTPS, CSP, 출력 인코딩, 입력 처리, CSRF Token 등과 함께 방어 계층을 구성한다.

---

## 2. 구분해서 설명해야 하는 핵심

```text
CSRF
→ 피해자의 브라우저가 인증 Cookie를 자동 전송하는 점을 악용한 요청 위조 방어

CORS
→ 다른 Origin의 브라우저 Script가 요청과 응답을 다루는 범위 제어

XSS
→ 신뢰하는 페이지 안에서 공격자 Script가 실행되는 문제
```

CORS 오류가 보였다고 서버의 상태 변경이 반드시 실행되지 않았다고 단정하지 않는다. 또한 CSRF Token과 HttpOnly Cookie를 적용했더라도 XSS 방어가 자동으로 완료되는 것은 아니다.
