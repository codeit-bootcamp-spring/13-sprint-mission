package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private BasicChannelService channelService;

    @Nested
    @DisplayName("채널 생성")
    class CreateTest {

        @Test
        @DisplayName("PUBLIC 채널 생성에 성공한다")
        void createSuccess() {
            // given
            CreateChannelRequest request =
                    org.mockito.Mockito.mock(CreateChannelRequest.class);

            Channel savedChannel =
                    org.mockito.Mockito.mock(Channel.class);

            ChannelDto expectedResponse =
                    org.mockito.Mockito.mock(ChannelDto.class);

            given(request.getName()).willReturn("일반 채널");
            given(request.getDescription()).willReturn("일반 채널 설명");

            given(channelRepository.save(any(Channel.class)))
                    .willReturn(savedChannel);

            given(channelMapper.toDto(savedChannel))
                    .willReturn(expectedResponse);

            // when
            ChannelDto result = channelService.create(request);

            // then
            assertThat(result).isSameAs(expectedResponse);

            then(channelRepository)
                    .should()
                    .save(any(Channel.class));

            then(channelMapper)
                    .should()
                    .toDto(savedChannel);
        }
    }

    @Nested
    @DisplayName("채널 조회")
    class FindTest {

        @Test
        @DisplayName("채널 조회에 성공한다")
        void findSuccess() {
            // given
            UUID channelId = UUID.randomUUID();

            Channel channel =
                    org.mockito.Mockito.mock(Channel.class);

            ChannelDto expectedResponse =
                    org.mockito.Mockito.mock(ChannelDto.class);

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(channelMapper.toDto(channel))
                    .willReturn(expectedResponse);

            // when
            ChannelDto result = channelService.find(channelId);

            // then
            assertThat(result).isSameAs(expectedResponse);

            then(channelRepository)
                    .should()
                    .findById(channelId);
        }

        @Test
        @DisplayName("존재하지 않는 채널을 조회하면 실패한다")
        void findFailWhenChannelNotFound() {
            // given
            UUID channelId = UUID.randomUUID();

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> channelService.find(channelId))
                    .isInstanceOf(ChannelNotFoundException.class);

            then(channelMapper)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("채널 수정")
    class UpdateTest {

        @Test
        @DisplayName("채널 수정에 성공한다")
        void updateSuccess() {
            // given
            UUID channelId = UUID.randomUUID();

            UpdateChannelRequest request =
                    org.mockito.Mockito.mock(UpdateChannelRequest.class);

            Channel channel =
                    org.mockito.Mockito.mock(Channel.class);

            ChannelDto expectedResponse =
                    org.mockito.Mockito.mock(ChannelDto.class);

            given(request.getName()).willReturn("수정된 채널");
            given(request.getDescription()).willReturn("수정된 설명");

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(channel.getType())
                    .willReturn(ChannelType.PUBLIC);

            given(channelMapper.toDto(channel))
                    .willReturn(expectedResponse);

            // when
            ChannelDto result =
                    channelService.update(channelId, request);

            // then
            assertThat(result).isSameAs(expectedResponse);

            then(channel)
                    .should()
                    .update(
                            ChannelType.PUBLIC,
                            "수정된 채널",
                            "수정된 설명"
                    );

            then(channelMapper)
                    .should()
                    .toDto(channel);
        }

        @Test
        @DisplayName("존재하지 않는 채널을 수정하면 실패한다")
        void updateFailWhenChannelNotFound() {
            // given
            UUID channelId = UUID.randomUUID();

            UpdateChannelRequest request =
                    org.mockito.Mockito.mock(UpdateChannelRequest.class);

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> channelService.update(channelId, request)
            ).isInstanceOf(ChannelNotFoundException.class);

            then(channelMapper)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("채널 삭제")
    class DeleteTest {

        @Test
        @DisplayName("채널 삭제에 성공한다")
        void deleteSuccess() {
            // given
            UUID channelId = UUID.randomUUID();

            Channel channel =
                    org.mockito.Mockito.mock(Channel.class);

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            // when
            channelService.delete(channelId);

            // then
            then(channelRepository)
                    .should()
                    .delete(channel);
        }

        @Test
        @DisplayName("존재하지 않는 채널을 삭제하면 실패한다")
        void deleteFailWhenChannelNotFound() {
            // given
            UUID channelId = UUID.randomUUID();

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> channelService.delete(channelId)
            ).isInstanceOf(ChannelNotFoundException.class);

            then(channelRepository)
                    .should(never())
                    .delete(any(Channel.class));
        }
    }
}