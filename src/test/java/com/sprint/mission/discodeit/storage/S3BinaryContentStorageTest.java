package com.sprint.mission.discodeit.storage;


import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import com.sprint.mission.discodeit.config.S3Config;
import com.sprint.mission.discodeit.config.S3Property;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;


import static org.assertj.core.api.Assertions.*;

@DisplayName("S3 Storage Test")
@ActiveProfiles("test")
@Slf4j
public class S3BinaryContentStorageTest {
    static final String BUCKET = "discodeit-binary-content-storage-ojg";

    static S3MockContainer s3Mock; // 진짜 S3처럼 행동하는 로컬 서버 컨테이너
    static S3BinaryContentStorage s3BinaryContentStorage;
    static S3Client s3Client;
    static S3Presigner s3Presigner;

    @BeforeAll
    static void startMock() {
        s3Mock = new S3MockContainer("latest");
        s3Mock.start();

        S3Property props = new S3Property();
        props.setBucket(BUCKET);
        props.setRegion("ap-northeast-2");
        props.setEndpoint(s3Mock.getHttpEndpoint());   // 목 주소
        props.setPresignTime(10);

        S3Config config = new S3Config();
        s3Client = config.s3Client(props);
        s3Presigner = config.s3Presigner(props);

        s3BinaryContentStorage = new S3BinaryContentStorage(s3Presigner,s3Client,props);

        s3Client.createBucket(b -> b.bucket(BUCKET));
    }

    @AfterAll
    static void stop(){if (s3Mock != null) s3Mock.stop();}

    private BinaryContentDto getBinaryContentDto(UUID id, String name,Long size, String type) {
        return new BinaryContentDto(id, name, size, type, null);
    }


    @Test
    @DisplayName("s3 file test")
    void test() throws IOException, InterruptedException {
        // given
        UUID id = UUID.randomUUID();
        byte[] content = "test byte for this content".getBytes();
        BinaryContentDto dto = getBinaryContentDto(id,"testfile.txt",(long)content.length,"text/plain");

        // when
        // then
        UUID res = s3BinaryContentStorage.put(id,content);
        assertThat(res).isEqualTo(id);

        log.info("S3 upload - id = {}",id);

        ResponseEntity<?> rent =  s3BinaryContentStorage.download(dto);

        URI url = rent.getHeaders().getLocation();

        log.info("S3 download url returned - {}", rent.getHeaders().getLocation());

        HttpResponse<byte[]> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(url).build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        assertThat(response.body()).isEqualTo(content);
        log.debug("S3 download uri test\norigin = {}\nsaved = {}",content,response.body());
    }
}
