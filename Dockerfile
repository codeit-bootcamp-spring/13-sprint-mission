# Amazon Corretto 17을 베이스 이미지로 사용 (Amazon Linux 2 기반 - apt-get 지원)
FROM amazoncorretto:17-al2

# 컨테이너 내 작업 디렉토리를 /app으로 설정
WORKDIR /app

# 필요한 패키지 설치 (xargs 포함된 findutils 설치, yum 캐시 삭제로 이미지 크기 최소화)
RUN yum update -y && yum install -y findutils && yum clean all

# 로컬의 프로젝트 파일을 컨테이너의 /app으로 복사 (.dockerignore에 정의된 파일 제외)
COPY . .

# 환경 변수: 실행할 jar 파일의 이름을 구성하기 위해 프로젝트 정보 설정
ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""

# Gradle Wrapper를 사용하여 애플리케이션 빌드 (테스트 스킵)
RUN ./gradlew clean build -x test --no-daemon

# 컨테이너가 리스닝할 포트를 80번으로 노출
EXPOSE 80

# 컨테이너 시작 시 실행될 명령어 (jar 파일명을 환경 변수로 동적으로 구성, JSON 형식으로 신호 처리 개선)
ENTRYPOINT ["sh", "-c", "exec java ${JVM_OPTS} -jar /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar"]