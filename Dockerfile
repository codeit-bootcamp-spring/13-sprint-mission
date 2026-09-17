FROM amazoncorretto:17-jdk AS builder

RUN dnf install -y findutils && dnf clean all

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar -x test

FROM amazoncorretto:17-headless

RUN dnf install -y curl-minimal && dnf clean all

WORKDIR /app

ARG PROJECT_NAME=discodeit
ARG PROJECT_VERSION=1.2-M8

ENV PROJECT_NAME=${PROJECT_NAME}
ENV PROJECT_VERSION=${PROJECT_VERSION}
ENV JVM_OPTS=""


COPY --from=builder /workspace/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar ${PROJECT_NAME}-${PROJECT_VERSION}.jar

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
  CMD curl -fsS http://localhost/ || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JVM_OPTS -jar $PROJECT_NAME-$PROJECT_VERSION.jar"]
