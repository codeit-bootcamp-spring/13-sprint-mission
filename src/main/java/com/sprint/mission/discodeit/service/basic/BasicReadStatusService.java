package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Override
    public ReadStatusResponse create(CreateReadStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("읽음 상태 생성 요청 정보가 없습니다.");
        }

        User user = userRepository.find(request.userId());
        if (user == null) {
            throw new IllegalArgumentException("유저의 아이디가 없습니다.");
        }

        Channel channel = channelRepository.find(request.channelId());
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        ReadStatus existingReadStatus =
                readStatusRepository.findByUserIdAndChannelId(
                        request.userId(),
                        request.channelId()
                );

        if (existingReadStatus != null) {
            throw new IllegalArgumentException("이미 해당 유저의 읽음 상태가 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(
                request.userId(),
                request.channelId()
        );

        readStatusRepository.create(readStatus);

        return ReadStatusResponse.from(readStatus);
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("유저 아이디는 필수입니다.");
        }

        List<ReadStatus> readStatuses =
                readStatusRepository.findAllByUserId(userId);
        return readStatuses.stream()
                .map(ReadStatusResponse::from)
                .toList();
    }

    @Override
    public ReadStatusResponse find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        ReadStatus readStatus = readStatusRepository.find(id);
        if (readStatus == null) {
            throw new IllegalArgumentException("해당 아이디 정보가 없습니다.");
        }

        return ReadStatusResponse.from(readStatus);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("읽음 상태 삭제 요청 정보가 없습니다.");
        }

        ReadStatus readStatus = readStatusRepository.find(id);
        if (readStatus == null) {
            throw new IllegalArgumentException("삭제할 상태 정보가 없습니다.");
        }
        readStatusRepository.delete(id);
    }

    @Override
    public void update(UUID id, UpdateReadStatusRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("읽음 상태 아이디는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("읽음 상태 수정 요청 정보가 없습니다.");
        }

        ReadStatus readStatus = readStatusRepository.find(id);
        if (readStatus == null) {
            throw new IllegalArgumentException("수정할 읽음 상태 정보가 없습니다.");
        }

        readStatus.markAsRead(request.lastReadTime());

        readStatusRepository.update(readStatus);
    }
}
