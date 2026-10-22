
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew clean bootJar --no-daemon

RUN find build/libs -maxdepth 1 -name '*.jar' \
    ! -name '*-plain.jar' \
    -exec cp {} /app/app.jar \;

# --------- run 스테이지 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

ENV JVM_OPTS=""

COPY --from=build /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JVM_OPTS -jar app.jar"]