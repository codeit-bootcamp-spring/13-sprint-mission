package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

//ReadStatusService 구현체
@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override //ReadStatus 생성
    public ReadStatus create(ReadStatusCreateRequest request) {
        UUID userId = request.getUserId();
        UUID channelId = request.getChannelId();

        if (!userRepository.existsById(userId)) { //유저 존재 검증
            throw new NoSuchElementException("user with id " + userId + " does not exist");
        }
        if (!channelRepository.existsById(channelId)) { //채널 존재 검증
            throw new NoSuchElementException("channel with id " + channelId + " does not exist");
        }
        //이미 같은 user-channel 관계가 존재하는지 확인
        if (readStatusRepository.findAllByUserId(userId).stream()
                .anyMatch(readStatus -> readStatus.getChannelId().equals(channelId))) {
            throw new NoSuchElementException("channel with userId " + userId + " and channelId " + channelId+ " already exists"); //IllegalStateException 참고
        }
        Instant lastReadAt = request.getLastReadAt();
        ReadStatus readStatus = new ReadStatus(userId, channelId, lastReadAt);
        return  readStatusRepository.save(readStatus);
    }

    @Override //단건조회
    public ReadStatus find(UUID readStatusId) {
        return readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new NoSuchElementException("readStatus with id " + readStatusId + " does not exist"));
    }
     @Override //유저 기준 전체 조회
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return readStatusRepository.findAllByUserId(userId).stream().toList();
     }

    @Override //업데이트(미구현 상태)
    public ReadStatus update(UUID readStatusId, ReadStatusUpdateRequest readStatus) {
        return null;
    }

    @Override //삭제
    public void delete(UUID readStatusId) {
        if (!readStatusRepository.existsById(readStatusId)) {
            throw new NoSuchElementException("readStatus with id " + readStatusId + " does not exist");
        }
        readStatusRepository.deleteById(readStatusId);
    }

}
