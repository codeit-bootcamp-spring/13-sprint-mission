package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicReadStatusServiceTest {

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ReadStatusMapper readStatusMapper;

    @InjectMocks
    private BasicReadStatusService readStatusService;

    @Nested
    @DisplayName("ReadStatus 생성")
    class CreateReadStatus {

        @Test
        @DisplayName("사용자와 채널이 존재하고 중복이 아니면 생성")
        void create_success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());

            User user = mock(User.class);
            Channel channel = mock(Channel.class);
            ReadStatus savedReadStatus = mock(ReadStatus.class);
            ReadStatusDto expected = mock(ReadStatusDto.class);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(readStatusRepository.existsByUserIdAndChannelId(
                    userId,
                    channelId
            )).willReturn(false);

            given(readStatusRepository.save(any(ReadStatus.class)))
                    .willReturn(savedReadStatus);

            given(readStatusMapper.toDto(savedReadStatus))
                    .willReturn(expected);

            // when
            ReadStatusDto result = readStatusService.createReadStatus(request);

            // then
            assertThat(result)
                    .isSameAs(expected);

            then(readStatusRepository).should()
                    .existsByUserIdAndChannelId(userId, channelId);

            then(readStatusRepository).should()
                    .save(any(ReadStatus.class));

            then(readStatusMapper).should()
                    .toDto(savedReadStatus);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
        void create_fail_no_user() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> readStatusService.createReadStatus(request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("채널이 존재하지 않으면 예외가 발생")
        void create_fail_no_channel() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());

            User user = mock(User.class);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> readStatusService.createReadStatus(request))
                    .isInstanceOf(ChannelNotFoundException.class);
        }

        @Test
        @DisplayName("동일한 사용자와 채널의 ReadStatus가 존재하면 예외가 발생")
        void create_fail_duplicate() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());

            User user = mock(User.class);
            Channel channel = mock(Channel.class);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(readStatusRepository.existsByUserIdAndChannelId(
                    userId,
                    channelId
            )).willReturn(true);

            // when & then
            assertThatThrownBy(() -> readStatusService.createReadStatus(request))
                    .isInstanceOf(ReadStatusAlreadyExistsException.class);

            then(readStatusRepository).should(never())
                    .save(any(ReadStatus.class));
        }
    }

    @Nested
    @DisplayName("ReadStatus 단건 조회")
    class GetReadStatus {

        @Test
        @DisplayName("ReadStatus가 존재하면 DTO로 반환")
        void get_success() {
            // given
            UUID readStatusId = UUID.randomUUID();

            ReadStatus readStatus = mock(ReadStatus.class);
            ReadStatusDto expected = mock(ReadStatusDto.class);

            given(readStatusRepository.findById(readStatusId))
                    .willReturn(Optional.of(readStatus));

            given(readStatusMapper.toDto(readStatus))
                    .willReturn(expected);

            // when
            ReadStatusDto result = readStatusService.getReadStatus(readStatusId);

            // then
            assertThat(result).isSameAs(expected);

            then(readStatusRepository).should()
                    .findById(readStatusId);

            then(readStatusMapper).should()
                    .toDto(readStatus);
        }

        @Test
        @DisplayName("ReadStatus가 존재하지 않으면 예외가 발생")
        void get_fail() {
            // given
            UUID readStatusId = UUID.randomUUID();

            given(readStatusRepository.findById(readStatusId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> readStatusService.getReadStatus(readStatusId))
                    .isInstanceOf(ReadStatusNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("사용자별 ReadStatus 목록 조회")
    class GetReadStatusesByUserId {

        @Test
        @DisplayName("사용자 ID에 해당하는 ReadStatus 목록을 DTO로 반환")
        void get_success() {
            // given
            UUID userId = UUID.randomUUID();

            ReadStatus readStatus1 = mock(ReadStatus.class);
            ReadStatus readStatus2 = mock(ReadStatus.class);

            ReadStatusDto dto1 = mock(ReadStatusDto.class);
            ReadStatusDto dto2 = mock(ReadStatusDto.class);

            given(readStatusRepository.findAllByUserId(userId))
                    .willReturn(List.of(readStatus1, readStatus2));

            given(readStatusMapper.toDto(readStatus1))
                    .willReturn(dto1);

            given(readStatusMapper.toDto(readStatus2))
                    .willReturn(dto2);

            // when
            List<ReadStatusDto> result = readStatusService.getReadStatusesByUserId(userId);

            // then
            assertThat(result)
                    .containsExactly(dto1, dto2);

            then(readStatusRepository).should()
                    .findAllByUserId(userId);

            then(readStatusMapper).should()
                    .toDto(readStatus1);

            then(readStatusMapper).should()
                    .toDto(readStatus2);
        }

        @Test
        @DisplayName("ReadStatus가 없으면 빈 목록을 반환")
        void get_no_list() {
            // given
            UUID userId = UUID.randomUUID();

            given(readStatusRepository.findAllByUserId(userId))
                    .willReturn(List.of());

            // when
            List<ReadStatusDto> result = readStatusService.getReadStatusesByUserId(userId);

            // then
            assertThat(result)
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("ReadStatus 수정")
    class UpdateReadStatus {

        @Test
        @DisplayName("ReadStatus가 존재하면 마지막 확인 시간을 수정")
        void update_success() {
            // given
            UUID readStatusId = UUID.randomUUID();

            ReadStatusUpdateRequest request = mock(ReadStatusUpdateRequest.class);

            ReadStatus readStatus = mock(ReadStatus.class);
            ReadStatusDto expected = mock(ReadStatusDto.class);

            given(readStatusRepository.findById(readStatusId))
                    .willReturn(Optional.of(readStatus));

            given(readStatusMapper.toDto(readStatus))
                    .willReturn(expected);

            // when
            ReadStatusDto result = readStatusService.updateReadStatus(
                    readStatusId,
                    request
            );

            // then
            assertThat(result)
                    .isSameAs(expected);

            then(readStatus).should()
                    .updateLastReadAt();

            then(readStatusMapper).should()
                    .toDto(readStatus);
        }

        @Test
        @DisplayName("ReadStatus가 존재하지 않으면 예외가 발생")
        void update_fail() {
            // given
            UUID readStatusId = UUID.randomUUID();

            ReadStatusUpdateRequest request = mock(ReadStatusUpdateRequest.class);

            given(readStatusRepository.findById(readStatusId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> readStatusService.updateReadStatus(
                    readStatusId,
                    request
            )).isInstanceOf(ReadStatusNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("ReadStatus 삭제")
    class DeleteReadStatus {

        @Test
        @DisplayName("ReadStatus가 존재하면 삭제")
        void delete_success() {
            // given
            UUID readStatusId = UUID.randomUUID();

            ReadStatus readStatus = mock(ReadStatus.class);

            given(readStatusRepository.findById(readStatusId))
                    .willReturn(Optional.of(readStatus));

            given(readStatus.getId())
                    .willReturn(readStatusId);

            // when
            readStatusService.deleteReadStatus(readStatusId);

            // then
            then(readStatusRepository).should()
                    .deleteById(readStatusId);
        }

        @Test
        @DisplayName("ReadStatus가 존재하지 않으면 예외가 발생")
        void delete_fail() {
            // given
            UUID readStatusId = UUID.randomUUID();

            given(readStatusRepository.findById(readStatusId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> readStatusService.deleteReadStatus(readStatusId))
                    .isInstanceOf(ReadStatusNotFoundException.class);

            then(readStatusRepository).should(never())
                    .deleteById(readStatusId);
        }
    }
}