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
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReadStatusService implements ReadStatusService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final ReadStatusMapper readStatusMapper;

    @Override
    @Transactional
    public ReadStatusDto create(ReadStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(()->new UserNotFoundException(request.userId()));
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(()->new ChannelNotFoundException(request.channelId()));

        if(readStatusRepository.existsByUser_IdAndChannel_Id(user.getId(), channel.getId())){
            throw new ReadStatusAlreadyExistsException(user.getId(), channel.getId());
        }

        ReadStatus readStatus = new ReadStatus(user, channel);
        readStatusRepository.save(readStatus);
        return readStatusMapper.toDto(readStatus);
    }

    @Override
    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        List<ReadStatus> readStatuses = readStatusRepository.findAllByUser_Id(userId);
        List<ReadStatusDto> responses = new ArrayList<>();

        for (ReadStatus readStatus : readStatuses) {
            responses.add(readStatusMapper.toDto(readStatus));
        }

        return responses;
    }

    @Override
    @Transactional
    public ReadStatusDto update(ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusCheck(request.id());

        readStatus.updateLastReadAt(request.lastReadAt());
        readStatusRepository.save(readStatus);
        return readStatusMapper.toDto(readStatus);
    }

    private ReadStatus readStatusCheck(UUID id) {
        return readStatusRepository.findById(id)
                .orElseThrow(()->new ReadStatusNotFoundException(id));
    }
}
