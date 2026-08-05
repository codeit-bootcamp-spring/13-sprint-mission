# 베이스 이미지: Linux OS와 Java17을 기본으로 깔아준다.
# AWS로 빌린 컴퓨터 안에 도커 컨테이너 공간을 만들고 그 안에 다운한다.-> 컴퓨터 자체에 때려 박는게 아님.
#컴퓨터 자체가 아니라 도커 컨테이너에 다운하는 이유-> 나중에 갈아 끼우기 편해서.
FROM amazoncorretto:17

# 컨테이너 내부 작업 디렉토리 설정 (이후 명령어들은 이 경로 기준으로 실행됨)
#AWS 컴퓨터 안에 독커 컨테이너 안에 app폴더를 만들고 이후 명령어들은 이 폴더 내부에 적용 된다.
WORKDIR /app

# 현재 디렉토리(프로젝트 전체)를 컨테이너의 /app으로 복사 (.dockerignore에 있는 건 제외됨)
COPY . .

# Gradle Wrapper로 프로젝트 빌드 (-x test: 테스트는 이미지 빌드 시 생략)
# [동작 과정]
# 1. Gradle이 프로젝트에 필요한 외부 라이브러리들을 다운로드한다.
# 2. 자바 컴파일러(javac)가 모든 .java를 컴파일하여 .class 파일로 변환한다.
# 3. Gradle이 .class 파일, 설정 파일, 외부 라이브러리들을 모아 하나의 .jar 파일로 압축/포장한다.
#
# [.jar로 만드는 이유]
# 컨테이너 안에서 .java 원본 소스로 매번 바로 실행할 수도 있지만,
# 컨테이너가 켜질 때마다 매번 컴파일하고 라이브러리를 다운로드하면 실행에 수 분 이상 걸린다.
# 따라서 빌드 시점에 모든 컴파일과 라이브러리 포함을 끝낸 실행 완제품(.jar)을 미리 만들어 두고,
# 컨테이너 실행 시에는 JVM이 이 .jar 파일 하나만 즉시 띄우도록 하여 빠른 속도와 안정성을 확보한다.
RUN ./gradlew clean build -x test

# 실행할 jar 파일명을 조합하는 데 쓰일 프로젝트 이름
ENV PROJECT_NAME=discodeit

# 실행할 jar 파일명을 조합하는 데 쓰일 프로젝트 버전 Build.gradle에 명시한 버전과 동일해야함
ENV PROJECT_VERSION=1.2-M8

# JVM 실행 옵션 (기본값 없음, 필요시 실행 시점에 주입)
#AWS의 컴퓨터 옵션이 도커 컨테이너가 실행 될 때 쉘을 통해 자동으로 할당됨.
ENV JVM_OPTS=""

# 컨테이너가 외부에 노출할 포트 명시(문서화 역할, 실제 포트 매핑은 run/compose에서 함)
EXPOSE 80

# 컨테이너 실행 시(전원 켜질 때) 자동으로 돌아가는 최종 명령어
# [sh -c] 셸을 통해 $JVM_OPTS, ${PROJECT_NAME} 등의 변수를 실제 문자열값으로 바꿔 대입한 후 java -jar로 실행시킴
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar"]