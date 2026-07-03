package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DuplicateResourceException;
import com.sprint.mission.discodeit.exception.ObjectNotFoundException;
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

    //interface
    @Override
    @Transactional
    public ReadStatus createReadStatus(ReadStatusCreateRequest request) {
        //유저 검색
        User userTemp = userRepository.findById(request.userId())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));
        //채널 검색
        Channel channelTemp = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 채널은 데이터파일에 존재하지 않습니다."));

        //ReadStatus 존재 검증
        validateReadStatusExists(request.userId(), request.channelId());

        //ReadStatus 생성
        ReadStatus readStatus = new ReadStatus(userTemp, channelTemp);
        readStatus = readStatusRepository.save(readStatus);
        log.info("ReadStatus가 생성됨.");

        return readStatus;
    }

    @Override
    @Transactional
    public ReadStatus findReadStatus(UUID readStatusId) {
        //ReadStatus 검색
        ReadStatus readStatusTemp = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 ReadStatus는 데이터파일에 존재하지 않습니다."));

        return readStatusTemp;
    }

    @Override
    @Transactional
    public List<ReadStatus> findAllReadStatusByUserId(UUID userId) {
        //ReadStatus들 검색
        List<ReadStatus> readStatusList = readStatusRepository.findAllByUserId(userId);

        return readStatusList;
    }

    @Override
    @Transactional
    public ReadStatus updateReadStatus(UUID readStatusId, ReadStatusUpdateRequest request) {
        //ReadStatus 검색
        ReadStatus readStatusTemp = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 ReadStatus는 데이터파일에 존재하지 않습니다."));

        //ReadStatus 업데이트
        readStatusTemp.updateLastReadAt();
        //dirty checking
        //readStatusTemp = readStatusRepository.save(readStatusTemp);

        return readStatusTemp;
    }

    @Override
    @Transactional
    public void deleteReadStatus(UUID readStatusId) {
        //ReadStatus 검색
        ReadStatus readStatusTemp = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 ReadStatus는 데이터파일에 존재하지 않습니다."));

        //ReadStatus 삭제
        readStatusRepository.deleteById(readStatusId);

        log.info("ReadStatus: {}가 삭제됨.", readStatusTemp.getId());
    }


    // 들어온 userId 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException("유저: " + userId + "이 존재하지 않습니다.");
        }
    }
    // 들어온 channelId필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateChannelExists(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new ObjectNotFoundException("채널: " + channelId + "이 존재하지 않습니다.");
        }
    }
    // 생성하려는 ReadStatus가 레포지터리에 이미 존재하는지 검증하는 메서드
    private void validateReadStatusExists(UUID userId, UUID channelId) {
        if (readStatusRepository.existsByUserIdAndChannelId(userId, channelId)) {
            throw new DuplicateResourceException("만들려는 ReadStatus가 이미 존재합니다.");
        }
    }
}
