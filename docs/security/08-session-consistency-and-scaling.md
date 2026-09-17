# Session 정합성과 다중 인스턴스

이 문서는 Session 사용자의 권한 변경, 같은 Session의 동시 요청, 여러 애플리케이션 인스턴스에서의 Session 유지 문제를 함께 정리한다.

현재 프로젝트에는 역할 변경 뒤 대상 Session을 만료하는 처리가 있다. 실행 중 요청의 추가 정합성 전략과 Spring Session·Redis는 설계 후보로 구분한다.

## 1. 세션 권한 변경과 실행 중 요청

세션 사용자가 로그인한 뒤 DB의 Role이 바뀌어도 기존 Session의 Authentication 권한이 자동으로 최신화된다고 가정하면 안 된다.

권한 회수를 즉시 반영하려면 다음 방법을 비교할 수 있다.

- 기존 활성 Session 만료 후 재로그인
- 중요 작업 직전에 최신 권한 재확인
- 권한 Version 관리

현재 프로젝트의 `UserRoleManager`는 Role 변경 뒤 `SessionRegistry`에서 대상 사용자의 활성 Session을 찾아 `expireNow()`로 만료시킨다. 이는 다음 요청부터 새 인증을 요구하게 하는 전략이다.

다만 이미 `@PreAuthorize`를 통과해 실행 중인 요청은 권한을 회수했다고 자동으로 중간 취소되지 않는다. 실행 중 요청까지 보호해야 하는 중요 작업은 실제 변경 직전에 DB 상태를 재확인하거나, 권한 조건을 포함한 조건부 UPDATE, 필요한 범위의 비관적 락 등을 업무 규칙에 맞게 검토한다.

이 문제는 검사 시점과 사용 시점이 달라지는 TOCTOU 문제이며, 인증·인가뿐 아니라 동시성, 데이터 정합성, 트랜잭션 정책과 연결된다.

---

## 2. 동일 세션의 동시 요청과 객체 공유

같은 HTTP Session에서 동시에 처리되는 요청은 Session에 저장된 같은 `SecurityContext` 참조를 공유할 수 있다.

ThreadLocal은 각 Thread가 Context에 접근하는 위치를 분리하지만, 그 안에 넣는 Context 객체 자체가 언제나 새 객체라는 뜻은 아니다.

따라서 한 요청에서 기존 `Authentication`이나 `SecurityContext`를 직접 변경하면 같은 세션의 다른 동시 요청에 영향을 줄 수 있다. 요청별로 임시 인증 변경이 필요하다면 기존 공유 객체를 제자리에서 수정하지 말고 새 `SecurityContext`를 만들어 요청 범위에 설정하는 방식을 검토한다.

---

## 3. 다중 인스턴스의 Session 관리

### Sticky Session

Sticky Session은 특정 사용자의 후속 요청을 같은 애플리케이션 인스턴스로 라우팅하는 방식이다. “고유한 종류의 세션”을 새로 만드는 것이 아니다.

서버 로컬 메모리에 Session을 둘 수 있다는 장점이 있지만, 해당 인스턴스 장애, 배포, 증설·축소, 라우팅 변경 시 Session 연속성이 깨질 수 있고 특정 서버에 부하가 몰릴 수 있다.

### Spring Session과 Redis

Spring Session은 애플리케이션이 사용하는 `HttpSession` 저장소를 Redis 같은 공유 저장소로 교체할 수 있게 한다.

```text
브라우저
→ Session ID Cookie

애플리케이션 A / B
→ 같은 Session ID로 공유 저장소 조회

Redis
→ Session 속성
→ SecurityContext
```

브라우저에는 기존처럼 Session ID만 두고, SecurityContext와 인증 정보는 서버 측 Session 속성으로 유지한다. 어느 애플리케이션 인스턴스가 요청을 받아도 같은 Redis Session을 조회할 수 있다.

대신 Redis 가용성, 네트워크 비용, Session TTL, 직렬화 호환성, 저장 데이터 보호, 장애 시 로그인 영향까지 운영 범위에 포함된다.

---

## 4. 선택 기준

| 문제 | 우선 검토할 방법 |
| --- | --- |
| 권한 변경을 다음 요청부터 반영 | 대상 사용자의 활성 Session 만료 |
| 실행 중인 중요 쓰기의 권한 정합성 | 쓰기 직전 재검증, 조건부 UPDATE, 필요한 범위의 Lock |
| 같은 Session의 요청별 임시 인증 변경 | 공유 Context를 직접 수정하지 않고 새 Context 구성 |
| 단일 서버 장애에도 Session 유지 | Spring Session과 공유 저장소 |
| 간단한 단일 서버 라우팅 유지 | Sticky Session의 장애·확장 한계를 함께 검토 |
