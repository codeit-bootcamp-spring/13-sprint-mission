package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void setUp() {
        Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
        em.persist(channel);

        em.persist(channel);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("채널명 존재 확인 - 존재함")
    void existsByName_true() {
        assertThat(channelRepository.existsByName("공지")).isTrue();
    }

    @Test
    @DisplayName("채널명 존재 확인 - 존재하지 않음")
    void existsByName_false() {

        assertThat(channelRepository.existsByName("소개")).isFalse();

    }
}