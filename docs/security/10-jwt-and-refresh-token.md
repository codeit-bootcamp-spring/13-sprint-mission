# JWT와 Refresh Token

이 문서는 Session 인증과 비교해 JWT 요청 인증을 이해하고, Refresh Token을 운영할 때 발생하는 저장·Rotation·동시성·응답 유실 문제를 정리한다.

현재 Discodeit은 Session 기반 Form Login을 사용한다. 아래 내용은 JWT를 현재 프로젝트에 이미 적용했다는 설명이 아니라 이후 토큰 인증 학습과 설계를 위한 기록이다.

## 1. JWT Stateless 인증

### 현재까지 정리한 핵심

JWT Stateless 방식에서도 현재 요청 동안 인가와 사용자 접근을 위해 `Authentication`과 `SecurityContext`는 필요할 수 있다.

차이는 요청 종료 뒤 서버 Session에 인증 상태를 보존하지 않는다는 점이다.

```text
요청
→ Bearer Access Token
→ 서명 / 만료 검증
→ Claim 확인
→ Authentication 구성
→ SecurityContext
→ 인가
→ 요청 종료
```

다음 요청에서는 기존 Session의 SecurityContext를 복원하는 대신 Token을 다시 검증해 인증 상태를 구성한다.

### JWT Signature

JWT Payload는 일반적으로 암호화된 비밀 내용이 아니라 Base64Url로 표현되어 내용을 확인할 수 있다.

Signature의 핵심 목적은 Payload를 숨기는 것이 아니라 토큰이 발급 이후 변조되었는지 검증하는 것이다.

따라서 비밀번호, 주민번호 등 노출되어서는 안 되는 민감정보를 Payload에 넣지 않는다.

---

## 2. Access Token 만료와 Refresh Token

Access Token을 짧게 유지하는 이유 중 하나는 탈취됐을 때 악용 가능한 시간을 줄이기 위해서다.

```text
Access Token
→ API 접근
→ 비교적 짧은 수명

Refresh Token
→ 새로운 Access Token 발급
→ 상대적으로 긴 수명
→ 일반 API 요청마다 보내지 않음
```

Refresh Token은 긴 수명을 가지므로 매 요청마다 전송하면 노출 기회가 증가한다.

### 서버 상태와 Stateless의 트레이드오프

JWT를 쓴다고 해서 인증 시스템 전체가 무조건 아무 상태도 관리하지 않는 것은 아니다.

예를 들어 다음 요구사항을 만족하려면 서버 상태를 일부 관리할 수 있다.

- Refresh Token 유효성 관리
- 강제 로그아웃
- Token Reuse 탐지
- Access Token Blocklist

Redis Blocklist를 매 요청 확인하면 토큰 폐기는 쉬워지지만 Redis 네트워크 조회와 저장소 의존성이 추가되어 Stateless의 장점 일부가 줄어든다.

서비스에 즉시 Access Token 폐기 요구가 낮고 Access Token 만료가 충분히 짧다면, Blocklist 없이 최대 만료 시간만큼의 지연을 허용하는 정책도 비교할 수 있다.

---

## 3. Refresh Token 저장

Refresh Token 원문을 DB/Redis에 영구 보관하면 저장소 유출 시 해당 토큰을 그대로 사용할 위험이 있다.

서버가 Refresh Token 원문을 다시 복원할 필요가 없다면 해시값을 저장하고 요청으로 들어온 토큰을 동일한 방식으로 해시해 비교하는 방법을 고려할 수 있다.

서버가 충분한 엔트로피의 긴 Random Token을 만든다면 비밀번호처럼 사용자가 추측하기 쉬운 값과 특성이 다르므로 반드시 BCrypt 같은 느린 비밀번호 해시와 동일한 방식으로 저장해야 하는 것은 아니다.

---

## 4. Refresh Token Rotation과 Reuse Detection

```text
Refresh Token A
→ 재발급 성공
→ A 폐기
→ Refresh Token B 발급
```

이미 폐기된 A가 나중에 다시 들어오면 정상적인 클라이언트 재사용인지 탈취된 토큰인지 판단해야 한다.

단순히 다른 IP라는 이유만으로 공격으로 확정하면 안 된다. 모바일 네트워크, NAT, 프록시 등으로 정상 사용자도 IP가 바뀔 수 있기 때문이다.

보수적인 정책에서는 이미 사용된 토큰의 재사용을 탐지하면 해당 Token Family를 폐기하고 재로그인을 요구하는 방식을 고려할 수 있다.

