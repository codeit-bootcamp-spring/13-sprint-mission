package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BasicReadStatusService implements ReadStatusService {
    // 의존성 주입
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        // 관련된 Channel이나 User가 존재하지 않으면 예외 발생
        channelRepository.findById(request.getChannelId())
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 채널입니다."));
        userRepository.findById(request.getUserId())
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 사용자입니다."));
        // 같은 Channel과 User와 관련된 객체가 이미 존재하면 예외 발생
        readStatusRepository.findById(request.getChannelId())
                .ifPresent(readStatus -> { throw new IllegalArgumentException("이미 존재하는 정보입니다.");});
        ReadStatus readStatus = new ReadStatus(request.getChannelId(), request.getUserId(), request.getLastReadAt());
        readStatusRepository.save(readStatus);
        return ReadStatusResponse.from(readStatus);
    }

    @Override
    public ReadStatusResponse find(UUID readStatusId) { // id로 조회
        ReadStatus readStatus = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new NoSuchElementException(readStatusId + " 를 찾을 수 없습니다."));
        return ReadStatusResponse.from(readStatus);
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) { // userId를 조건으로 조회
        return readStatusRepository.findAllByUserId(userId).stream()
                .map(ReadStatusResponse::from)
                .toList();
    }

    @Override
    public ReadStatusResponse update(UUID readStatusId, ReadStatusUpdateRequest request) {
        ReadStatus readStatus=readStatusRepository.findById(readStatusId)
                .orElseThrow(()->new NoSuchElementException(readStatusId+" 를 찾을 수 없습니다."));
        readStatus.update(request.getNewLastReadAt());
        readStatusRepository.save(readStatus);
        return ReadStatusResponse.from(readStatus);
    }

    @Override
    public void delete(UUID readStatusId) { // id로 삭제
        if (!readStatusRepository.existById(readStatusId)){
            throw new NoSuchElementException(readStatusId+" 를 찾을 수 없습니다.");
        }
        readStatusRepository.deleteById(readStatusId);
    }
}
