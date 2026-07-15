package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("읽음 상태 생성 요청은 비어 있을 수 없습니다.");
        }

        UUID userId = request.getUserId();
        UUID channelId = request.getChannelId();

        if (userId == null) {
            throw new IllegalArgumentException("사용자 id는 필수입니다.");
        }

        if (channelId == null) {
            throw new IllegalArgumentException("채널 id는 필수입니다.");
        }

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("읽음 상태를 생성할 사용자를 찾을 수 없습니다. userId=" + userId);
        }

        if (!channelRepository.existsById(channelId)) {
            throw new IllegalArgumentException("읽음 상태를 생성할 채널을 찾을 수 없습니다. channelId=" + channelId);
        }

        ReadStatus existingReadStatus =
                readStatusRepository.findByUserIdAndChannelId(userId, channelId);

        if (existingReadStatus != null) {
            return toResponse(existingReadStatus);
        }

        ReadStatus readStatus = new ReadStatus(userId, channelId);

        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

        return toResponse(savedReadStatus);
    }

    @Override
    public ReadStatusResponse find(UUID id) {
        ReadStatus readStatus = findReadStatusById(id);

        return toResponse(readStatus);
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 id는 필수입니다.");
        }

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("읽음 상태를 조회할 사용자를 찾을 수 없습니다. userId=" + userId);
        }

        List<ReadStatus> readStatuses = readStatusRepository.findAllByUserId(userId);
        List<ReadStatusResponse> responses = new ArrayList<>();

        for (ReadStatus readStatus : readStatuses) {
            responses.add(toResponse(readStatus));
        }

        return responses;
    }

    @Override
    public ReadStatusResponse update(ReadStatusUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("읽음 상태 수정 요청은 비어 있을 수 없습니다.");
        }

        UUID readStatusId = request.getId();

        if (readStatusId == null) {
            throw new IllegalArgumentException("수정할 읽음 상태 id는 필수입니다.");
        }

        ReadStatus readStatus = findReadStatusById(readStatusId);

        /*
         * 현재 ReadStatus 엔티티는 updateLastReadAt() 호출 시
         * 현재 시간 기준으로 lastReadAt을 갱신한다.
         */
        readStatus.updateLastReadAt();

        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

        return toResponse(savedReadStatus);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("삭제할 읽음 상태 id는 필수입니다.");
        }

        if (!readStatusRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 읽음 상태를 찾을 수 없습니다. id=" + id);
        }

        readStatusRepository.deleteById(id);
    }

    private ReadStatus findReadStatusById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("읽음 상태 id는 필수입니다.");
        }

        return readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "읽음 상태를 찾을 수 없습니다. id=" + id
                ));
    }

    private ReadStatusResponse toResponse(ReadStatus readStatus) {
        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getLastReadAt()
        );
    }
}