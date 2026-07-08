package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.MapperMethod;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JAPReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
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
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicChannelService implements ChannelService {
    private final JPAChannelRepository channelRepository;
    private final JAPReadStatusRepository readStatusRepository;
    private final JPAUserRepository userRepository;
    private final JPAMessageRepository messageRepository;
    private final MapperMethod mapperMethod;
    private final MapStructMapper mapStructMapper;

    @Override
    @Transactional
    public ChannelDto createPublicChannel(PublicChannelCreateRequest cpb){
        Channel cnl = new Channel(cpb.name(), cpb.description(), ChannelType.PUBLIC);
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

        for (UUID pid : cpv.participantIds()){
            User user = userRepository.findById(pid).stream().findFirst().orElseThrow(
                    () -> new DiscodeitException(
                            "User by id - " + pid + " not existed."
                            ,"Channel"
                            ,400
                    )
            );
            readStatusRepository.save(new ReadStatus(user,cnl,Instant.now()));
        }
        return mapStructMapper.toDto(cnl,userDtoFromChannel(cnl),lastMessageAt(cnl));
    }

    @Override
    @Transactional
    public List<ChannelDto> findAllByUserID(UUID userID) {

        Stream<ChannelDto> pv = readStatusRepository.findWithDetailByUserId(userID)
                .stream()
                .map(rs -> mapStructMapper.toDto(
                        rs.getChannel()
                        , userDtoFromChannel(rs.getChannel())
                        , lastMessageAt(rs.getChannel())
                ));
        Stream<ChannelDto> pb = readStatusRepository.findWithDetailByChannelType(ChannelType.PUBLIC)
                .stream()
                .map(rs -> mapStructMapper.toDto(
                        rs.getChannel()
                        , userDtoFromChannel(rs.getChannel())
                        , lastMessageAt(rs.getChannel())
                ));

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

        return mapStructMapper.toDto(cnl,userDtoFromChannel(cnl),lastMessageAt(cnl));
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
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createdAt")));
        return messageRepository.findByChannelIdOrderByCreatedAtDesc(channel.getId(), pageable)
                .stream()
                .findFirst()
                .map(Message::getCreatedAt)
                .orElse(null);
    }
}
