package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public ChannelResponse createPrivateChannel (ChannelPrivateRequest dto) {

        Channel channel = new Channel("PRIVATE_CHANNEL", "비공개 채널", ChannelType.PRIVATE);
        if (dto.channelIds() != null) {
            channel.assignUsers(dto.channelIds());
        }
        channelRepository.save(channel);
        if (dto.channelIds() != null) {
            for (UUID targetUserId : dto.channelIds()) {
                ReadStatus readStatus = new ReadStatus(channel, targetUserId);
                readStatusRepository.save(readStatus);
            }
        }
        return new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription());
    }

    @Override
    public ChannelResponse createPublicChannel (ChannelPublicRequest dto) {
        Channel channel = new Channel(dto.name(), dto.description(), ChannelType.PUBLIC);
        channelRepository.save(channel);
        return new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChannelResponse> findById(UUID id) {
        return channelRepository.findById(id)
                .map(c -> new ChannelResponse
                        (c.getId(),
                         c.getChannelTitles(),
                         c.getDescription()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> findAll(UUID userId){
        return channelRepository.findAll().stream()
                .filter(channel -> {
                    if (!channel.isPrivate()) {
                        return true;
                    }
                    return channel.getUserIds() != null && channel.getUserIds().contains(userId);
                })
                .map(channel -> new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> findAllByUserId(UUID userId){
        return channelRepository.findAll().stream()
                .filter(channel -> {
                    if (!channel.isPrivate()) {
                        return true;
                    }
                    return channel.getUserIds() != null && channel.getUserIds().contains(userId);
                })
                .map(channel -> new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    public ChannelResponse update(UUID uuid, ChannelPublicRequest dto) {
        Channel channel = channelRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널을 찾을 수 없습니다."));

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.updateTitles(dto.name(), dto.description());

        return new ChannelResponse(channel.getId(), channel.getChannelTitles(), channel.getDescription());
    }

    @Override
    public void delete(UUID id) {
        messageRepository.deleteByChannelId(id);
        readStatusRepository.deleteByChannelId(id);

        channelRepository.deleteById(id);
    }

}
