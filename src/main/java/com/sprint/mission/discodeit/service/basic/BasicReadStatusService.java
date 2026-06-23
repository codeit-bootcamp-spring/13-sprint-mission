package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.input.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.input.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository rsr;
    private final UserRepository ur;
    private final ChannelRepository ch;

    @Override
    public ReadStatus create(ReadStatusCreateRequest rscr){

        // not found exception
        ch.findById(rscr.channelId()).orElseThrow(
                () -> new DiscodeitException(
                        "Channel with id " + rscr.channelId() + " not found",
                        "ReadStatus",
                        404
                )
        );
        ur.findByID(rscr.userId()).orElseThrow(
                () -> new DiscodeitException(
                        "User with id " + rscr.userId() + " not found",
                        "ReadStatus",
                        404
                )
        );


        // already exist exception
        if (
                !rsr.findByUserId(rscr.userId()).isEmpty() | !rsr.findByChennalID(rscr.channelId()).isEmpty()
        ) throw new DiscodeitException(
                "ReadStatus whith userId " + rscr.userId() + "and channelId " + rscr.channelId() + " already existed",
                "UserStatus",
                400
        );


        ReadStatus res = new ReadStatus(rscr.userId(), rscr.channelId(), rscr.lastReadAt());

        rsr.save(res);
        return res;
    }

    @Override
    public List<ReadStatus> findAllByUserID(UUID userID){
        return rsr.find(rs -> rs.getUserId().equals(userID));
    }

    @Override
    public ReadStatus update(UUID id, ReadStatusUpdateRequest rsur){
        ReadStatus rs = rsr.findByID(id).orElseThrow(
                () -> new DiscodeitException(
                        "ReadStatus with id " + id + "not found",
                        "ReadStatus",
                        404)
        );
        rs.setLastReadAt(rsur.newLastReadAt());
        rsr.save(rs);
        return rs;
    }
    @Override
    public void delete(UUID id){
        rsr.delete(id);
    }
}
