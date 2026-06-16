package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest dto) {
        // [요구사항] 관련된 Channel 이나 User 가 존재하지 않으면 예외를 발생
        Channel channel = channelRepository.findById(dto.channelId());
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // [요구사항] 같은 Channel 과 User 와 관련된 객체가 이미 존재하면 예외를 발생
        boolean exists = readStatusRepository.findAll().stream()
                .anyMatch(rs -> rs.getChannelId().equals(dto.channelId()) && rs.getUserId().equals(dto.userId()));
        if (exists) {
            throw new IllegalArgumentException("해당 채널과 사용자에 대한 읽음 상태 객체가 이미 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(dto.channelId(), dto.userId());
        readStatusRepository.create(readStatus);

        return new ReadStatusResponse(
                readStatus.getId(), readStatus.getChannelId(), readStatus.getUserId(),
                readStatus.getCreatedAt(), readStatus.getUpdatedAt()
        );
    }

    @Override
    public Optional<ReadStatusResponse> find(UUID id) {
        // [요구사항] id로 조회
        return readStatusRepository.findById(id)
                .map(rs -> new ReadStatusResponse(
                        rs.getId(), rs.getChannelId(), rs.getUserId(),
                        rs.getCreatedAt(), rs.getUpdatedAt()
                ));
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        // [요구사항] userId를 조건으로 조회
        return readStatusRepository.findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .map(rs -> new ReadStatusResponse(
                        rs.getId(), rs.getChannelId(), rs.getUserId(),
                        rs.getCreatedAt(), rs.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest dto) {
        // [요구사항] 수정 대상 객체의 id 파라미터로 객체를 찾고 없으면 예외 처리
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 읽음 상태 정보를 찾을 수 없습니다."));

        readStatus.updateLastRead();
        readStatusRepository.update(readStatus);

        return new ReadStatusResponse(
                readStatus.getId(), readStatus.getChannelId(), readStatus.getUserId(),
                readStatus.getCreatedAt(), readStatus.getUpdatedAt()
        );
    }

    @Override
    public void delete(UUID id) {
        // [요구사항] id로 삭제
        readStatusRepository.delete(id);
    }
}
