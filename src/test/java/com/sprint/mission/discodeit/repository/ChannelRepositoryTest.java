package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepository;

    @Test
    @DisplayName("채널을 이름순으로 2개씩 조회한다.")
    void findAll_pagingAndSorting_success() {
        // given
        channelRepository.saveAll(List.of(
                new Channel("공지채널", "전체 공지 채널입니다.", ChannelType.PUBLIC),
                new Channel("일반채널", "소통하는 공간입니다.", ChannelType.PUBLIC),
                new Channel("고독채널", "사지만 올릴 수 있는 채널입니다.", ChannelType.PUBLIC)
        ));

        PageRequest pageable = PageRequest.of(
                0, 2, Sort.by(Sort.Direction.ASC, "name"));

        // when
        Page<Channel> result = channelRepository.findAll(pageable);

        // then
        assertThat(result.getContent())
                .extracting(Channel::getName)
                .containsExactly("고독채널", "공지채널");

        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("데이터가 없으면 빈 페이자를 반환한다,")
    void findAii_paging_empty() {
        // given
        PageRequest pageable = PageRequest.of(0, 2);

        // when
        Page<Channel> result = channelRepository.findAll(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }
}
