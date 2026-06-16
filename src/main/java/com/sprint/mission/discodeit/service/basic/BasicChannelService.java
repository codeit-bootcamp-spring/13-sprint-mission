package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.CreatePrivateChannelInput;
import com.sprint.mission.discodeit.dto.input.CreatePublicChannelInput;
import com.sprint.mission.discodeit.dto.input.UpdateChannelInput;
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
    public void createPublicChannel(CreatePublicChannelInput cpb){
        cr.save(new Channel(cpb.getName(), cpb.getDescription(), ChannelType.PUBLIC));
        // read status service 추가해야 함.
    }

    @Override
    public void createPrivateChannel(CreatePrivateChannelInput cpv){
        Channel cnl = new Channel("", "", ChannelType.PRIVATE);
        cr.save(cnl);
        rsr.save(new ReadStatus(UUID.fromString(cpv.getId()),cnl.getId()));
    }

    @Override
    public ChannelOutput findChannelInfoById(UUID id) {
        Channel cnl =  cr.findById(id);
        return toChannelOutput(cnl);
    }

    @Override
    public List<ChannelOutput> findAllByUserID(UUID userID) {

        List<UUID> cnlIDinReadStatus = rsr.find(c -> c.getUserID().equals(userID))
                .stream().map(ReadStatus::getChannelID).toList();

        return cr.findAll().stream()
                .filter(
                        c -> c.getType().equals(ChannelType.PUBLIC)
                                || cnlIDinReadStatus.contains(c.getId())
                )
                .map(this::toChannelOutput)
                .toList();
    }

    @Override
    public void updateChannelInfo(UpdateChannelInput uci) throws RuntimeException {
        Channel cnl = cr.findById(uci.idToUUID());

        if (cnl.getType().equals(ChannelType.PRIVATE)) throw new RuntimeException("Private channel");

        if (!uci.name().isEmpty()) cnl.setName(uci.name());
        if (!uci.description().isEmpty()) cnl.setDescription(uci.description());
        cr.save(cnl);
    }

    @Override
    public void deleteChannel(UUID id) {
        cr.delete(id);
        mr.find(m -> m.getChannelID().equals(id))
                .forEach(ms -> mr.delete(ms.getId()));
        rsr.find(r -> r.getChannelID().equals(id))
                .forEach(rs -> rsr.delete(rs.getId()));
    }

    private ChannelOutput toChannelOutput(Channel chn){
        List<UUID> userIDs = null;

        List<Message> msg = mr.findByChannelID(chn.getId())
                .stream()
                .sorted(Comparator.comparing(
                        (m1) -> m1.getCreatedAt()
                ))
                .toList();

        if (chn.getType().equals(ChannelType.PRIVATE)) {
            userIDs = rsr.findbyChennalID(chn.getId()).stream()
                    .map(ReadStatus::getUserID)
                    .toList();
        } else {
            userIDs = List.of();
        }


        return ChannelOutput.builder()
                .channelID(chn.getId())
                .channelName(chn.getName())
                .channelDescription(chn.getDescription())
                .lastMsgTime(!msg.isEmpty() ? msg.get(0).getUpdatedAt() : null)
                .userIDs(userIDs)
                .build();
    }
}
