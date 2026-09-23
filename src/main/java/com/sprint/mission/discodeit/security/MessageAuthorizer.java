package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메시지 소유권을 검사하는 컴포넌트.
 * <p>
 * @PreAuthorize의 SpEL에서 {@code @messageAuthorizer.isAuthor(...)} 형태로 참조한다.
 * 메시지 작성자는 DB를 조회해야 알 수 있어 SpEL만으로는 표현할 수 없기 때문이다.
 */
@Component("messageAuthorizer")
@RequiredArgsConstructor
public class MessageAuthorizer {

  private final MessageRepository messageRepository;

  /**
   * 존재하지 않는 메시지에 대해서도 false를 반환하여 리소스 존재 여부가 노출되지 않도록 한다.
   */
  @Transactional(readOnly = true)
  public boolean isAuthor(UUID messageId, UUID userId) {
    return messageRepository.findById(messageId)
        .map(Message::getAuthor)
        .map(User::getId)
        .map(authorId -> authorId.equals(userId))
        .orElse(false);
  }
}
