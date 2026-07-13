package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusMapper readStatusMapper;

    @Override
    public ReadStatusDto create(ReadStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + request.userId() + " 를 찾을 수 없습니다."));

        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NoSuchElementException("채널 ID: " + request.channelId() + " 를 찾을 수 없습니다."));

        if (readStatusRepository.existsByUserIdAndChannelId(request.userId(), request.channelId())) {
            throw new IllegalArgumentException("이미 해당 유저의 채널 읽음 상태가 존재합니다.");
        }

        ReadStatus readStatus = ReadStatus.create(user, channel, request.lastReadAt());
        ReadStatus savedReadStatus = readStatusRepository.save(readStatus);

        log.info("ReadStatus: {}가 생성됨.", savedReadStatus.getId());
        return readStatusMapper.toDto(savedReadStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadStatusDto find(UUID readStatusId) {
        ReadStatus readStatus = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new NoSuchElementException("읽음 상태 ID: " + readStatusId + " 를 찾을 수 없습니다."));

        return readStatusMapper.toDto(readStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("유저 ID: " + userId + " 를 찾을 수 없습니다.");
        }

        return readStatusRepository.findAllByUserId(userId).stream()
                .map(readStatusMapper::toDto)
                .toList();
    }

    @Override
    public ReadStatusDto update(ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(request.readStatusId())
                .orElseThrow(() -> new NoSuchElementException("읽음 상태 ID: " + request.readStatusId() + " 를 찾을 수 없습니다."));

        readStatus.updateLastReadAt(request.newLastReadAt());

        log.info("ReadStatus: {}가 수정됨.", readStatus.getId());
        return readStatusMapper.toDto(readStatus);
    }

    @Override
    public void delete(UUID readStatusId) {
        if (!readStatusRepository.existsById(readStatusId)) {
            throw new NoSuchElementException("읽음 상태 ID: " + readStatusId + " 를 찾을 수 없습니다.");
        }

        readStatusRepository.deleteById(readStatusId);
        log.info("ReadStatus: {}가 삭제됨.", readStatusId);
    }
}