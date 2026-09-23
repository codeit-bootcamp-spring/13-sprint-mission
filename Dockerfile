# === build 스테이지 =============================================
FROM amazoncorretto:17 AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN yum install -y findutils && yum clean all && chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew clean build -x test --no-daemon

# === run 스테이지 =============================================

FROM amazoncorretto:17-alpine

WORKDIR /app

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""
ENV SPRING_PROFILES_ACTIVE=prod


COPY --from=build /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar ./


EXPOSE 80

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar ${PROJECT_NAME}-${PROJECT_VERSION}.jar"]