package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.readStatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReadStatusReader {

    private final ReadStatusRepository readStatusRepository;

    public ReadStatus getReadStatus(UUID readStatusId){
        return readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));
    }

    /**
     * 채널의 읽음 상태에 연결된 사용자 ID를 조회해 비공개 채널 파일 이벤트의 수신자를 정한다.
     */
    public List<UUID> getUserIdsByChannelId(UUID channelId){
        return readStatusRepository.findByChannelId(channelId)
                .stream()
                .map(read -> read.getUser().getId())
                .toList();
    }
}
