package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NoSuchElementException("유저 ID: " + request.userId() + " 를 찾을 수 없습니다.");
        }

        if (!channelRepository.existsById(request.channelId())) {
            throw new NoSuchElementException("채널 ID: " + request.channelId() + " 를 찾을 수 없습니다.");
        }

        boolean alreadyExists = readStatusRepository.findAll().stream()
                .anyMatch(readStatus -> readStatus.getUserId().equals(request.userId())
                        && readStatus.getChannelId().equals(request.channelId()));

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 해당 유저의 채널 읽음 상태가 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(
                request.userId(),
                request.channelId(),
                request.lastReadAt()
        );
        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

        log.info("ReadStatus: {}가 생성됨.", savedReadStatus.getId());
        return toResponse(savedReadStatus);
    }

    @Override
    public ReadStatusResponse find(UUID readStatusId) {
        ReadStatus readStatus = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new NoSuchElementException("읽음 상태 ID: " + readStatusId + " 를 찾을 수 없습니다."));

        return toResponse(readStatus);
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("유저 ID: " + userId + " 를 찾을 수 없습니다.");
        }

        return readStatusRepository.findAll().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ReadStatusResponse update(ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(request.readStatusId())
                .orElseThrow(() -> new NoSuchElementException("읽음 상태 ID: " + request.readStatusId() + " 를 찾을 수 없습니다."));

        readStatus.updateLastReadAt(request.newLastReadAt());
        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

        log.info("ReadStatus: {}가 수정됨.", savedReadStatus.getId());
        return toResponse(savedReadStatus);
    }

    @Override
    public void delete(UUID readStatusId) {
        if (!readStatusRepository.existsById(readStatusId)) {
            throw new NoSuchElementException("읽음 상태 ID: " + readStatusId + " 를 찾을 수 없습니다.");
        }

        readStatusRepository.deleteById(readStatusId);
        log.info("ReadStatus: {}가 삭제됨.", readStatusId);
    }

    private ReadStatusResponse toResponse(ReadStatus readStatus) {
        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getLastReadAt(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt()
        );
    }
}
