package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
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

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    //생성
    @Override
    public ReadStatus create(ReadStatusCreateRequest request) {
        userRepository.findById(request.userId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 사용자 입니다."));
        channelRepository.findById(request.channelId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 채널 입니다."));

        boolean alreadyExists = readStatusRepository.findAllByUserId(request.userId())
                .stream()
                .anyMatch(rs -> rs.getChannelId().equals(request.channelId()));

        if (alreadyExists){
            throw new IllegalArgumentException("이미 존재하는 ReadStatus입니다.");
        }

        ReadStatus readStatus = new ReadStatus(request.userId(), request.channelId());
        readStatusRepository.save(readStatus);
        return readStatus;
    }
    @Override
    public ReadStatus find(UUID id) {
        return readStatusRepository.findById(id)
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 ReadStatus입니다."));
    }

    //전체 조회
    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return readStatusRepository.findAllByUserId(userId);
    }

    //수정
    @Override
    public ReadStatus update(UUID id, ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 ReadStatus입니다."));
        readStatus.updateLastReadAt(request.lastReadAt());

        readStatusRepository.save(readStatus);
        return readStatus;
    }

    //삭제
    @Override
    public void delete(UUID id) {
       readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 ReadStatus입니다."));

       readStatusRepository.deleteById(id);
    }
}
