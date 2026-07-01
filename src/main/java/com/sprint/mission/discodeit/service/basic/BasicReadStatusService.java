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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    private ReadStatusResponse toResponse(ReadStatus readStatus){
        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getLastReadAt()
        );
    }

    //생성
    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        userRepository.findById(request.userId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 사용자 입니다."));
        channelRepository.findById(request.channelId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 채널 입니다."));

        boolean alreadyExists = readStatusRepository.findAllByUserId(request.userId())
                .stream()
                .anyMatch(rs -> rs.getChannelId().equals(request.channelId()));

        if (alreadyExists){
            return readStatusRepository.findAllByUserId(request.userId())
                    .stream()
                    .filter(rs -> rs.getChannelId().equals(request.channelId()))
                    .findFirst()
                    .map(this::toResponse)
                    .orElseThrow();
        }

        ReadStatus readStatus = new ReadStatus(request.userId(), request.channelId());
        readStatusRepository.save(readStatus);
        return toResponse(readStatus);
    }
    //조회
    @Override
    public ReadStatusResponse find(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 ReadStatus입니다."));
        return toResponse(readStatus);
    }

    //전체 조회
    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        List<ReadStatus> allByUserId = readStatusRepository.findAllByUserId(userId);
        return allByUserId.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //수정
    @Override
    public ReadStatusResponse update(UUID id, ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 ReadStatus입니다."));
        readStatus.updateLastReadAt(request.newLastReadAt());

        readStatusRepository.save(readStatus);
        return toResponse(readStatus);
    }

    //삭제
    @Override
    public void delete(UUID id) {
       readStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 ReadStatus입니다."));

       readStatusRepository.deleteById(id);
    }
}
