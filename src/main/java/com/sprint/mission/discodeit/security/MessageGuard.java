package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.command.message.MessageCreateCommand;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.basic.MessageReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MessageGuard {

    private final MessageReader messageReader;

    public boolean isOwner(UUID messageId, UUID authenticatedUserId) {
        Message message = messageReader.getWithAuthor(messageId);

        User author = message.getAuthor();
        if (author == null) {
            // 탈퇴한 사용자가 작성한 메시지는 소유자를 확인할 수 없다.
            log.warn("메시지 {}의 작성자 정보가 없습니다.", messageId);
            return false;
        }

        if (!author.getId().equals(authenticatedUserId)) {
            log.warn("사용자 {}는 메시지 {}의 작성자가 아닙니다.", authenticatedUserId, messageId);
            return false;
        }

        return true;
    }

    public boolean isOwner(MessageCreateCommand command, UUID authenticatedUserId) {

        if (command == null || authenticatedUserId == null) {
            // MessageCreateCommand의 내용이 없다.
            return false;
        }
        if (command.userId() == null) {
            // MessageCreateCommand의 작성자의 아이디가 없다.
            return false;
        }

        if (!command.userId().equals(authenticatedUserId)) {
            log.warn("사용자 {}는 메시지 {}의 작성자가 아닙니다.", authenticatedUserId, command.userId());
            return false;
        }

        return true;
    }

}
