package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusUpdateCommand;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.readStatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readStatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final ReadStatusMapper readStatusMapper;
    private final ChannelReader channelReader;
    private final UserReader userReader;

    public void saveAll(Channel channel, List<UUID> userIds, Instant readAt) {
        if (userIds == null) throw new UserNotFoundException();

        List<UUID> distinctUserIds = userIds.stream().distinct().toList();
        if (distinctUserIds.isEmpty()) throw new UserNotFoundException();

        int insertedRowsCount = readStatusRepository.burkInsert(channel.getId(), distinctUserIds, readAt);

        if (insertedRowsCount != distinctUserIds.size()) throw new UserNotFoundException();
    }

    @PreAuthorize("#command.userId() == authentication.principal.userDto.id "
            + "and @channelGuard.isAccess(#channelId, authentication.principal.userDto.id)")
    public ReadStatusDto save(UUID channelId, ReadStatusCreateCommand command) {
        User user = getUserRequireThrow(command.userId());
        Channel channel = validateChannelAndReadStatus(command.userId(), channelId);

        ReadStatus save = readStatusRepository.save(new ReadStatus(channel, user, command));

        return readStatusMapper.toDto(save);
    }

    @Transactional(readOnly = true)
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return readStatusRepository.findByChannelId(channelId);
    }

    @Transactional(readOnly = true)
    public List<ReadStatus> findAllByChannelIds(List<UUID> channelIds) {
        if (channelIds.isEmpty()) return List.of();

        return readStatusRepository.findByChannelIdIn(channelIds);
    }

    public void deleteByChannelId(UUID channelId) {
        if (!readStatusRepository.existsByChannel_Id(channelId)) return;

        readStatusRepository.deleteByChannel_Id(channelId);
    }

    @Transactional(readOnly = true)
    public ReadStatusDto findById(UUID id) {
        ReadStatus readStatus = getReadStatusRequireThrow(id);
        return readStatusMapper.toDto(readStatus);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("#userId == authentication.principal.userDto.id")
    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        return readStatusRepository.findByUserId(userId)
                .stream()
                .map(readStatusMapper::toDto)
                .toList();
    }

    public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateCommand command) {
        ReadStatus readStatus = getReadStatusRequireThrow(readStatusId);

        readStatus.updateInfo(command);

        return readStatusMapper.toDto(readStatusRepository.save(readStatus));
    }

    public void delete(UUID id) {
        if (!readStatusRepository.existsById(id)) throw new ReadStatusNotFoundException(id);
        readStatusRepository.deleteById(id);
    }

    public void deleteByUserId(UUID userId) {
        if (!readStatusRepository.existsByUser_Id(userId)) return;

        readStatusRepository.deleteByUser_Id(userId);
    }

    private ReadStatus getReadStatusRequireThrow(UUID id) {
        return readStatusRepository.findById(id)
                .orElseThrow(() -> new ReadStatusNotFoundException(id));
    }

    private Channel getChannelRequireThrow(UUID channelId) {
        return channelReader.getChannel(channelId);
    }

    private User getUserRequireThrow(UUID userId) {
        return userReader.getUser(userId);
    }

    private Channel validateChannelAndReadStatus(UUID userId, UUID channelId) {
        Channel channel = getChannelRequireThrow(channelId);

        boolean hasReadStatus = readStatusRepository.existsByChannel_IdAndUser_Id(channelId, userId);
        if (hasReadStatus) {
            throw new ReadStatusAlreadyExistsException(channelId, userId);
        }

        return channel;
    }

}
