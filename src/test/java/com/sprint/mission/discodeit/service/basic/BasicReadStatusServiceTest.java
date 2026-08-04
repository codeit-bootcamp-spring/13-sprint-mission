package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicReadStatusService 단위 테스트")
class BasicReadStatusServiceTest {

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private BasicReadStatusService readStatusService;

    @Nested
    @DisplayName("읽음 상태 생성")
    class Create {

        @Test
        @DisplayName("정상적인 요청이면 읽음 상태를 생성한다")
        void success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();
            UUID readStatusId = UUID.randomUUID();

            ReadStatusCreateRequest request =
                    org.mockito.Mockito.mock(ReadStatusCreateRequest.class);

            User user = org.mockito.Mockito.mock(User.class);
            Channel channel = org.mockito.Mockito.mock(Channel.class);
            ReadStatus savedReadStatus =
                    org.mockito.Mockito.mock(ReadStatus.class);

            given(request.getUserId()).willReturn(userId);
            given(request.getChannelId()).willReturn(channelId);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(readStatusRepository.findByUser_IdAndChannel_Id(
                    userId,
                    channelId
            )).willReturn(null);

            given(readStatusRepository.save(
                    org.mockito.ArgumentMatchers.any(ReadStatus.class)
            )).willReturn(savedReadStatus);

            stubReadStatus(
                    savedReadStatus,
                    readStatusId,
                    userId,
                    channelId,
                    Instant.now()
            );

            // when
            ReadStatusResponse response =
                    readStatusService.create(request);

            // then
            assertThat(response).isNotNull();

            verify(readStatusRepository)
                    .save(org.mockito.ArgumentMatchers.any(ReadStatus.class));
        }

        @Test
        @DisplayName("이미 읽음 상태가 존재하면 새로 저장하지 않고 기존 값을 반환한다")
        void existingReadStatus() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();
            UUID readStatusId = UUID.randomUUID();

            ReadStatusCreateRequest request =
                    org.mockito.Mockito.mock(ReadStatusCreateRequest.class);

            User user = org.mockito.Mockito.mock(User.class);
            Channel channel = org.mockito.Mockito.mock(Channel.class);
            ReadStatus existing =
                    org.mockito.Mockito.mock(ReadStatus.class);

            given(request.getUserId()).willReturn(userId);
            given(request.getChannelId()).willReturn(channelId);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(readStatusRepository.findByUser_IdAndChannel_Id(
                    userId,
                    channelId
            )).willReturn(existing);

            stubReadStatus(
                    existing,
                    readStatusId,
                    userId,
                    channelId,
                    Instant.now()
            );

            // when
            ReadStatusResponse response =
                    readStatusService.create(request);

            // then
            assertThat(response).isNotNull();

