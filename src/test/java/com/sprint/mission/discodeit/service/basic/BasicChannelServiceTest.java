package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.isNull;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.channel.ChannelPublicCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicChannelService 단위 테스트")
class BasicChannelServiceTest {

  @Mock
  ChannelRepository repository;
  @Mock
  ReadStatusRepository readStatusRepository;
  @Mock
  MessageRepository messageRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  ChannelMapper mapper;
  @InjectMocks
  BasicChannelService service;

  @Test
  @DisplayName("공개 채널을 생성")
  void createPublicChannel() {
    ChannelResponse response = mock(ChannelResponse.class);
    given(repository.findAll()).willReturn(List.of());
    given(mapper.toDto(any(Channel.class), eq(List.of()), isNull())).willReturn(response);

    ChannelResponse result =
        service.createPublicChannel(new ChannelPublicCreateRequest("공개 채널", "공개채널 입니다."));

    assertThat(result).isEqualTo(response);
    then(repository).should().save(any(Channel.class));
  }

  @Test
  @DisplayName("채널명이 중복되면 공개 채널 생성에 실패")
  void rejectDuplicateName() {
    Channel channel = Channel.publicChannelBuilder()
        .type(ChannelType.PUBLIC)
        .name("중복채널")
        .build();
    given(repository.findAll()).willReturn(List.of(channel));

    assertThatThrownBy(() ->
        service.createPublicChannel(new ChannelPublicCreateRequest("중복채널", "asdf")))
        .isInstanceOf(ChannelAlreadyExistsException.class);
  }
}
