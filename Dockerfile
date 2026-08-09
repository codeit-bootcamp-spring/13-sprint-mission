# ------1. build 스테이지
FROM amazoncorretto:17 AS build
WORKDIR /app

RUN yum install -y findutils && yum clean all

# 의존성 먼저: 빌드 스크립트/래퍼만 복사해 의존성 다운로드를 캐시 레이어로 고정
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

# 소스 복사 후 실행 가능 jar 빌드 (테스트는 CI 에서, 이미지 빌드에선 제외)
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ---- ② run 스테이지 ----
FROM amazoncorretto:17
WORKDIR /app

ENV PROJECT_NAME=discodeit PROJECT_VERSION=1.2-M8 JVM_OPTS="" SERVER_PORT=80

COPY --from=build /app/build/libs/discodeit-1.2-M8.jar ./

EXPOSE 80

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar ${PROJECT_NAME}-${PROJECT_VERSION}.jar"]