# 비동기 처리와 SecurityContext

이 문서는 `@Async`, `CompletableFuture`, 별도 `Executor`에서 인증 정보를 어떻게 다룰지 정리한다.

현재 Discodeit에 비동기 SecurityContext 전파를 구현했다는 의미는 아니다. Java 비동기 학습이나 실제 비동기 기능을 적용할 때 판단 기준으로 사용한다.

## 1. 비동기와 SecurityContext

### 확인한 문제

`SecurityContextHolder`의 기본 전략은 현재 실행 Thread와 `SecurityContext`를 연결한다.

따라서 다른 Thread에서 실행되는 `@Async`, `CompletableFuture`, 별도 `Executor` 작업은 요청 Thread의 인증 정보를 자동으로 사용할 수 있다고 가정하면 안 된다.

```text
요청 Thread-A
→ SecurityContext 있음

비동기 Thread-B
→ 별도 전파가 없으면 요청의 SecurityContext 없음
```

### 전파가 정말 필요한 경우

Spring Security는 다음과 같은 래퍼를 제공한다.

- `DelegatingSecurityContextRunnable`
- `DelegatingSecurityContextCallable`
- `DelegatingSecurityContextExecutor`
- `DelegatingSecurityContextAsyncTaskExecutor`

이들은 명시적으로 지정했거나 구성·제출 과정에서 캡처한 SecurityContext를 작업 실행 전에 설정하고, 실행이 끝나면 정리하는 책임을 묶는다. 정확한 캡처 시점은 사용하는 생성자와 위임 객체 구성에 따라 확인해야 한다. 직접 ThreadLocal에 값을 넣고 지우는 방식보다 누락 위험을 줄일 수 있다.

단, Context를 전달한다고 문제가 끝나는 것은 아니다. 제출 시점의 권한을 사용할지, 실제 실행 시점에 DB에서 최신 권한을 다시 확인할지는 업무 정책으로 결정해야 한다.

### ID만 전달할지 Context를 전달할지

비동기 작업에 사용자 ID만 필요하다면 ID를 명시적으로 인자로 전달하는 편이 의존성과 관리 범위가 작다.

SecurityContext 전체 전파는 다음과 같이 실제 인증 정보가 필요한 경우에 고려한다.

- 하위 호출이 현재 `Authentication`을 요구한다.
- 감사 정보가 현재 Principal과 Authority를 필요로 한다.
- Method Security가 비동기 Thread에서도 동작해야 한다.

권한 변경 가능성이 중요한 작업이라면 전달된 Authority만 신뢰하지 않고 실행 시점에 최신 권한이나 업무 상태를 다시 확인할 수 있다.

이 주제는 Java 비동기 수업과 기존 Findex의 `CompletableFuture + 전용 Executor` 경험에 연결한다.

---

## 2. 적용 전 확인할 질문

1. 비동기 작업에 정말 전체 `Authentication`이 필요한가?
2. 사용자 ID만 전달해도 업무 요구사항을 만족하는가?
3. 요청 시점 권한과 실행 시점 권한 중 무엇을 기준으로 삼아야 하는가?
4. 전용 Executor에서 Context 설정과 정리가 모두 보장되는가?
5. 실패·재시도·지연 실행에서도 같은 정책을 유지해야 하는가?
