package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicReadStatusService implements ReadStatusService {

    //필드
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusMapper readStatusMapper;

    //interface
    @Override
    @Transactional
    public ReadStatusDto createReadStatus(ReadStatusCreateRequest request) {
        //유저 검색
        User userTemp = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));
        //채널 검색
        Channel channelTemp = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));

        //ReadStatus 존재 검증
        validateReadStatusExists(request.userId(), request.channelId());

        //ReadStatus 생성
        ReadStatus readStatus = new ReadStatus(userTemp, channelTemp);
        readStatus = readStatusRepository.save(readStatus);
        log.info("ReadStatus가 생성됨.");

        return readStatusMapper.toDto(readStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadStatusDto getReadStatus(UUID readStatusId) {
        //ReadStatus 검색
        ReadStatus readStatusTemp = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));

        return readStatusMapper.toDto(readStatusTemp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadStatusDto> getReadStatusesByUserId(UUID userId) {
        return readStatusRepository.findAllByUserId(userId).stream()
                .map(readStatusMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ReadStatusDto updateReadStatus(UUID readStatusId, ReadStatusUpdateRequest request) {
        //ReadStatus 검색
        ReadStatus readStatusTemp = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));

        //ReadStatus 업데이트
        readStatusTemp.updateLastReadAt();

        return readStatusMapper.toDto(readStatusTemp);
    }

    @Override
    @Transactional
    public void deleteReadStatus(UUID readStatusId) {
        //ReadStatus 검색
        ReadStatus readStatusTemp = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));

        //ReadStatus 삭제
        readStatusRepository.deleteById(readStatusId);

        log.info("ReadStatus: {}가 삭제됨.", readStatusTemp.getId());
    }

    // 생성하려는 ReadStatus가 레포지터리에 이미 존재하는지 검증하는 메서드
    private void validateReadStatusExists(UUID userId, UUID channelId) {
        if (readStatusRepository.existsByUserIdAndChannelId(userId, channelId)) {
            throw new ReadStatusAlreadyExistsException(userId, channelId);
        }
    }
}
