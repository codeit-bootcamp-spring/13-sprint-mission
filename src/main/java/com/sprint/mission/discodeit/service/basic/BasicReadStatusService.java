package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.input.CreateReadyStatusInput;
import com.sprint.mission.discodeit.dto.input.UpdateReadStatusInput;
import com.sprint.mission.discodeit.entity.ReadStatus;
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
    public void create(CreateReadyStatusInput crsi){
        if (
                ch.find(c -> c.getId().equals(crsi.getChannelID())).isEmpty()
                || ur.find(c -> c.getId().equals(crsi.getUserID())).isEmpty()
        ) throw new RuntimeException("invalid create readstatus");
        if (
                ! rsr.find(rs -> rs.getUserID().equals(crsi.getUserID())).isEmpty()
                && ! rsr.find(rs -> rs.getChannelID().equals(crsi.getChannelID())).isEmpty()
        ) throw new RuntimeException("object already created");
        rsr.save(new ReadStatus(crsi.getUserID(), crsi.getChannelID()));
    }

    @Override
    public ReadStatus find(UUID id){
        return rsr.find(rs -> rs.getId().equals(id)).get(0);
    }

    @Override
    public List<ReadStatus> findAllByUserID(UUID userID){
        return rsr.find(rs -> rs.getUserID().equals(userID));
    }

    // wich field will change?
    @Override
    public void update(UpdateReadStatusInput ursi){
        ReadStatus rs = rsr.find(r -> r.getId().equals(ursi.getReadStatusID())).get(0);
        rs.setUpdatedAt();
    }
    @Override
    public void delete(UUID id){
        rsr.delete(id);
    }
}