---

## 5. Refresh Token Rotation의 동시성

동일한 Refresh Token A로 두 요청이 거의 동시에 들어오면 다음 경쟁 상태가 생길 수 있다.

```text
요청 1: A = ACTIVE 확인
요청 2: A = ACTIVE 확인

요청 1: B 발급
요청 2: C 발급
```

A는 한 번만 사용되어야 하므로 두 요청이 모두 성공하면 안 된다.

### 후보 1: 비관적 락

```text
SELECT ... FOR UPDATE
→ A Row 잠금
→ ACTIVE 확인
→ USED 변경
→ 새 Token 저장
→ Commit
```

확실하게 직렬화할 수 있지만 Lock 대기와 트랜잭션 유지 시간이 생긴다.

### 후보 2: 조건부 UPDATE

```sql
UPDATE refresh_token
SET status = 'USED'
WHERE id = ?
  AND status = 'ACTIVE';
```

변경 row가 1이면 성공, 0이면 이미 소비된 Token으로 판단할 수 있다.

이 문제의 핵심은 전형적인 단순 Lost Update라기보다 **하나의 Token이 두 번 소비되는 Check-then-act 경쟁 상태**다.

조건부 UPDATE 역시 DB 내부 Lock과 동시성 제어가 사라지는 것은 아니지만, 애플리케이션의 별도 `SELECT FOR UPDATE → 상태 확인 → UPDATE` 흐름을 줄일 수 있다.

---

## 6. Rotation과 Transaction

기존 A를 `USED`로 만들고 새로운 B를 저장하는 것은 하나의 논리적 작업이다.

```text
Transaction
→ A: ACTIVE → USED
→ B 저장
→ 둘 다 성공: COMMIT
→ B 저장 실패: ROLLBACK
```

B 저장이 실패했는데 A만 USED로 남으면 사용자가 재발급할 방법을 잃을 수 있으므로 두 DB 작업의 원자성을 고려해야 한다.

---

## 7. 응답 유실과 멱등성

DB에서는 A가 USED, B가 ACTIVE로 정상 Commit됐지만 네트워크 오류로 클라이언트가 B를 받지 못할 수 있다.

이때 A를 다시 요청했다고 즉시 공격으로 판단하면 정상적인 네트워크 재시도를 오탐할 수 있다.

후보:

- 짧은 Grace Period
- Idempotency Key
- 재발급 결과를 아주 짧은 TTL로 공유 Cache에 보관

DB에는 B의 Hash만 보관한다면 Hash에서 B 원문을 다시 복원할 수 없다. 동일 B를 다시 전달해야 하는 요구사항이 있다면 짧은 시간 동안 재시도 결과를 안전하게 보관하는 별도 설계를 검토해야 한다.

Redis 같은 공유 Cache를 사용할 경우 다중 인스턴스에서도 재시도 결과를 공유할 수 있지만, 민감한 Token 원문 저장 위험, Redis 장애 의존성, TTL 정책을 함께 고려해야 한다.

---

## 8. 사용자 상태 변경과 JWT

JWT 발급 뒤 DB에서 사용자가 탈퇴하거나 `enabled=false`로 바뀌어도, 서버가 매 요청 Token 정보만 신뢰한다면 기존 Access Token 만료 전까지 상태 변경이 즉시 반영되지 않을 수 있다.

선택지는 서비스 요구사항에 따라 달라진다.

- 짧은 Access Token 만료
- Refresh 시점에 사용자 상태 재확인
- 중요한 요청에서 사용자 상태 추가 조회
- 즉시 폐기가 필요하다면 Blocklist 등 별도 상태 관리

즉 JWT 사용 여부만으로 정답을 정하지 않고 **즉시 반영 요구와 운영 복잡성의 트레이드오프**를 비교한다.

---

## 9. 구현 전에 정할 정책

1. Access Token 만료 시간을 어느 정도로 허용할 것인가?
2. 강제 로그아웃과 권한 회수를 즉시 반영해야 하는가?
3. Refresh Token 원문과 Hash 중 무엇을 저장할 것인가?
4. Rotation 중 같은 Token의 동시 사용을 어떻게 한 번만 성공시킬 것인가?
5. 정상 응답이 유실됐을 때 재시도를 공격으로 판단할 것인가?
6. Redis 같은 공유 상태 저장소의 장애를 어디까지 감수할 것인가?
