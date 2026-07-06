package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.input.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.input.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.output.ChannelDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JAPReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final JPAChannelRepository channelRepository;
    private final JPAMessageRepository messageRepository;
    private final JAPReadStatusRepository readStatusRepository;
    private final JPAUserRepository userRepository;


    @Override
    @Transactional
    public Channel createPublicChannel(PublicChannelCreateRequest cpb){
        Channel cnl = new Channel(cpb.name(), cpb.description(), ChannelType.PUBLIC);
        channelRepository.save(cnl);
        return cnl;
    }

    @Override
    @Transactional
    public Channel createPrivateChannel(PrivateChannelCreateRequest cpv){
        Channel cnl = new Channel("", "", ChannelType.PRIVATE);
        channelRepository.save(cnl);

        for (UUID pid : cpv.participantIds()){
            User user = userRepository.findById(pid).stream().findFirst().orElseThrow(
                    () -> new DiscodeitException(
                            "User by id - " + pid + " not existed."
                            ,"Channel"
                            ,400
                    )
            );
            readStatusRepository.save(new ReadStatus(user,cnl,null));
        }
        return cnl;
    }

    @Override
    public List<ChannelDto> findAllByUserID(UUID userID) {

        List<UUID> cnlIDinReadStatus = readStatusRepository.findByUserId(userID)
                .stream().map(
                        rs -> rs.getChannel().getId()
                ).toList();

        return channelRepository.findAll().stream()
                .filter(
                        c -> c.getType().equals(ChannelType.PUBLIC)
                                || cnlIDinReadStatus.contains(c.getId())
                )
                .map(this::toChannelOutput)
                .toList();
    }

    @Override
    @Transactional
    public Channel update(UUID id, PublicChannelUpdateRequest uci) {
        Channel cnl = channelRepository.findById(id).orElseThrow(
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

        return cnl;
    }

    @Override
    @Transactional
    public void deleteChannel(UUID id) {
        channelRepository.findById(id).orElseThrow(
                () -> new DiscodeitException(
                        "Channel whith id " + id + "not found",
                        "Channel",
                        404
                )
        );

        channelRepository.deleteById(id);
        messageRepository.deleteAll(messageRepository.findByChannelId(id));
        readStatusRepository.deleteAll(readStatusRepository.findByChannelId(id));
    }




    private ChannelDto toChannelOutput(Channel chn){
        final List<UUID> userIDs = new ArrayList<>();

        List<Message> msg = messageRepository.findByChannelId(chn.getId())
                .stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedAt))
                .toList();

        if (chn.getType().equals(ChannelType.PRIVATE)) {
            for (ReadStatus rs : readStatusRepository.findByChannelId(chn.getId())) {
                userIDs.add(rs.getUser().getId());
            }
        }

        return ChannelDto.builder()
                .id(chn.getId())
                .type(chn.getType())
                .name(chn.getName())
                .description(chn.getDescription())
                .lastMessageAt(!msg.isEmpty() ? msg.get(0).getUpdatedAt() : chn.getCreatedAt())
                .participantIds(userIDs)
                .build();
    }
}
