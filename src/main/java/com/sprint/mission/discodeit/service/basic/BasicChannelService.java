package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelPrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelMapper channelMapper;

    @Override
    public ChannelResponse createPrivateChannel(ChannelPrivateRequest dto) {
        Channel channel = new Channel("PRIVATE_CHANNEL", "비공개 채널", ChannelType.PRIVATE);

        if (dto.channelIds() != null) {
            channel.assignUsers(dto.channelIds());
        }

        Channel savedChannel = channelRepository.save(channel);

        if (dto.channelIds() != null) {
            for (UUID userId : dto.channelIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
                ReadStatus readStatus = new ReadStatus(savedChannel, user);
                readStatusRepository.save(readStatus);
            }
        }

        return channelMapper.toDto(savedChannel);
    }

    @Override
    public ChannelResponse createPublicChannel(ChannelPublicRequest dto) {
        Channel channel = new Channel(dto.name(), dto.description(), ChannelType.PUBLIC);
        Channel savedChannel = channelRepository.save(channel);
        return channelMapper.toDto(savedChannel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChannelResponse> findById(UUID id) {
        return channelRepository.findById(id)
                .map(channelMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> findAll(UUID userId) {
        return findAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        return channelRepository.findAll().stream()
                .filter(channel -> channel.getType() == ChannelType.PUBLIC
                        || channel.getUserIds() != null && channel.getUserIds().contains(userId))
                .map(channelMapper::toDto)
                .toList();
    }

    @Override
    public ChannelResponse update(UUID id, ChannelPublicRequest dto) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 채널을 찾을 수 없습니다."));

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.updateTitles(dto.name(), dto.description());

        return channelMapper.toDto(channel);
    }

    @Override
    public void delete(UUID id) {
        messageRepository.deleteByChannelId(id);
        readStatusRepository.deleteByChannelId(id);
        channelRepository.deleteById(id);
    }
}
