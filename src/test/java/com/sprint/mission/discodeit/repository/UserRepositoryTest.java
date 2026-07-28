package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

  @Autowired
  private DataSource dataSource;

  @Test
  void 테스트_데이터소스는_H2를_사용한다() throws Exception {
    String databaseName =
        dataSource.getConnection()
            .getMetaData()
            .getDatabaseProductName();

    assertThat(databaseName).isEqualTo("H2");
  }
}