package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.MessageFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 파일 업로드 상태 이벤트의 수신 범위를 정하기 위해 파일이 속한 채널을 조회한다.
 * 채널을 찾지 못하면 호출자가 프로필 파일 여부를 별도로 확인할 수 있도록 Optional을 반환한다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageFileReader {

    private final MessageFileRepository messageFileRepository;

    public Optional<Channel> getChannelByBinaryContentId(UUID binaryContentId){
        return messageFileRepository.findChannelByBinaryContentId(binaryContentId);
    }
}
