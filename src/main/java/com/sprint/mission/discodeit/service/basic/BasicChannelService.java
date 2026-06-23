package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.input.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.input.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.output.ChannelDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.DiscodeitException;
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
    public Channel createPublicChannel(PublicChannelCreateRequest cpb){
        Channel cnl = new Channel(cpb.name(), cpb.description(), ChannelType.PUBLIC);
        cr.save(cnl);
        return cnl;
    }

    @Override
    public Channel createPrivateChannel(PrivateChannelCreateRequest cpv){
        Channel cnl = new Channel("", "", ChannelType.PRIVATE);
        cr.save(cnl);

        for (UUID pid : cpv.participantIds()){
            rsr.save(new ReadStatus(pid,cnl.getId(),null));
        }
        return cnl;
    }

    @Override
    public List<ChannelDto> findAllByUserID(UUID userID) {

        List<UUID> cnlIDinReadStatus = rsr.find(c -> c.getUserId().equals(userID))
                .stream().map(ReadStatus::getChannelId).toList();

        return cr.findAll().stream()
                .filter(
                        c -> c.getType().equals(ChannelType.PUBLIC)
                                || cnlIDinReadStatus.contains(c.getId())
                )
                .map(this::toChannelOutput)
                .toList();
    }

    @Override
    public Channel update(UUID id, PublicChannelUpdateRequest uci) {
        Channel cnl = cr.findById(id).orElseThrow(
                () -> new DiscodeitException(
                        "channel with id " + id + "not found",
                        "Channel",
                        404
                )
        );

        if (cnl.getType().equals(ChannelType.PRIVATE)) {
            throw new DiscodeitException(
                    "Private channel can not be update",
                    "Channel",
                    400
            );
        }


        cnl.setName(uci.newName());
        cnl.setDescription(uci.newDescription());

        cr.save(cnl);
        return cnl;
    }

    @Override
    public void deleteChannel(UUID id) {
        cr.findById(id).orElseThrow(
                () -> new DiscodeitException(
                        "Channel whith id " + id + "not found",
                        "Channel",
                        404
                )
        );

        cr.delete(id);
        mr.find(m -> m.getChannelId().equals(id))
                .forEach(ms -> mr.delete(ms.getId()));
        rsr.find(r -> r.getChannelId().equals(id))
                .forEach(rs -> rsr.delete(rs.getId()));
    }




    private ChannelDto toChannelOutput(Channel chn){
        List<UUID> userIDs;

        List<Message> msg = mr.findByChannelID(chn.getId())
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt))
                .toList();

        if (chn.getType().equals(ChannelType.PRIVATE)) {
            userIDs = rsr.findByChennalID(chn.getId()).stream()
                    .map(ReadStatus::getUserId)
                    .toList();
        } else {
            userIDs = List.of();
        }


        return ChannelDto.builder()
                .channelId(chn.getId())
                .name(chn.getName())
                .description(chn.getDescription())
                .lastMessageAt(!msg.isEmpty() ? msg.get(0).getUpdatedAt() : null)
                .participantIds(userIDs)
                .build();
    }
}
