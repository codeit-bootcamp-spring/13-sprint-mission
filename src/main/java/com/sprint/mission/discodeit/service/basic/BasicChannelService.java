package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JAPReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final JPAChannelRepository channelRepository;
    private final JPAMessageRepository messageRepository;
    private final JAPReadStatusRepository readStatusRepository;
    private final JPAUserRepository userRepository;
    private final ChannelMapper channelMapper;

    @Override
    @Transactional
    public ChannelDto createPublicChannel(PublicChannelCreateRequest cpb){
        Channel cnl = new Channel(cpb.name(), cpb.description(), ChannelType.PUBLIC);
        return channelMapper.toDto(channelRepository.save(cnl));
    }

    @Override
    @Transactional
    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest cpv){
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
        return channelMapper.toDto(cnl);
    }

    @Override
    public List<ChannelDto> findAllByUserID(UUID userID) {

        Stream<ChannelDto> pv = readStatusRepository.findByUserId(userID)
                .stream()
                .map(
                        rs -> channelMapper.toDto(rs.getChannel())
                );

        Stream<ChannelDto> pb = channelRepository.findByTypeIs(ChannelType.PUBLIC)
                .stream().map(channelMapper::toDto);

        return Stream.concat(pv,pb).toList();
    }

    @Override
    @Transactional
    public ChannelDto update(UUID id, PublicChannelUpdateRequest uci) {
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

        return channelMapper.toDto(cnl);
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
    }
}
