package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.CreatePrivateChannelInput;
import com.sprint.mission.discodeit.dto.input.CreatePublicChannelInput;
import com.sprint.mission.discodeit.dto.input.UpdateChannelInput;
import com.sprint.mission.discodeit.dto.output.ChannelOutput;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.DiscodeitChannelException;
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
        if (cpb.name().isEmpty()) throw new DiscodeitChannelException("channel name is required!",400);
        cr.save(new Channel(cpb.name(), cpb.description(), ChannelType.PUBLIC));
    }

    @Override
    public void createPrivateChannel(CreatePrivateChannelInput cpv){
        Channel cnl = new Channel("", "", ChannelType.PRIVATE);
        cr.save(cnl);
        rsr.save(new ReadStatus(UUID.fromString(cpv.id()),cnl.getId()));
    }

    @Override
    public ChannelOutput findChannelInfoById(UUID id) {
        return toChannelOutput(
                cr.findById(id)
                        .orElseThrow(
                                () -> new DiscodeitChannelException("channel with id " + id + " not found",400)
                        )
        );
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
    public void updateChannelInfo(UpdateChannelInput uci) {
        Channel cnl = cr.findById(uci.idToUUID()).orElseThrow(
                () -> new DiscodeitChannelException("channel with id " + uci.idToUUID() + "not found", 400)
        );

        if (cnl.getType().equals(ChannelType.PRIVATE)) {
            throw new DiscodeitChannelException("channel with id " + uci.idToUUID() + " is Private channel",400);
        }

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
        List<UUID> userIDs;

        List<Message> msg = mr.findByChannelID(chn.getId())
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt))
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
