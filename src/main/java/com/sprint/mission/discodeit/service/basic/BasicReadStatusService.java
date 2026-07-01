package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        if(userRepository.findById(request.userId())==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }
        if(channelRepository.findById(request.channelId())==null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        List<ReadStatus> readStatuses = readStatusRepository.findAll();
        for (ReadStatus readStatus : readStatuses) {
            if(readStatus.getUserId().equals(request.userId()) && readStatus.getChannelId().equals(request.channelId())) {
                throw new IllegalArgumentException("해당 채널에 대한 읽기 상태가 존재합니다.");
            }
        }

        ReadStatus readStatus = new ReadStatus(request.userId(), request.channelId());
        readStatusRepository.save(readStatus);
        return returnResponse(readStatus);
    }

    @Override
    public ReadStatusResponse find(UUID id) {
        ReadStatus readStatus = readStatusCheck(id);
        return returnResponse(readStatus);
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        if(userRepository.findById(userId)==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }

        List<ReadStatus> readStatuses = readStatusRepository.findAll();
        List<ReadStatusResponse> responses = new ArrayList<>();
        for (ReadStatus readStatus : readStatuses) {
            if(readStatus.getUserId().equals(userId)) {
                responses.add(returnResponse(readStatus));
            }
        }

        return responses;
    }

    @Override
    public ReadStatusResponse update(ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusCheck(request.id());

        readStatus.updateLastReadAt(request.lastReadAt());
        readStatusRepository.save(readStatus);
        return returnResponse(readStatus);
    }

    @Override
    public void delete(UUID id) {
        ReadStatus readStatus = readStatusCheck(id);
        readStatusRepository.delete(id);
    }

    private ReadStatus readStatusCheck(UUID id) {
        ReadStatus readStatus = readStatusRepository.findById(id);
        if(readStatus==null) {
            throw new IllegalArgumentException("존재하지 않는 읽기 상태입니다.");
        }
        return readStatus;
    }

    private ReadStatusResponse returnResponse(ReadStatus readStatus) {
        return new ReadStatusResponse(readStatus.getId(), readStatus.getUserId(), readStatus.getChannelId(), readStatus.getUpdatedAt());
    }
}
