package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    @Transactional
    public ChannelResponse createPublic(PublicChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PUBLIC ,request.channelName(), request.description());
        channelRepository.save(channel);
        return new ChannelResponse(channel.getId(), ChannelType.PUBLIC, channel.getName(), channel.getDescription(), null, List.of());
    }

    @Override
    @Transactional
    public ChannelResponse createPrivate(PrivateChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepository.save(channel);

        for (UUID userId : request.userIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

            ReadStatus readStatus = new ReadStatus(user, channel);
            readStatusRepository.save(readStatus);
        }

        return new ChannelResponse(channel.getId(), ChannelType.PRIVATE, null, null, null, request.userIds());
    }

    @Override
    public ChannelResponse find(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));

        Instant lastMessageAt = lastedAt(channel.getId());
        List<UUID> memberIds = joinMembers(channel);

        return new ChannelResponse(channel.getId(), channel.getType(), channel.getName(), channel.getDescription(), lastMessageAt, memberIds);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        List<ChannelResponse> responses = new ArrayList<>();
        List<Channel> channels = channelRepository.findAll();

        Set<UUID> joinedChannelIds = readStatusRepository.findAllByUser_Id(userId).stream()
                .map(readStatus -> readStatus.getChannel().getId())
                .collect(Collectors.toSet());

        for (Channel channel : channels) {
            boolean isJoin = channel.getType() == ChannelType.PUBLIC
                    || joinedChannelIds.contains(channel.getId());

            if(isJoin) {
                Instant lastMessageAt = lastedAt(channel.getId());
                List<UUID> memberIds = joinMembers(channel);

                responses.add(new ChannelResponse(channel.getId(), channel.getType(), channel.getName(), channel.getDescription(), lastMessageAt, memberIds));
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public ChannelResponse update(UUID channelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));
        if(channel.getType()==ChannelType.PRIVATE) {
            throw new IllegalArgumentException("비공개 채널은 수정할 수 없습니다.");
        }

        channel.update(request.channelName(), request.description());
        channelRepository.save(channel);

        Instant lastMessageAt = lastedAt(channel.getId());
        List<UUID> memberIds = joinMembers(channel);

        return new ChannelResponse(channel.getId(), channel.getType(), channel.getName(), channel.getDescription(), lastMessageAt, memberIds);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));

        channelRepository.delete(channel);
    }

    private Instant lastedAt(UUID channelId) {
        List<Message> messages = messageRepository.findAllByChannel_Id(channelId);
        Instant lastMessageAt = null;
        for (Message message : messages) {
            if(lastMessageAt==null || message.getUpdatedAt().isAfter(lastMessageAt)) {
                lastMessageAt = message.getUpdatedAt();
            }
        }
        return lastMessageAt;
    }

    private List<UUID> joinMembers(Channel channel) {
        List<UUID> memberIds = new ArrayList<>();
        if(channel.getType()==ChannelType.PUBLIC) return memberIds;

        List<ReadStatus> readStatuses = readStatusRepository.findAllByChannel_Id(channel.getId());
        for (ReadStatus readStatus : readStatuses) {
            memberIds.add(readStatus.getUser().getId());
        }
        return memberIds;
    }
}
