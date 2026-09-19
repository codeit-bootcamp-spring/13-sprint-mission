package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicMessageService 단위 테스트")
class BasicMessageServiceTest {

  @Mock
  MessageRepository repository;
  @Mock
  ChannelRepository channelRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  BinaryContentRepository contentRepository;
  @Mock
  BinaryContentStorage storage;
  @Mock
  MessageMapper mapper;
  @Mock
  PageResponseMapper pageMapper;
  @InjectMocks
  BasicMessageService service;

  @Test
  @DisplayName("메시지를 정상적으로 생성한다")
  void 메시지_생성() {
    Channel channel = Channel.publicChannelBuilder()
        .type(ChannelType.PUBLIC)
        .name("공개 채널")
        .build();
    User author = User.builder()
        .username("김김김")
        .email("asdf@test.com")
        .password("password")
        .role(Role.USER)
        .build();
    MessageCreateRequest request =
        new MessageCreateRequest("안녕하세요", author.getId(), channel.getId());
    MessageResponse response = new MessageResponse(
        java.util.UUID.randomUUID(), null, null, "안녕하세요", channel.getId(), null, List.of());
    given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
    given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
    given(mapper.toDto(any(Message.class))).willReturn(response);

    MessageResponse result = service.createMessage(request, null);

    assertThat(result).isEqualTo(response);
    then(repository).should().save(any(Message.class));
  }

  @Test
  @DisplayName("채널이 없으면 메시지 생성에 실패한다")
  void 채널이_없으면_메시지_생성에_실패() {
    java.util.UUID channelId = java.util.UUID.randomUUID();
    MessageCreateRequest request =
        new MessageCreateRequest("안녕하세요", java.util.UUID.randomUUID(), channelId);
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.createMessage(request, null))
        .isInstanceOf(ChannelNotFoundException.class);
  }
}
