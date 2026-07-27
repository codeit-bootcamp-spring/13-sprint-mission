package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.*;
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
    private final ReadStatusMapper readStatusMapper;

    @Override
    @Transactional
    public ReadStatusDto create(CreateReadStatusCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("읽음 상태 생성 요청 정보가 없습니다.");
        }

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("유저의 아이디가 없습니다."));

        Channel channel = channelRepository.findById(command.channelId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

        Optional<ReadStatus> existingReadStatus =
                readStatusRepository.findByChannelIdAndUserId(
                        command.channelId(),
                        command.userId()
                );

        if (existingReadStatus.isPresent()) {
            ReadStatus readStatus = existingReadStatus.get();
            readStatus.update(command.lastReadAt());

            return readStatusMapper.toDto(readStatus);
        }

        ReadStatus readStatus = new ReadStatus(
                user,
                channel,
                command.lastReadAt()
        );

        ReadStatus savedReadStatus =
                readStatusRepository.save(readStatus);

        return readStatusMapper.toDto(savedReadStatus);
    }

    @Override
    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("유저 아이디는 필수입니다.");
        }

        List<ReadStatus> readStatuses =
                readStatusRepository.findAllByUserId(userId);
        return readStatusMapper.toDtoList(readStatuses);
    }

    @Override
    public ReadStatusDto find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 읽음 상태 정보가 없습니다."));
        return readStatusMapper.toDto(readStatus);
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
    public ReadStatusDto update(UUID id, UpdateReadStatusCommand command) {
        if (id == null) {
            throw new IllegalArgumentException("읽음 상태 아이디는 필수입니다.");
        }

        if (command == null) {
            throw new IllegalArgumentException("읽음 상태 수정 요청 정보가 없습니다.");
        }

        ReadStatus readStatus = readStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 읽음 상태 정보가 없습니다."));

        readStatus.update(command.lastReadTime());
        return readStatusMapper.toDto(readStatus);
    }
}
