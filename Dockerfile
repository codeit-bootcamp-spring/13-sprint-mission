# =========================================================
# 1. 빌드 단계
# =========================================================

FROM amazoncorretto:17 AS builder

WORKDIR /app

# Gradle Wrapper 실행에 필요한 xargs 설치
RUN dnf install -y findutils && dnf clean all

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY admin ./admin
COPY src ./src

RUN chmod +x gradlew

# Gradle Wrapper를 사용해 실행 가능한 JAR 빌드
RUN ./gradlew clean bootJar --no-daemon


# =========================================================
# 2. 실행 단계
# =========================================================

FROM amazoncorretto:17

WORKDIR /app

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""
ENV SERVER_PORT=80

COPY --from=builder /app/build/libs/discodeit-1.2-M8.jar ./

EXPOSE 80

CMD ["sh", "-c", "java $JVM_OPTS -jar ${PROJECT_NAME}-${PROJECT_VERSION}.jar"]