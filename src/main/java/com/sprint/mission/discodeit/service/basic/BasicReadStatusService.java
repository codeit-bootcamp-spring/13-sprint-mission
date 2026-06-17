package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public ReadStatus createReadStatus(ReadStatusCreateRequest request) {
        //입력값 검증 처리하겠습니다
        validateUUID(request.userId());
        validateUUID(request.channelId());

        //존재하는 유저, 채널인지 검증
        validateUserExists(request.userId());
        validateChannelExists(request.channelId());

        //ReadStatus 존재 검증
        validateReadStatusExists(request.userId(), request.channelId());

        //ReadStatus 생성
        ReadStatus readStatus = new ReadStatus(request.userId(), request.channelId());
        readStatusRepository.createReadStatus(readStatus);
        log.info("ReadStatus가 생성됨.");

        return readStatus;
    }

    @Override
    public ReadStatus findReadStatus(UUID readStatusId) {
        //입력값 검증 처리하겠습니다
        validateUUID(readStatusId);

        //ReadStatus 검색
        ReadStatus readStatusTemp = readStatusRepository.findReadStatusById(readStatusId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 ReadStatus는 데이터파일에 존재하지 않습니다."));

        return readStatusTemp;
    }

    @Override
    public List<ReadStatus> findAllReadStatusByUserId(UUID userId) {
        //입력값 검증 처리하겠습니다
        validateUUID(userId);

        //ReadStatus들 검색
        List<ReadStatus> readStatusList = readStatusRepository.findAllReadStatusByUserId(userId);

        return readStatusList;
    }

    @Override
    public ReadStatusUpdateResponse updateReadStatus(ReadStatusUpdateRequest request) {
        //입력값 검증 처리하겠습니다
        validateUUID(request.readStatusId());
        validateUUID(request.userId());
        validateUUID(request.channelId());

        //ReadStatus 검색
        ReadStatus readStatusTemp = readStatusRepository.findReadStatusById(request.readStatusId())
                .orElseThrow(() -> new RuntimeException("에러: 해당 ReadStatus는 데이터파일에 존재하지 않습니다."));

        //ReadStatus 업데이트
        readStatusTemp.updateLastAccessTime();
        readStatusRepository.save();

        return ReadStatusUpdateResponse.from(readStatusTemp);
    }

    @Override
    public void deleteReadStatus(UUID readStatusId) {
        //입력값 검증 처리하겠습니다
        validateUUID(readStatusId);

        //ReadStatus 검색
        ReadStatus readStatusTemp = readStatusRepository.findReadStatusById(readStatusId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 ReadStatus는 데이터파일에 존재하지 않습니다."));

        //ReadStatus 삭제
        readStatusRepository.deleteReadStatusById(readStatusId);

        log.info("ReadStatus: {}가 삭제됨.", readStatusTemp.getId());
    }


    // 들어온 UUID 필드가 null인지 검증하는 메서드
    private void validateUUID(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("에러: 입력값이 Null입니다.");
        }
    }
    // 들어온 userId 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateUserExists(UUID userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new RuntimeException("유저: " + userId + "이 존재하지 않습니다.");
        }
    }
    // 들어온 channelId필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateChannelExists(UUID channelId) {
        if (!channelRepository.existsChannelById(channelId)) {
            throw new RuntimeException("채널: " + channelId + "이 존재하지 않습니다.");
        }
    }
    // 생성하려는 ReadStatus가 레포지터리에 이미 존재하는지 검증하는 메서드
    private void validateReadStatusExists(UUID userId, UUID channelId) {
        if (readStatusRepository.existsReadStatusByUserIdAndChannelId(userId, channelId)) {
            throw new RuntimeException("만들려는 ReadStatus가 이미 존재합니다.");
        }
    }
}
