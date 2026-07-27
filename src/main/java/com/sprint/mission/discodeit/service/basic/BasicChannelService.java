package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private final ChannelRepository repository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelMapper channelMapper;

    @Override
    @Transactional
    public ChannelDto createPublicChannel(CreatePublicChannelCommand publicChannel) {
        if (publicChannel == null) {
            throw new IllegalArgumentException("공개 채널 생성 요청은 필수입니다.");
        }
        if (publicChannel.name() == null || publicChannel.name().isBlank()) {
            throw new IllegalArgumentException("채널이름이 공백일 수는 없습니다.");
        }

        Channel channel = new Channel(
                publicChannel.name(),
                publicChannel.description(),
                ChannelType.PUBLIC
        );
        repository.save(channel);

        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public ChannelDto createPrivateChannel(CreatePrivateChannelCommand privateChannel) {
        if (privateChannel == null) {
            throw new IllegalArgumentException("비공개 채널 생성 요청은 필수입니다.");
        }
        if (privateChannel.participantIds() == null || privateChannel.participantIds().isEmpty()) {
            throw new IllegalArgumentException("비공개 채널 참여자는 필수입니다.");
        }
        List<User> participants = new ArrayList<>();
        Set<UUID> duplicateCheckSet = new HashSet<>();

        for (UUID participantId : privateChannel.participantIds()) {
            if (participantId == null) {
                throw new IllegalArgumentException("참여자 ID는 필수입니다.");
            }
            if (!duplicateCheckSet.add(participantId)) {
                throw new IllegalArgumentException("중복된 참여자 ID가 있습니다.");
            }

        User user = userRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 참여자 ID입니다."));
        participants.add(user);
        }


        Channel channel = new Channel(
                null,
                null,
                ChannelType.PRIVATE
        );

        repository.save(channel);

        for (User user : participants) {
            ReadStatus readStatus = new ReadStatus(user, channel);
            readStatusRepository.save(readStatus);
        }

        return channelMapper.toDto(channel);
    }



    @Override
    public ChannelDto find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        Channel channel = repository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 채널 ID입니다."));

        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public ChannelDto update(UUID id, CreatePublicChannelCommand request) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("채널 수정 요청은 필수입니다.");
        }

        Channel channel = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널 ID입니다."));

        channel.update(request.name(), request.description());
        return channelMapper.toDto(channel);
    }


    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        Channel channel = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널 ID입니다."));

        List<ReadStatus> readStatuses = readStatusRepository.findAllByChannelId(id);
        readStatusRepository.deleteAll(readStatuses);

        repository.delete(channel);
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 아이디는 필수입니다.");
        }

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        List<ReadStatus> readStatuses = readStatusRepository.findAllByUserId(userId);

        List<UUID> privateChannelIds = readStatuses.stream()
                .map(readStatus -> readStatus.getChannel().getId())
                .toList();

        return repository.findAll()
                .stream()
                .filter(channel ->
                        channel.getType() == ChannelType.PUBLIC
                                || privateChannelIds.contains(channel.getId())
                )
                .map(channelMapper::toDto)
                .toList();
    }

}
