package com.sprint.mission.discodeit.storage.s3;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

@Slf4j
public class AWSS3Test {

    // AWS S3 클라이언트 인스턴스
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String region;
    private static final String TEST_FILE_PATH = "test-file.txt";
    private static final String S3_KEY = "test-uploads/test-file.txt";

    public AWSS3Test() {
        // Properties 객체 생성 (.env 파일 읽기용)
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(".env")) {
            properties.load(input);
        } catch (IOException e) {
            log.error(".env 파일을 읽을 수 없습니다", e);
            throw new RuntimeException(".env 파일을 읽을 수 없습니다", e);
        }

        String accessKey = properties.getProperty("AWS_S3_ACCESS_KEY");
        String secretKey = properties.getProperty("AWS_S3_SECRET_KEY");
        this.region = properties.getProperty("AWS_S3_REGION", "ap-northeast-2");
        this.bucketName = properties.getProperty("AWS_S3_BUCKET");

        // 필수 설정 값들이 모두 있는지 검증
        if (accessKey == null || secretKey == null || bucketName == null) {
            throw new RuntimeException(".env 파일에서 필요한 AWS 설정이 없습니다");
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("AWSS3Test 초기화 완료");
    }

    public void testUpload() {
        try {
            // 테스트 파일이 존재하지 않으면 생성
            if (!Files.exists(Paths.get(TEST_FILE_PATH))) {
                String testContent = "AWS S3 업로드 테스트 파일입니다\n" +
                        "생성 시간: " + System.currentTimeMillis();
                // 테스트 파일 생성 및 내용 작성
                Files.write(Paths.get(TEST_FILE_PATH), testContent.getBytes());
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(S3_KEY)
                    .contentType("text/plain")
                    .build();

            // S3 클라이언트를 사용하여 파일 업로드 실행
            s3Client.putObject(putObjectRequest,
                    // 업로드할 로컬 파일 경로 지정
                    Paths.get(TEST_FILE_PATH));

            // 업로드 성공 메시지 출력
            log.info("S3에 파일 업로드 성공: s3://{}/{}", bucketName, S3_KEY);
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패", e);
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }

    public void testDownload() {
        try {
            String downloadedFilePath = "downloaded-file.txt";

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(S3_KEY)
                    .build();

            // S3 클라이언트를 사용하여 파일 다운로드 실행
            s3Client.getObject(getObjectRequest,
                    // 다운로드한 파일을 저장할 로컬 경로 지정
                    ResponseTransformer.toFile(Paths.get(downloadedFilePath)));

            // 다운로드 성공 메시지 출력
            log.info("S3에서 파일 다운로드 성공: {}", downloadedFilePath);
        } catch (Exception e) {
            // 다운로드 실패 시 에러 메시지 출력
            log.error("S3 파일 다운로드 실패", e);
            throw new RuntimeException("S3 파일 다운로드 실패", e);
        }
    }

    public void testCreatePresignedUrl() {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    // 다운로드 요청 설정
                    .getObjectRequest(builder -> builder
                            .bucket(bucketName)
                            .key(S3_KEY)
                            .build())
                    // Presigned URL의 유효 시간 설정 (10분)
                    .signatureDuration(Duration.ofMinutes(10))
                    .build();

            // S3 Presigner를 사용하여 Presigned URL 생성
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            String presignedUrl = presignedRequest.url().toString();

            log.info("Presigned URL 생성 성공");
            // URL 전체는 길어서 마지막 50자만 출력
            log.info("Presigned URL (마지막 50자): {}", presignedUrl.substring(Math.max(0, presignedUrl.length() - 50)));
            // URL 유효 시간 정보 출력
            log.info("URL 유효 시간: 10분");
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패", e);
            throw new RuntimeException("Presigned URL 생성 실패", e);
        }
    }

    /**
     * 메인 메소드
     * AWSS3Test의 모든 테스트를 순서대로 실행합니다
     * @param args 커맨드라인 인자 (사용하지 않음)
     */
    public static void main(String[] args) {
        // AWSS3Test 인스턴스 생성
        AWSS3Test awsS3Test = new AWSS3Test();

        // ========== S3 업로드 테스트 시작 ==========
        log.info("========== S3 업로드 테스트 시작 ==========");
        awsS3Test.testUpload();
        log.info("========== S3 업로드 테스트 완료 ==========\n");

        // ========== S3 다운로드 테스트 시작 ==========
        log.info("========== S3 다운로드 테스트 시작 ==========");
        awsS3Test.testDownload();
        log.info("========== S3 다운로드 테스트 완료 ==========\n");

        // ========== Presigned URL 생성 테스트 시작 ==========
        log.info("========== Presigned URL 생성 테스트 시작 ==========");
        awsS3Test.testCreatePresignedUrl();
        log.info("========== Presigned URL 생성 테스트 완료 ==========\n");

        // 모든 테스트 완료 메시지 출력
        log.info("모든 S3 테스트 완료!");
    }
}