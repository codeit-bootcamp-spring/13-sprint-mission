package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;
    private final ChannelMapper channelMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ChannelDto createPublic(PublicChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PUBLIC ,request.channelName(), request.description());
        channelRepository.save(channel);
        log.info("공개 채널 생성 완료: id={}, name={}", channel.getId(), channel.getName());
        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepository.save(channel);

        for (UUID userId : request.userIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

            ReadStatus readStatus = new ReadStatus(user, channel);
            readStatusRepository.save(readStatus);
        }

        log.info("비공개 채널 생성 완료: id={}", channel.getId());
        return channelMapper.toDto(channel);
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        List<Channel> channels = channelRepository.findAll();

        Set<UUID> joinedChannelIds = readStatusRepository.findAllByUser_Id(userId).stream()
                .map(readStatus -> readStatus.getChannel().getId())
                .collect(Collectors.toSet());

        List<Channel> visibleChannels = new ArrayList<>();
        List<UUID> visibleChannelIds = new ArrayList<>();
        for (Channel channel : channels) {
            boolean isJoin = channel.getType() == ChannelType.PUBLIC || joinedChannelIds.contains(channel.getId());
            if (isJoin) {
                visibleChannels.add(channel);
                visibleChannelIds.add(channel.getId());
            }
        }

        List<ReadStatus> allReadStatuses = readStatusRepository.findAllByChannel_IdIn(visibleChannelIds);
        List<Message> allMessages = messageRepository.findAllByChannel_IdIn(visibleChannelIds);
        List<ChannelDto> responses = new ArrayList<>();
        for (Channel channel : visibleChannels) {
            List<UserDto> participants = findParticipantsOf(channel.getId(), allReadStatuses);
            Instant lastMessageAt = findLastMessageAtOf(channel.getId(), allMessages);
            responses.add(channelMapper.toDto(channel, participants, lastMessageAt));
        }

        return responses;
    }

    @Override
    @Transactional
    public ChannelDto update(UUID channelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));
        if(channel.getType()==ChannelType.PRIVATE) {
            throw new IllegalArgumentException("비공개 채널은 수정할 수 없습니다.");
        }

        channel.update(request.channelName(), request.description());
        channelRepository.save(channel);

        log.info("채널 수정 완료: id={}", channel.getId());
        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));

        channelRepository.delete(channel);
        log.info("채널 삭제 완료: id={}", id);
    }

    private List<UserDto> findParticipantsOf(UUID id, List<ReadStatus> allReadStatuses) {
        List<UserDto> participants = new ArrayList<>();
        for (ReadStatus readStatus : allReadStatuses) {
            if (readStatus.getChannel().getId().equals(id)) {
                participants.add(userMapper.toDto(readStatus.getUser()));
            }
        }
        return participants;
    }

    private Instant findLastMessageAtOf(UUID id, List<Message> allMessages) {
        Instant lastMessageAt = null;
        for (Message message : allMessages) {
            if (message.getChannel().getId().equals(id)) {
                if (lastMessageAt == null || message.getUpdatedAt().isAfter(lastMessageAt)) {
                    lastMessageAt = message.getUpdatedAt();
                }
            }
        }
        return lastMessageAt;
    }
}
