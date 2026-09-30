package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.request.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.*;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MapStructMapper mapStructMapper;

    @Override
    @Transactional(readOnly = true)
    public ReadStatusDto create(ReadStatusCreateRequest request){

        // not found exception
        Channel channel = channelRepository.findById(request.channelId()).stream().findFirst().orElseThrow(
                () -> new ChannelNotFoundException("Channel with id - {} was not found", request.channelId())
        );
        User user = userRepository.findById(request.userId()).stream().findFirst().orElseThrow(
                () -> new UserNotFoundException("User with Id - {} was not founded", request.userId())
        );


        // already exist exception
        if (readStatusRepository.existsByUserOrChannel(user,channel))
            throw new ReadStatusDuplicatedException("Read Status with user - {}, channel - {} was already existed", request.userId(), request.channelId());


        return mapStructMapper.toDto(
                readStatusRepository.save(new ReadStatus(user, channel, request.lastReadAt()))
        );
    }


    // todo - 유저가 볼 수 있는 거 전부? 혹은 유저 id 에 대해서?
    // 아마 후자? public 은 read status 가 바로 생성되지 않기 때문.
    @Override
    @Transactional(readOnly = true)
    public List<ReadStatusDto> findAllByUserID(UUID userID){
        return readStatusRepository.findAllByUserId(userID).stream()
                .map(mapStructMapper::toDto)
                .toList();
    }

    // todo - Instant 시간을 클라이언트가 보내주나? 확인.
    @Override
    @Transactional
    public ReadStatusDto update(UUID id, ReadStatusUpdateRequest request){
        ReadStatus readStatus = readStatusRepository.findById(id).stream().findFirst().orElseThrow(
                () -> new ReadStatusNotFoundException("ReadStatus with id - {} was not found", id)
        );
        readStatus.update();
        return mapStructMapper.toDto(readStatus);
    }
    @Override
    @Transactional
    public void delete(UUID id){
        readStatusRepository.deleteById(id);
    }
}
