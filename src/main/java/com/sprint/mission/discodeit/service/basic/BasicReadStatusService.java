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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest dto) {
        Channel channel = channelRepository.findById(dto.channelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));
        userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean exists = readStatusRepository.findAll().stream()
                .anyMatch(rs -> rs.getChannelId().equals(dto.channelId()) && rs.getUserId().equals(dto.userId()));
        if (readStatusRepository.existsByChannelIdAndUserId(dto.channelId(), dto.userId())) {
            throw new IllegalArgumentException("해당 채널과 사용자에 대한 읽음 상태 객체가 이미 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(channel, dto.userId());
        readStatusRepository.save(readStatus);

        return new ReadStatusResponse(
                readStatus.getId(), readStatus.getChannel().getId(), readStatus.getUserId(),
                readStatus.getCreatedAt(), readStatus.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReadStatusResponse> find(UUID id) {
        return readStatusRepository.findById(id)
                .map(rs -> new ReadStatusResponse(
                        rs.getId(), rs.getChannel().getId(), rs.getUserId(),
                        rs.getCreatedAt(), rs.getUpdatedAt()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        return readStatusRepository.findAllByUserId(userId).stream()
                .map(rs -> new ReadStatusResponse(
                        rs.getId(), rs.getChannel().getId(), rs.getUserId(),
                        rs.getCreatedAt(), rs.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest dto) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 읽음 상태 정보를 찾을 수 없습니다."));

        readStatus.updateLastRead();

        return new ReadStatusResponse(
                readStatus.getId(), readStatus.getChannel().getId(), readStatus.getUserId(),
                readStatus.getCreatedAt(), readStatus.getUpdatedAt()
        );
    }

    @Override
    public void delete(UUID id) {
        readStatusRepository.deleteById(id);
    }
}
