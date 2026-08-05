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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepository;

    @Test
    @DisplayName("타입으로 PUBLIC 채널 목록을 조회한다")
    void findAllByType_success_public() {
        Channel publicChannel = new Channel("general", "public channel", null, ChannelType.PUBLIC);
        Channel privateChannel = new Channel(null, null, null, ChannelType.PRIVATE);
        channelRepository.saveAll(List.of(publicChannel, privateChannel));

        List<Channel> result = channelRepository.findAllByType(ChannelType.PUBLIC).stream().toList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ChannelType.PUBLIC);
        assertThat(result.get(0).getName()).isEqualTo("general");
    }

    @Test
    @DisplayName("해당 타입의 채널이 없으면 빈 목록을 반환한다")
    void findAllByType_empty() {
        Channel privateChannel = new Channel(null, null, null, ChannelType.PRIVATE);
        channelRepository.save(privateChannel);

        List<Channel> result = channelRepository.findAllByType(ChannelType.PUBLIC).stream().toList();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("채널 목록을 이름 기준 오름차순으로 페이징 조회한다")
    void findAll_withPagingAndSort() {
        Channel notice = new Channel("notice", "notice channel", null, ChannelType.PUBLIC);
        Channel general = new Channel("general", "general channel", null, ChannelType.PUBLIC);
        Channel random = new Channel("random", "random channel", null, ChannelType.PUBLIC);
        channelRepository.saveAll(List.of(notice, general, random));

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("name").ascending());

        Page<Channel> result = channelRepository.findAll(pageRequest);

        assertThat(result.getContent())
                .extracting(Channel::getName)
                .containsExactly("general", "notice");
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}