package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
                .orElseThrow(() -> new ChannelNotFoundException(dto.channelId()));
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException(dto.userId()));

        if (readStatusRepository.existsByChannelIdAndUserId(dto.channelId(), dto.userId())) {
            throw new ReadStatusAlreadyExistException(dto.channelId(), dto.userId());
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
                .orElseThrow(() -> new ReadStatusNotFoundException(id));

        readStatus.updateLastRead();

        return readStatusMapper.toDto(readStatus);
    }

    @Override
    public void delete(UUID id) {
        readStatusRepository.deleteById(id);
    }
}