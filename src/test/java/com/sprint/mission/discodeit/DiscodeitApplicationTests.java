package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.config.AdminInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class DiscodeitApplicationTests {

	@MockitoBean
	private AdminInitializer adminInitializer;

	@Test
	void contextLoads() {
	}

}
