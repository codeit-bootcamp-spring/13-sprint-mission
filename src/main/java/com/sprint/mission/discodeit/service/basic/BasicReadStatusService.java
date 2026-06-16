package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public BasicReadStatusService(ReadStatusRepository readStatusRepository, UserRepository userRepository, ChannelRepository channelRepository) {
        this.readStatusRepository = readStatusRepository;
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
    }

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        if (userRepository.findById(request.userId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        if (channelRepository.findById(request.channelId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        boolean isAlreadyExist = readStatusRepository.findAll().stream()
                .anyMatch(rs -> rs.getUserId().equals(request.userId())
                        && rs.getChannelId().equals(request.channelId()));

        if (isAlreadyExist) {
            throw new IllegalArgumentException("이미 해당 채널에 참여 중인 유저입니다.");
        }

        ReadStatus readStatus = ReadStatus.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .userId(request.userId())
                .channelId(request.channelId())
                .readAt(Instant.now())
                .build();

        readStatusRepository.save(readStatus);

        return convertToResponse(readStatus);
    }

    @Override
    public ReadStatusResponse find(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 읽음 상태 정보입니다."));

        return convertToResponse(readStatus);
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        return readStatusRepository.findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 읽음 상태 정보입니다."));

        readStatus.update(request.readAt());
        readStatusRepository.save(readStatus);

        return convertToResponse(readStatus);
    }

    @Override
    public void delete(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 읽음 상태 정보입니다."));

        readStatusRepository.delete(id);

    }

    private ReadStatusResponse convertToResponse(ReadStatus readStatus) {
        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getReadAt()
        );
    }
}
