# 1. build 스테이지
FROM eclipse-temurin:17-jdk AS build
# 기존 강의에서 사용한 것과 같이 eclipse temurin 17 이미지 사용
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 소스 코드 복사 후 실행 가능한 jar 블드
COPY src ./src
RUN ./gradlew bootJar -x test
# 테스트 코드를 제외하고 빌드하여 실행 가능한 jar 파일 생성

# 2. run 스테이지
FROM eclipse-temurin:17-jre
# jre 사용해 용량을 더 줄인다
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
# 불필요한 패키지 설치 막고 패키지 목록 임시 파일들을 전부 다 삭제해 이미지 용량을 줄인다

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=2.0-M9
ENV JVM_OPTS=""
COPY --from=build /workspace/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar ./

ENV TZ=Asia/Seoul
# 타임존 설정

ENV SPRING_PROFILES_ACTIVE=prod
# 운영 프로파일로 실행하기 위해

EXPOSE 80
# 80 포트 노출

HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
  CMD curl -fsS http://localhost:80/actuator/health || exit 1
# Actuator 헬스체크 60초 뒤에 실행, 15초에 한 번씩 요청 보내고 3초 이내 응답이 와야 건강한 것으로 판단
# 만일 3초 이내 응답이 오지 않는다면 재요청 횟수 5번
# 응답이 오지 않으면 서버는 아예 내린다

ENTRYPOINT ["sh", "-c", "exec java $JVM_OPTS -jar ${PROJECT_NAME}-${PROJECT_VERSION}.jar"]
# 반드시 실행되어야 할 명령어
