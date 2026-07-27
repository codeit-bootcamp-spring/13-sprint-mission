package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MapStructMapper mapStructMapper;

    @Override
    @Transactional
    public ReadStatusDto create(ReadStatusCreateRequest rscr){

        // not found exception
        Channel channel = channelRepository.findById(rscr.channelId()).stream().findFirst().orElseThrow(
                () -> new DiscodeitException(
                        "Channel with id " + rscr.channelId() + " not found",
                        "ReadStatus",
                        404
                )
        );
        User user = userRepository.findById(rscr.userId()).stream().findFirst().orElseThrow(
                () -> new DiscodeitException(
                        "User with id " + rscr.userId() + " not found",
                        "ReadStatus",
                        404
                )
        );


        // already exist exception
        if (
                !readStatusRepository.findByUserId(rscr.userId()).isEmpty() | !readStatusRepository.findByChannelId(rscr.channelId()).isEmpty()
        ) throw new DiscodeitException(
                "ReadStatus whith userId " + rscr.userId() + "and channelId " + rscr.channelId() + " already existed",
                "UserStatus",
                400
        );



        return mapStructMapper.toDto(
                readStatusRepository.save(new ReadStatus(user, channel, rscr.lastReadAt()))
        );
    }

    @Override
    @Transactional
    public List<ReadStatusDto> findAllByUserID(UUID userID){
        Stream<ReadStatusDto> rspb = readStatusRepository.findByChannelType(ChannelType.PUBLIC)
                .stream()
                .map(mapStructMapper::toDto);
        Stream<ReadStatusDto> rspv = readStatusRepository.findByUserId(userID)
                .stream().map(
                        mapStructMapper::toDto
                );
        return Stream.concat(rspb, rspv).toList();
    }

    @Override
    @Transactional
    public ReadStatusDto update(UUID id, ReadStatusUpdateRequest rsur){
        ReadStatus readStatus = readStatusRepository.findById(id).stream().findFirst().orElseThrow(
                () -> new DiscodeitException(
                        "ReadStatus with id " + id + "not found",
                        "ReadStatus",
                        404)
        );
        readStatus.setLastReadAt(rsur.newLastReadAt());
        return mapStructMapper.toDto(readStatus);
    }
    @Override
    @Transactional
    public void delete(UUID id){
        readStatusRepository.deleteById(id);
    }
}
