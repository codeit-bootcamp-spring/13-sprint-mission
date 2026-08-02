package com.sprint.mission.discodeit;

import com.jayway.jsonpath.JsonPath;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("total test")
public class ApplicationTest {

    @LocalServerPort
    int port; // 서버가 실제로 배정받은 포트 번호를 이 필드에 주입.

    @Autowired
    TestRestTemplate rest; // 진짜 HTTP 요청을 보내는 클라이언트를 주입받습니다.

    @Autowired
    UserRepository  userRepository;
    @Autowired
    ChannelRepository channelRepository;
    @Autowired
    MessageRepository messageRepository;

    private String base; // 공통 기본 url 담아놓을 용도.

    // build폴더 아래에 폴더를 세팅하면 gitignore 대상이고, gradle clean 할 때 알아서 지워진다.
    static final Path uploadDir = Paths.get("./build/test-uploads");

    void setUp(String domain) {
        userRepository.deleteAll();
        channelRepository.deleteAll();
        messageRepository.deleteAll(); // 샘플 데이터를 비워 시작 상태를 일정하게.
        base = "http://localhost:" + port + "/api/" + domain ;

        // 매 테스트마다 업로드 폴더를 비운다.
        File[] files = uploadDir.toFile().listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    @Nested
    @DisplayName("User domain test")
    class UserDomainTest {

        private ResponseEntity<String> createRequest(String data) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(data, headers);

            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("userCreateRequest", entity);

            return rest.postForEntity(base, parts, String.class);
        }

        @Test
        @DisplayName("user create and query")
        void createAndQuery() {
            setUp("users");
            ResponseEntity<String> created = createRequest(
                    "{\"username\" : \"김숙희\",\"email\":\"ksk@email.com\",\"password\":\"password\"}"
            );

            assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            created.getHeaders(); // [content-type:"application/json", date:"Sun, 02 Aug 2026 10:12:52 GMT", transfer-encoding:"chunked"]

            ResponseEntity<String> fetched = rest.getForEntity(
                    "http://localhost:" + port + "/api/users", String.class
            );
            assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 배열 형식이라 json 리딩 포맷을 $[0] 같은 배열포맷으로 변환.
            assertThat((String)JsonPath.read(fetched.getBody(),"$[0].username")).isEqualTo("김숙희");

        }
    }



}
