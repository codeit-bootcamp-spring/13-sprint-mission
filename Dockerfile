FROM amazoncorretto:17 AS builder

RUN yum install -y findutils && yum clean all

WORKDIR /app

COPY . .

RUN sed -i 's/\r$//' gradlew
RUN chmod +x gradlew
RUN ./gradlew --no-daemon clean bootJar -x test

FROM amazoncorretto:17

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]