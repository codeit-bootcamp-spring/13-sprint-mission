FROM amazoncorretto:17

WORKDIR /app

RUN yum install -y findutils

COPY . .

RUN chmod +x gradlew

RUN ./gradlew clean bootJar -x test

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""

RUN cp build/libs/discodeit-1.7.0.jar \
    build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar

EXPOSE 80

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar"]