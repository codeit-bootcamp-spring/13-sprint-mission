package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReadStatusService {

    private final ReadStatusRepository readStatusRepository;

    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        if (readStatusRepository.findByUserIdAndChannelId(
                request.getUserId(),
                request.getChannelId()).isPresent()) {

            throw new IllegalArgumentException("이미 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(
                        request.getUserId(),
                        request.getChannelId(),
                        Instant.now()
        );

        readStatusRepository.save(readStatus);

        return ReadStatusResponse.from(readStatus);
    }

    public ReadStatusResponse find(UUID id) {
        ReadStatus status = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("읽음 상태를 찾을 수 없습니다."));

        return ReadStatusResponse.from(status);
    }

    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        return readStatusRepository.findAllByUserId(userId)
                .stream()
                .map(ReadStatusResponse::from)
                .toList();
    }

    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest request) {
        ReadStatus status = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("읽음 상태를 찾을 수 없습니다."));

        status.update(request.getLastReadAt());
        readStatusRepository.save(status);

        return ReadStatusResponse.from(status);
    }

    // 스프린트 미션 4 - 특정 채널 메시지 수신 정보 수정
    public ReadStatusResponse updateLastReadAt(UUID userId, UUID channelId) {
        ReadStatus status = readStatusRepository
                .findByUserIdAndChannelId(userId, channelId)
                .orElseThrow(() ->
                        new IllegalArgumentException("읽음 상태를 찾을 수 없습니다."));

        status.update(Instant.now());

        readStatusRepository.save(status);

        return ReadStatusResponse.from(status);
    }

    public void delete(UUID id) {
        readStatusRepository.delete(id);
    }

}
