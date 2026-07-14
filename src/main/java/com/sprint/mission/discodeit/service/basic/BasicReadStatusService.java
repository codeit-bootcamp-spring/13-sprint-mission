package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Getter
public class BasicReadStatusService implements ReadStatusService {

    private final ReadStatusRepository readStatusRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReadStatusResponse create(CreateReadStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("읽음 상태 생성 요청 정보가 없습니다.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("유저의 아이디가 없습니다."));

        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

        Optional<ReadStatus> existingReadStatus =
                readStatusRepository.findByChannelIdAndUserId(request.userId(), request.channelId());

        if (existingReadStatus.isPresent()) {
            throw new IllegalArgumentException("이미 해당 유저의 읽음 상태가 존재합니다.");
        }

        ReadStatus readStatus = new ReadStatus(user, channel);

        readStatusRepository.save(readStatus);

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

        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 읽음 상태 정보가 없습니다."));
        return ReadStatusResponse.from(readStatus);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        ReadStatus readStatus = readStatusRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("삭제할 읽음 상태 정보가 없습니다."));
        readStatusRepository.delete(readStatus);
    }

    @Override
    public ReadStatusResponse update(UUID id, UpdateReadStatusRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("읽음 상태 아이디는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("읽음 상태 수정 요청 정보가 없습니다.");
        }

        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 읽음 상태 정보가 없습니다."));

        readStatus.update(request.lastReadTime());
        return ReadStatusResponse.from(readStatus);
    }
}
