FROM amazoncorretto:17 AS builder

WORKDIR /app

# 수정: Gradle을 실행하는 builder 단계에 설치해야 합니다.
RUN yum install -y findutils \
    && yum clean all

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew \
    && ./gradlew clean build --no-daemon

FROM amazoncorretto:17

WORKDIR /app

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""

COPY --from=builder /app/build/libs/discodeit-1.2-M8.jar /app/discodeit-1.2-M8.jar

EXPOSE 80

CMD ["sh", "-c", "exec java $JVM_OPTS -jar ${PROJECT_NAME}-${PROJECT_VERSION}.jar"]