package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.ChannelProfile;
import com.sprint.mission.discodeit.dto.input.CreateReadyStatusInput;
import com.sprint.mission.discodeit.dto.output.ChannelOutput;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository cr;
    private final MessageRepository mr;
    private final ReadStatusRepository rsr;

    @Override
    public void createPublicChannel(ChannelProfile cnp){
        cr.save(new Channel(cnp.getName(), cnp.getDescription(), ChannelType.PUBLIC));
    }

    @Override
    public void createPrivateChannel(CreateReadyStatusInput rsi){
        rsr.save(new ReadStatus(rsi.getUserID(),rsi.getChannelID()));
        cr.save(new Channel("", "", ChannelType.PRIVATE));
    }

    @Override
    public ChannelOutput findChannelInfoById(UUID id) {
        Channel cnl =  cr.find((c) -> c.getId().equals(id)).get(0);
        List<Message> msg = mr.find(m -> m.getChannelID().equals(id));
        // sort by cur to past
        msg.sort(Comparator.comparing(BaseEntity::getUpdatedAt).reversed());

        List<UUID> userIDs;
        if (cnl.getType().equals(ChannelType.PRIVATE)) {
            userIDs = rsr.find(c -> c.getChannelID().equals(cnl.getId()))
                    .stream()
                    .map(ReadStatus::getUserID)
                    .toList();
        } else {
            userIDs = List.of();
        }

        return ChannelOutput.builder()
                .channelID(cnl.getId())
                .channelName(cnl.getName())
                .channelDescription(cnl.getDescription())
                .lastMsgTime(msg.get(0).getUpdatedAt())
                .userIDs(userIDs)
                .build();
    }

    @Override
    public List<ChannelOutput> findAllByUserID(UUID userID) {
        List<ReadStatus> rst = rsr.find(c -> c.getUserID().equals(userID));

        return rst.stream()
                .map(rs -> findChannelInfoById(rs.getChannelID()))
                .toList();
    }

    @Override
    public void updateChannelInfo(UUID id, ChannelProfile cnp) throws RuntimeException {
        Channel cnl = cr.find(c -> c.getId().equals(id)).get(0);

        if (cnl.getType().equals(ChannelType.PRIVATE)) throw new RuntimeException("Private channel");

        cnl.setName(cnp.getName());
        cnl.setDescription(cnp.getDescription());
        cr.save(cnl);
    }

    @Override
    public void deleteChannel(UUID id) {
        cr.delete(id);
        mr.find(m -> m.getChannelID().equals(id))
                .forEach(c -> cr.delete(c.getId()));
        rsr.find(m -> m.getChannelID().equals(id))
                .forEach(c -> rsr.delete(c.getId()));
    }
}
