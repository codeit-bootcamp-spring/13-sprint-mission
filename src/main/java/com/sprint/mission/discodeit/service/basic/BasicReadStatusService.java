package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.readstatus.ReadStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.readstatus.ReadStatusUpdateCommand;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusMapper readStatusMapper;


    //생성
    @Override
    public ReadStatusDto create(ReadStatusCreateCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> UserNotFoundException.withId(command.userId()));
        Channel channel = channelRepository.findById(command.channelId())
                .orElseThrow(() -> ChannelNotFoundException.withId(command.channelId()));

        Optional<ReadStatus> existing = readStatusRepository.findByUserIdAndChannelId(command.userId(), command.channelId());
        if(existing.isPresent()){
            return readStatusMapper.toDto(existing.get());
        }
        ReadStatus readStatus = new ReadStatus(user, channel);
        readStatusRepository.save(readStatus);
        return readStatusMapper.toDto(readStatus);
    }

    //조회
    @Override
    @Transactional(readOnly = true)
    public ReadStatusDto find(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> ReadStatusNotFoundException.withId(id));
        return readStatusMapper.toDto(readStatus);
    }

    //전체 조회
    @Override
    @Transactional(readOnly = true)
    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        List<ReadStatus> allByUserId = readStatusRepository.findAllByUserId(userId);
        return allByUserId.stream()
                .map(readStatusMapper::toDto)
                .collect(Collectors.toList());
    }

    //수정
    @Override
    public ReadStatusDto update(UUID id, ReadStatusUpdateCommand command) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> ReadStatusNotFoundException.withId(id));
        readStatus.updateLastReadAt(command.newLastReadAt());

        readStatusRepository.save(readStatus);
        return readStatusMapper.toDto(readStatus);
    }

    //삭제
    @Override
    public void delete(UUID id) {
       readStatusRepository.findById(id)
                .orElseThrow(() -> ReadStatusNotFoundException.withId(id));

       readStatusRepository.deleteById(id);
    }
}
