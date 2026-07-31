package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReadStatusService implements ReadStatusService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    @Transactional
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 채널입니다."));

        if(readStatusRepository.existsByUser_IdAndChannel_Id(user.getId(), channel.getId())){
            throw new IllegalArgumentException("해당 채널에 대한 읽기 상태가 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(user, channel);
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
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }

        List<ReadStatus> readStatuses = readStatusRepository.findAllByUser_Id(userId);
        List<ReadStatusResponse> responses = new ArrayList<>();

        for (ReadStatus readStatus : readStatuses) {
            responses.add(returnResponse(readStatus));
        }

        return responses;
    }

    @Override
    @Transactional
    public ReadStatusResponse update(ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusCheck(request.id());

        readStatus.updateLastReadAt(request.lastReadAt());
        readStatusRepository.save(readStatus);
        return returnResponse(readStatus);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ReadStatus readStatus = readStatusCheck(id);
        readStatusRepository.delete(readStatus);
    }

    private ReadStatus readStatusCheck(UUID id) {
        return readStatusRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 읽기 상태입니다."));
    }

    private ReadStatusResponse returnResponse(ReadStatus readStatus) {
        return new ReadStatusResponse(readStatus.getId(), readStatus.getUser().getId(), readStatus.getChannel().getId(), readStatus.getUpdatedAt());
    }
}
