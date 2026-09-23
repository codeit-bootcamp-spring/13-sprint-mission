# Spring Security Deep Dive 안내

이 문서는 2026년 9월 16일 구두 학습에서 실제로 다룬 확장 주제의 안내 페이지다.

시간을 들여 확인한 내용은 제외하지 않는다. 다만 현재 Discodeit 코드에서 검증된 동작과 이후 적용할 수 있는 설계 후보가 한 문서에 섞이지 않도록 주제별 페이지로 분리한다.

```text
기본 흐름 이해
→ 실제 프로젝트 적용
→ 테스트
→ 구두 설명
→ Deep Dive 기록
→ 필요할 때 재현·구현
```

## 1. 문서 분리 기준

핵심 학습 범위와 Deep Dive를 구분하는 목적은 기록을 줄이기 위해서가 아니다.

- 하나의 페이지에서 서로 다른 문제를 동시에 설명하지 않는다.
- 현재 프로젝트에 구현된 내용과 향후 설계 후보를 구분한다.
- 면접과 복습에서는 핵심 문서를 먼저 보고, 필요할 때 Deep Dive 문서로 이동한다.

---

## 2. 주제별 Deep Dive

| 문서 | 주요 내용 | 현재 프로젝트 적용 여부 |
| --- | --- | --- |
| [07-async-security-context.md](07-async-security-context.md) | 비동기 Thread의 SecurityContext 전파, ID 전달과 Context 전달, 권한 판단 시점 | 학습·설계 후보 |
| [08-session-consistency-and-scaling.md](08-session-consistency-and-scaling.md) | 권한 회수, TOCTOU, 동일 Session 동시 요청, Sticky Session, Spring Session·Redis | 일부 적용, 일부 설계 후보 |
| [09-csrf-cors-xss.md](09-csrf-cors-xss.md) | CSRF·CORS·XSS의 역할과 관계, Cookie 보안 속성 | CSRF 설정 적용 |
| [10-jwt-and-refresh-token.md](10-jwt-and-refresh-token.md) | JWT, Access/Refresh Token, Rotation, 동시성, 응답 유실과 멱등성 | 향후 학습 후보 |

인증 Provider의 `supports`, `null`, 성공 결과와 예외 처리 순서는 [01-authentication-flow.md](01-authentication-flow.md)에 정리한다.

현재 설정에서 활성화된 Security Filter의 전체 순서와 `ExceptionTranslationFilter`의 위치는 [04-security-filter-chain.md](04-security-filter-chain.md)에 정리한다.

---

## 3. 프로젝트에서 확인된 내용과 설계 후보

현재 프로젝트 코드와 테스트에서 직접 확인한 내용은 다음과 같다.

- Session 기반 Form Login과 로그아웃
- `CookieCsrfTokenRepository`를 이용한 CSRF 방어
- Role 변경 뒤 `SessionRegistry`로 대상 Session 만료
- URL 인가와 Service `@PreAuthorize`
- 로그인·Session·권한 규칙 통합 테스트

다음 내용은 구두 학습과 설계 검토 기록이며, 현재 프로젝트에 모두 구현됐다는 뜻은 아니다.

- 비동기 SecurityContext 전파 래퍼
- JWT와 Refresh Token Rotation
- Spring Session과 Redis
- 권한 변경 중 실행되는 요청의 추가 정합성 처리

---

## 4. 현재 단계의 완료 기준

Deep Dive 문제를 모두 현재 미션에 구현하는 것이 완료 조건은 아니다.

먼저 다음 핵심 흐름을 자료 없이 설명하고 현재 프로젝트의 테스트로 연결할 수 있어야 한다.

```text
Form Login 최초 인증
→ AuthenticationManager / Provider
→ UserDetailsService / PasswordEncoder
→ SecurityContext / Session
→ 다음 요청에서 인증 상태 복원
→ URL / Method Authorization
→ 401 / 403
```

Deep Dive 내용은 학습 기록에서 제외하지 않는다. 다음 미션이나 실제 문제가 생기면 관련 페이지를 출발점으로 삼아 재현하고 검증한다.
