package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.request.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusDuplicatedException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final String SERVICE_NAME = "ReadStatus";

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public ReadStatusDto create(ReadStatusCreateRequest request){

        // not found exception
        Channel channel = channelRepository.findById(request.channelId()).stream().findFirst().orElseThrow(
                () -> new ChannelNotFoundException(
                        "{} - Channel with id - {} was not found",
                        SERVICE_NAME,
                        request.channelId()
                )
        );
        User user = userRepository.findById(request.userId()).stream().findFirst().orElseThrow(
                () -> new UserNotFoundException(
                        "User with Id - {} was not founded",
                        SERVICE_NAME,
                        request.userId()
                )
        );


        ReadStatus readStatus;

        // already exist exception
        if (readStatusRepository.existsByUserOrChannel(user,channel))
            readStatus = readStatusRepository.findByUserIdAndChannelId(user.getId(),channel.getId());
        else
            readStatus = readStatusRepository.save(new ReadStatus(user, channel, request.lastReadAt()));

        log.debug("[{}] - ReadStatus created. id:{}", SERVICE_NAME, readStatus.getId());

        return entityMapper.toDto(readStatus);
    }



    @Override
    @Transactional(readOnly = true)
    public List<ReadStatusDto> findAllByUserID(UUID userID){
        return readStatusRepository.findAllByUserId(userID).stream()
                .map(entityMapper::toDto)
                .toList();
    }


    @Override
    @Transactional
    public ReadStatusDto update(UUID id, ReadStatusUpdateRequest request){
        // 이 부분이 에러가 났음.
        // public 채널이면, 읽기 상태가 없으니까 없음 에러가 생긴다?
        // public. 이면 채널에 들어가는 순간 생기는?
//        ReadStatus readStatus = readStatusRepository.findById(id).stream().findFirst().orElseThrow(
//                () -> new ReadStatusNotFoundException(
//                        "%s - ReadStatus with id - %s was not found",
//                        SERVICE_NAME,
//                        id
//                )
//        );

        ReadStatus readStatus = readStatusRepository.findById(id).orElseThrow(
                () -> new ReadStatusNotFoundException(
                        "%s - ReadStatus with id - %s was not found",
                        SERVICE_NAME,
                        id
                )
        );


        readStatus.update(request.newNotificationEnabled(), request.newLastReadAt());
        return entityMapper.toDto(readStatus);
    }
    @Override
    @Transactional
    public void delete(UUID id){
        readStatusRepository.deleteById(id);
    }
}