            verify(readStatusRepository, never())
                    .save(org.mockito.ArgumentMatchers.any(ReadStatus.class));
        }

        @Test
        @DisplayName("생성 요청이 null이면 예외가 발생한다")
        void nullRequest() {
            assertThatThrownBy(() ->
                    readStatusService.create(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "읽음 상태 생성 요청은 비어 있을 수 없습니다"
                    );

            verify(userRepository, never())
                    .findById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("사용자 ID가 null이면 예외가 발생한다")
        void nullUserId() {
            // given
            ReadStatusCreateRequest request =
                    org.mockito.Mockito.mock(ReadStatusCreateRequest.class);

            given(request.getUserId()).willReturn(null);

            // when & then
            assertThatThrownBy(() ->
                    readStatusService.create(request)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자 id는 필수입니다");

            verify(userRepository, never())
                    .findById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("채널 ID가 null이면 예외가 발생한다")
        void nullChannelId() {
            // given
            UUID userId = UUID.randomUUID();

            ReadStatusCreateRequest request =
                    org.mockito.Mockito.mock(ReadStatusCreateRequest.class);

            given(request.getUserId()).willReturn(userId);
            given(request.getChannelId()).willReturn(null);

            // when & then
            assertThatThrownBy(() ->
                    readStatusService.create(request)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("채널 id는 필수입니다");

            verify(userRepository, never())
                    .findById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
        void userNotFound() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            ReadStatusCreateRequest request =
                    org.mockito.Mockito.mock(ReadStatusCreateRequest.class);

            given(request.getUserId()).willReturn(userId);
            given(request.getChannelId()).willReturn(channelId);

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    readStatusService.create(request)
            )
                    .isInstanceOf(UserNotFoundException.class);

            verify(channelRepository, never())
                    .findById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("채널이 존재하지 않으면 예외가 발생한다")
        void channelNotFound() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            ReadStatusCreateRequest request =
                    org.mockito.Mockito.mock(ReadStatusCreateRequest.class);

            User user = org.mockito.Mockito.mock(User.class);

            given(request.getUserId()).willReturn(userId);
            given(request.getChannelId()).willReturn(channelId);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    readStatusService.create(request)
            )
                    .isInstanceOf(ChannelNotFoundException.class);

            verify(readStatusRepository, never())
                    .save(org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("읽음 상태 단건 조회")
    class Find {

        @Test
        @DisplayName("존재하는 읽음 상태를 조회한다")
        void success() {
            // given
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            ReadStatus readStatus =
                    org.mockito.Mockito.mock(ReadStatus.class);

            given(readStatusRepository.findById(id))
                    .willReturn(Optional.of(readStatus));

            stubReadStatus(
                    readStatus,
                    id,
                    userId,
                    channelId,
                    Instant.now()
            );

            // when
            ReadStatusResponse response =
                    readStatusService.find(id);

            // then
            assertThat(response).isNotNull();

            verify(readStatusRepository).findById(id);
        }

        @Test
        @DisplayName("조회 ID가 null이면 예외가 발생한다")
        void nullId() {
            assertThatThrownBy(() ->
                    readStatusService.find(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("읽음 상태 id는 필수입니다");

            verify(readStatusRepository, never())
                    .findById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("읽음 상태가 존재하지 않으면 예외가 발생한다")
        void notFound() {
            // given
            UUID id = UUID.randomUUID();

            given(readStatusRepository.findById(id))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    readStatusService.find(id)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("읽음 상태를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("사용자별 읽음 상태 목록 조회")
    class FindAllByUserId {

        @Test
        @DisplayName("사용자의 읽음 상태 목록을 조회한다")
        void success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID firstId = UUID.randomUUID();
            UUID secondId = UUID.randomUUID();
            UUID firstChannelId = UUID.randomUUID();
            UUID secondChannelId = UUID.randomUUID();

            ReadStatus first =
                    org.mockito.Mockito.mock(ReadStatus.class);

            ReadStatus second =
                    org.mockito.Mockito.mock(ReadStatus.class);

            given(userRepository.existsById(userId))
                    .willReturn(true);

            given(readStatusRepository.findAllByUser_Id(userId))
                    .willReturn(List.of(first, second));

            stubReadStatus(
                    first,
                    firstId,
                    userId,
                    firstChannelId,
                    Instant.now()
            );

            stubReadStatus(
                    second,
                    secondId,
                    userId,
                    secondChannelId,
                    Instant.now()
            );

            // when
            List<ReadStatusResponse> responses =
                    readStatusService.findAllByUserId(userId);

            // then
            assertThat(responses).hasSize(2);

            verify(readStatusRepository)
                    .findAllByUser_Id(userId);
        }

        @Test
        @DisplayName("읽음 상태가 없으면 빈 목록을 반환한다")
        void empty() {
            // given
            UUID userId = UUID.randomUUID();

            given(userRepository.existsById(userId))
                    .willReturn(true);

            given(readStatusRepository.findAllByUser_Id(userId))
                    .willReturn(List.of());

            // when
            List<ReadStatusResponse> responses =
                    readStatusService.findAllByUserId(userId);

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("사용자 ID가 null이면 예외가 발생한다")
        void nullUserId() {
            assertThatThrownBy(() ->
                    readStatusService.findAllByUserId(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자 id는 필수입니다");

            verify(userRepository, never())
                    .existsById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
        void userNotFound() {
            // given
            UUID userId = UUID.randomUUID();

            given(userRepository.existsById(userId))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() ->
                    readStatusService.findAllByUserId(userId)
            )
                    .isInstanceOf(UserNotFoundException.class);

            verify(readStatusRepository, never())
                    .findAllByUser_Id(
                            org.mockito.ArgumentMatchers.any()
                    );
        }
    }

    @Nested
    @DisplayName("읽음 상태 수정")
    class Update {

        @Test
        @DisplayName("lastReadAt이 있으면 해당 시각으로 수정한다")
        void successWithLastReadAt() {
            // given
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();
            Instant lastReadAt = Instant.now();

            ReadStatusUpdateRequest request =
                    org.mockito.Mockito.mock(ReadStatusUpdateRequest.class);

            ReadStatus readStatus =
                    org.mockito.Mockito.mock(ReadStatus.class);

            given(request.getId()).willReturn(id);
            given(request.getLastReadAt()).willReturn(lastReadAt);

            given(readStatusRepository.findById(id))
                    .willReturn(Optional.of(readStatus));

            given(readStatusRepository.save(readStatus))
                    .willReturn(readStatus);

            stubReadStatus(
                    readStatus,
                    id,
                    userId,
                    channelId,
                    lastReadAt
            );

            // when
            ReadStatusResponse response =
                    readStatusService.update(request);

            // then
            assertThat(response).isNotNull();

            verify(readStatus)
                    .updateLastReadAt(lastReadAt);

            verify(readStatusRepository)
                    .save(readStatus);
        }

        @Test
        @DisplayName("lastReadAt이 없으면 현재 시각으로 수정한다")
        void successWithoutLastReadAt() {
            // given
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            ReadStatusUpdateRequest request =
                    org.mockito.Mockito.mock(ReadStatusUpdateRequest.class);

            ReadStatus readStatus =
                    org.mockito.Mockito.mock(ReadStatus.class);

            given(request.getId()).willReturn(id);

            given(readStatusRepository.findById(id))
                    .willReturn(Optional.of(readStatus));

            given(readStatusRepository.save(readStatus))
                    .willReturn(readStatus);

            stubReadStatus(
                    readStatus,
                    id,
                    userId,
                    channelId,
                    Instant.now()
            );

            // when
            ReadStatusResponse response =
                    readStatusService.update(request);

            // then
            assertThat(response).isNotNull();

            verify(readStatus)
                    .updateLastReadAt();

            verify(readStatusRepository)
                    .save(readStatus);
        }

        @Test
        @DisplayName("수정 요청이 null이면 예외가 발생한다")
        void nullRequest() {
            assertThatThrownBy(() ->
                    readStatusService.update(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "읽음 상태 수정 요청은 비어 있을 수 없습니다"
                    );

            verify(readStatusRepository, never())
                    .findById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("읽음 상태 ID가 null이면 예외가 발생한다")
        void nullId() {
            // given
            ReadStatusUpdateRequest request =
                    org.mockito.Mockito.mock(ReadStatusUpdateRequest.class);

            given(request.getId()).willReturn(null);

            // when & then
            assertThatThrownBy(() ->
                    readStatusService.update(request)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "수정할 읽음 상태 id는 필수입니다"
                    );

            verify(readStatusRepository, never())
                    .findById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("읽음 상태가 존재하지 않으면 예외가 발생한다")
        void notFound() {
            // given
            UUID id = UUID.randomUUID();

            ReadStatusUpdateRequest request =
                    org.mockito.Mockito.mock(ReadStatusUpdateRequest.class);

            given(request.getId()).willReturn(id);

            given(readStatusRepository.findById(id))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    readStatusService.update(request)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("읽음 상태를 찾을 수 없습니다");

            verify(readStatusRepository, never())
                    .save(org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("읽음 상태 삭제")
    class Delete {

        @Test
        @DisplayName("존재하는 읽음 상태를 삭제한다")
        void success() {
            // given
            UUID id = UUID.randomUUID();

            given(readStatusRepository.existsById(id))
                    .willReturn(true);

            // when
            readStatusService.delete(id);

            // then
            verify(readStatusRepository)
                    .deleteById(id);
        }

        @Test
        @DisplayName("삭제 ID가 null이면 예외가 발생한다")
        void nullId() {
            assertThatThrownBy(() ->
                    readStatusService.delete(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "삭제할 읽음 상태 id는 필수입니다"
                    );

            verify(readStatusRepository, never())
                    .existsById(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("존재하지 않는 읽음 상태를 삭제하면 예외가 발생한다")
        void notFound() {
            // given
            UUID id = UUID.randomUUID();

            given(readStatusRepository.existsById(id))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() ->
                    readStatusService.delete(id)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "삭제할 읽음 상태를 찾을 수 없습니다"
                    );

            verify(readStatusRepository, never())
                    .deleteById(org.mockito.ArgumentMatchers.any());
        }
    }

    private void stubReadStatus(
            ReadStatus readStatus,
            UUID id,
            UUID userId,
            UUID channelId,
            Instant lastReadAt
    ) {
        given(readStatus.getId()).willReturn(id);
        given(readStatus.getCreatedAt()).willReturn(Instant.now());
        given(readStatus.getUpdatedAt()).willReturn(Instant.now());
        given(readStatus.getUserId()).willReturn(userId);
        given(readStatus.getChannelId()).willReturn(channelId);
        given(readStatus.getLastReadAt()).willReturn(lastReadAt);
    }
}