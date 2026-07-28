package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
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

@Service
@RequiredArgsConstructor
@Transactional
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusMapper readStatusMapper;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest dto) {
        Channel channel = channelRepository.findById(dto.channelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (readStatusRepository.existsByChannelIdAndUserId(dto.channelId(), dto.userId())) {
            throw new IllegalArgumentException("해당 채널과 사용자에 대한 읽음 상태 객체가 이미 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(channel, user);
        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

        return readStatusMapper.toDto(savedReadStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReadStatusResponse> find(UUID id) {
        return readStatusRepository.findById(id)
                .map(readStatusMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        return readStatusRepository.findAllByUserId(userId).stream()
                .map(readStatusMapper::toDto)
                .toList();
    }

    @Override
    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest dto) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 읽음 상태 정보를 찾을 수 없습니다."));

        readStatus.updateLastRead();

        return readStatusMapper.toDto(readStatus);
    }

    @Override
    public void delete(UUID id) {
        readStatusRepository.deleteById(id);
    }
}