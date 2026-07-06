package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JAPReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final JAPReadStatusRepository JAPReadStatusrepository;
    private final JPAUserRepository JPAUserRepository;
    private final JPAChannelRepository JPAChannelRepository;
    private final ReadStatusMapper readStatusMapper;

    @Override
    @Transactional
    public ReadStatusDto create(ReadStatusCreateRequest rscr){

        // not found exception
        Channel channel = JPAChannelRepository.findById(rscr.channelId()).stream().findFirst().orElseThrow(
                () -> new DiscodeitException(
                        "Channel with id " + rscr.channelId() + " not found",
                        "ReadStatus",
                        404
                )
        );
        User user = JPAUserRepository.findById(rscr.userId()).stream().findFirst().orElseThrow(
                () -> new DiscodeitException(
                        "User with id " + rscr.userId() + " not found",
                        "ReadStatus",
                        404
                )
        );


        // already exist exception
        if (
                !JAPReadStatusrepository.findByUserId(rscr.userId()).isEmpty() | !JAPReadStatusrepository.findByChannelId(rscr.channelId()).isEmpty()
        ) throw new DiscodeitException(
                "ReadStatus whith userId " + rscr.userId() + "and channelId " + rscr.channelId() + " already existed",
                "UserStatus",
                400
        );



        return readStatusMapper.toDto(
                JAPReadStatusrepository.save(new ReadStatus(user, channel, rscr.lastReadAt()))
        );
    }

    @Override
    public List<ReadStatusDto> findAllByUserID(UUID userID){
        return JAPReadStatusrepository.findByUserId(userID)
                .stream().map(
                        readStatusMapper::toDto
                ).toList();
    }

    @Override
    @Transactional
    public ReadStatusDto update(UUID id, ReadStatusUpdateRequest rsur){
        ReadStatus readStatus = JAPReadStatusrepository.findById(id).stream().findFirst().orElseThrow(
                () -> new DiscodeitException(
                        "ReadStatus with id " + id + "not found",
                        "ReadStatus",
                        404)
        );
        readStatus.setLastReadAt(rsur.newLastReadAt());
        return readStatusMapper.toDto(readStatus);
    }
    @Override
    @Transactional
    public void delete(UUID id){
        JAPReadStatusrepository.deleteById(id);
    }
}
