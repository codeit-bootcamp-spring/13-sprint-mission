package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ChannelTypeException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MapperMethod mapperMethod;
    private final MapStructMapper mapStructMapper;

    @Override
    @Transactional
    public ChannelDto createPublicChannel(PublicChannelCreateRequest cpb){
        Channel cnl = new Channel(cpb.name(), cpb.description(), ChannelType.PUBLIC);

        log.info("public channel created - {}", cnl.getName());

        return mapStructMapper.toDto(
                channelRepository.save(cnl)
                ,userDtoFromChannel(cnl)
                ,lastMessageAt(cnl)
        );
    }

    @Override
    @Transactional
    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest cpv){
        Channel cnl = new Channel("", "", ChannelType.PRIVATE);
        channelRepository.save(cnl);

        log.info("private channel created - {}", cnl.getName());

        for (UUID pid : cpv.participantIds()){
            User user = getUserOrException(pid);
            readStatusRepository.save(new ReadStatus(user,cnl,Instant.now()));

            log.debug("User with id - {} is joined channel",pid);
        }
        return mapStructMapper.toDto(cnl,userDtoFromChannel(cnl),lastMessageAt(cnl));
    }




    @Override
    @Transactional
    public List<ChannelDto> findAllByUserID(UUID userID) {

        List<Channel> visible =  channelRepository.findVisibleChannelByUserId(userID);
        log.debug("visible channel query by userid - {}, channel count : {}", userID, visible.size());

        return visible.stream()
                .map(c -> mapStructMapper
                        .toDto(
                                c,
                                userDtoFromChannel(c),
                                lastMessageAt(c)
                        )
                )
                .toList();
    }

    @Override
    @Transactional
    public ChannelDto update(UUID id, PublicChannelUpdateRequest uci) {
        Channel cnl = getChannelOrException(id);

        log.debug("channel id - " + cnl.getId() + "updating...");

        channelTypeCheck(cnl);


        cnl.setName(uci.newName());
        cnl.setDescription(uci.newDescription());

        channelRepository.save(cnl);
        log.info("channel updated - " + cnl.getName());

        return mapStructMapper.toDto(cnl,userDtoFromChannel(cnl),lastMessageAt(cnl));
    }

    @Override
    @Transactional
    public void deleteChannel(UUID id) {
        getChannelOrException(id);
        channelRepository.deleteById(id);
        log.info("channel deleted - " + id);
    }

    private User getUserOrException(UUID id){
        return userRepository.findById(id).stream().findFirst().orElseThrow(
                () -> new UserNotFoundException("User with id - {} not found",id)
        );
    }


    private void channelTypeCheck(Channel cnl){
        if (cnl.getType().equals(ChannelType.PRIVATE)) {
            throw new ChannelTypeException("Channel with id - {} was private",cnl.getId());
        }
    }

    private Channel getChannelOrException(UUID id){
        return channelRepository.findById(id).orElseThrow(
                () -> new ChannelNotFoundException("Channel with id - {} not found",id)
        );
    }


    private List<UserDto> userDtoFromChannel(Channel channel) {
        return readStatusRepository.findByChannelId(channel.getId())
                .stream()
                .map(
                        rs -> {
                            User user = rs.getUser();
                            BinaryContent bc = user.getProfile();
                            return mapStructMapper.toDto(
                                    user
                                    ,mapStructMapper.toDto(bc, mapperMethod.getByteFrom(bc))
                                    ,user.online()
                            );
                        }
                ).toList();
    }



    private Instant lastMessageAt(Channel channel) {
        return messageRepository.findLastestMessageByChannel(channel.getId())
                .stream()
                .findFirst()
                .map(Message::getCreatedAt)
                .orElse(null);
    }
}
