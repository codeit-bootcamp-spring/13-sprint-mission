# --- build ---
# set base image with setting nickname in this job
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# copy files assosiate gradle first
# make image cache for build time
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
# run command for set environment cache image
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# copy file(src) to destination(./src)
# files in .dockerignore will not copy in container
COPY src ./src
# build bootable jar file = /app/test
RUN ./gradlew bootJar -x test


# --- runner ---
FROM eclipse-temurin:17-jdk AS app
WORKDIR /app

# update and install dependency.
# - curl : healthcheck with Actuator
# apt cache remove
RUN apt-get update \
#    && apt-get install -y --no-install-recommands curl \
    && rm -rf /var/lib/apt/lists/*

# copy bootjar file( = /app/build/libs/sprintlog-boot-0.0.1-SNAPSHOT (default) ) to app.jar
COPY --from=build /app/build/libs/discodeit-0.0.1-SNAPSHOT.jar app.jar

# set timezone for logging
ENV TZ=Asia/Seoul

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8

# jvm running options for script(gradle, start.ch, ...)
# default is "". changable when container run
ENV JVM_OPTS=""

# notice export Port is 80
EXPOSE 80

# running bootjar file with options
ENTRYPOINT ["bash","-c","java $JVM_OPTS -jar app.jar"]


