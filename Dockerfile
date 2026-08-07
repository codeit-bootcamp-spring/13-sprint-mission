# 1단계: 애플리케이션 빌드
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew \
    && ./gradlew clean bootJar --no-daemon


# 2단계: 실행 전용 이미지
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/discodeit-1.2-M8.jar app.jar

ENV JVM_OPTS=""
ENV PORT=80

EXPOSE 80

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]