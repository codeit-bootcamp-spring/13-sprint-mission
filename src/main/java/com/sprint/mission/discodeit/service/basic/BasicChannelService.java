package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.CreatePrivateChannelInput;
import com.sprint.mission.discodeit.dto.input.CreatePublicChannelInput;
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
        // 일반 리스트 정렬시, immutableCollectios 예외를 뱉었음.
        // stream 이나, 새로운 List 구현체를 반환해서 작업할 것.
        List<Message> msg = mr.findByChannelID(cnl.getId())
                .stream()
                .sorted(Comparator.comparing(
                        (m1) -> m1.getCreatedAt()
                ))
                .toList();

        List<UUID> userIDs;
        if (cnl.getType().equals(ChannelType.PRIVATE)) {
            userIDs = rsr.findbyChennalID(cnl.getId()).stream()
                    .map(ReadStatus::getUserID)
                    .toList();
        } else {
            userIDs = List.of();
        }

        return ChannelOutput.builder()
                .channelID(cnl.getId())
                .channelName(cnl.getName())
                .channelDescription(cnl.getDescription())
                .lastMsgTime(!msg.isEmpty() ? msg.get(0).getUpdatedAt() : null)
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
    public void updateChannelInfo(UUID id, CreatePublicChannelInput cnp) throws RuntimeException {
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
                .forEach(ms -> mr.delete(ms.getId()));
        rsr.find(r -> r.getChannelID().equals(id))
                .forEach(rs -> rsr.delete(rs.getId()));
    }
}
