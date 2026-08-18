[![codecov](https://codecov.io/gh/totobabo01/13-sprint-mission/graph/badge.svg?token=6KJJQXJX8B)](https://codecov.io/gh/totobabo01/13-sprint-mission)
# Discodeit

Spring Boot 기반의 채팅 애플리케이션 프로젝트입니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.14
- Gradle
- Spring Data JPA
- PostgreSQL
- AWS S3
- AWS RDS
- AWS ECS Fargate
- Amazon ECR Public
- Docker
- GitHub Actions
- JaCoCo
- Codecov

## 주요 기능

- 사용자 관리
- 공개/비공개 채널 관리
- 메시지 생성, 조회, 수정, 삭제
- 메시지 커서 페이지네이션
- 이미지 및 파일 첨부
- AWS S3 기반 BinaryContent 저장
- Presigned URL 기반 파일 다운로드
- Spring Boot Actuator 상태 모니터링

## AWS 배포 구성

애플리케이션은 AWS 환경에 다음과 같이 배포합니다.

- ECS Fargate를 통한 컨테이너 실행
- Amazon RDS PostgreSQL 데이터베이스 사용
- Amazon S3를 통한 첨부파일 저장
- Amazon ECR Public을 통한 Docker 이미지 관리
- ECS Task Role을 통한 S3 접근
- GitHub Actions OIDC를 통한 AWS 인증

## CI/CD

### CI

`main` 브랜치를 대상으로 Pull Request가 생성되거나 `main` 브랜치에 코드가 Push되면 GitHub Actions CI가 실행됩니다.

CI 과정:

1. 소스 코드 Checkout
2. Java 17 설정
3. Gradle 환경 설정
4. 테스트 실행
5. JaCoCo 테스트 커버리지 리포트 생성
6. Codecov 커버리지 업로드

현재 테스트 커버리지는 Codecov를 통해 관리합니다.

### CD

GitHub Actions를 이용하여 Docker 이미지 빌드 및 AWS ECS 배포를 자동화합니다.

배포 과정:

1. GitHub Actions 실행
2. GitHub OIDC를 통한 AWS IAM Role 인증
3. Docker 이미지 빌드
4. Amazon ECR Public에 이미지 Push
5. 새로운 ECS Task Definition Revision 등록
6. ECS Service 업데이트
7. Fargate Task 재배포

## 테스트

전체 테스트 실행:

bash
./gradlew clean test