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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public ChannelResponse createPublic(PublicChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PUBLIC ,request.channelName(), request.description());
        channelRepository.save(channel);
        return new ChannelResponse(channel.getId(), ChannelType.PUBLIC, channel.getChannelName(), channel.getDescription(), null, List.of());
    }

    @Override
    public ChannelResponse createPrivate(PrivateChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        channelRepository.save(channel);

        for (UUID userId : request.userIds()) {
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("존재하지 않는 계정입니다.");
            }

            ReadStatus readStatus = new ReadStatus(userId, channel.getId());
            readStatusRepository.save(readStatus);
        }

        return new ChannelResponse(channel.getId(), ChannelType.PRIVATE, null, null, null, request.userIds());
    }

    @Override
    public ChannelResponse find(UUID id) {
        Channel channel = channelRepository.findById(id);
        if(channel==null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        Instant lastMessageAt = lastedAt(channel.getId());
        List<UUID> memberIds = joinMembers(channel);

        return new ChannelResponse(channel.getId(), channel.getType(), channel.getChannelName(), channel.getDescription(), lastMessageAt, memberIds);
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        List<ChannelResponse> responses = new ArrayList<>();
        List<Channel> channels = channelRepository.findAll();

        for (Channel channel : channels) {
            boolean isJoin = false;

            if(channel.getType()==ChannelType.PUBLIC) {
                isJoin = true;
            }
            else {
                List<ReadStatus> readStatuses = readStatusRepository.findAll();
                for (ReadStatus readStatus : readStatuses) {
                    if(readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channel.getId())) {
                        isJoin = true;
                        break;
                    }
                }
            }

            if(isJoin) {
                Instant lastMessageAt = lastedAt(channel.getId());
                List<UUID> memberIds = joinMembers(channel);

                responses.add(new ChannelResponse(channel.getId(), channel.getType(), channel.getChannelName(), channel.getDescription(), lastMessageAt, memberIds));
            }
        }

        return responses;
    }

    @Override
    public ChannelResponse update(UUID channelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId);
        if(channel==null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        if(channel.getType()==ChannelType.PRIVATE) {
            throw new IllegalArgumentException("비공개 채널은 수정할 수 없습니다.");
        }

        channel.update(request.channelName(), request.description());
        channelRepository.save(channel);

        Instant lastMessageAt = lastedAt(channel.getId());
        List<UUID> memberIds = joinMembers(channel);

        return new ChannelResponse(channel.getId(), channel.getType(), channel.getChannelName(), channel.getDescription(), lastMessageAt, memberIds);
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id);
        if(channel==null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        List<Message> messages = messageRepository.findAll();
        for (Message message : messages) {
            if(message.getChannelId().equals(id)) {
                messageRepository.delete(message.getId());
            }
        }

        List<ReadStatus> readStatuses = readStatusRepository.findAll();
        for (ReadStatus readStatus : readStatuses) {
            if(readStatus.getChannelId().equals(id)) {
                readStatusRepository.delete(readStatus.getId());
            }
        }
        channelRepository.delete(id);
    }

    private Instant lastedAt(UUID channelId) {
        List<Message> messages = messageRepository.findAll();
        Instant lastMessageAt = null;
        for (Message message : messages) {
            if(message.getChannelId().equals(channelId)) {
                if(lastMessageAt==null || message.getUpdatedAt().isAfter(lastMessageAt)) {
                    lastMessageAt = message.getUpdatedAt();
                }
            }
        }
        return lastMessageAt;
    }

    private List<UUID> joinMembers(Channel channel) {
        List<UUID> memberIds = new ArrayList<>();
        if(channel.getType()==ChannelType.PRIVATE) {
            List<ReadStatus> readStatuses = readStatusRepository.findAll();

            for (ReadStatus readStatus : readStatuses) {
                if (readStatus.getChannelId().equals(channel.getId())){
                    memberIds.add(readStatus.getUserId());
                }
            }
        }
        return memberIds;
    }
}
