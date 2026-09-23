package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.basic.MessageReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageGuard 단위 테스트")
class MessageGuardTest {

    @Mock
    MessageReader messageReader;

    @Mock
    Message message;

    @Mock
    User author;

    @InjectMocks
    MessageGuard messageGuard;

    @Test
    @DisplayName("메시지 작성자이면 소유권 확인에 성공한다")
    void isOwner_returnsTrue_whenUserIsAuthor() {
        UUID messageId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        given(messageReader.getWithAuthor(messageId)).willReturn(message);
        given(message.getAuthor()).willReturn(author);
        given(author.getId()).willReturn(authorId);

        boolean result = messageGuard.isOwner(messageId, authorId);

        assertThat(result).isTrue();
        then(messageReader).should().getWithAuthor(messageId);
    }

    @Test
    @DisplayName("메시지 작성자가 아니면 소유권 확인에 실패한다")
    void isOwner_returnsFalse_whenUserIsNotAuthor() {
        UUID messageId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        given(messageReader.getWithAuthor(messageId)).willReturn(message);
        given(message.getAuthor()).willReturn(author);
        given(author.getId()).willReturn(authorId);

        boolean result = messageGuard.isOwner(messageId, requesterId);

        assertThat(result).isFalse();
        then(messageReader).should().getWithAuthor(messageId);
    }

    @Test
    @DisplayName("작성자가 탈퇴한 메시지는 소유권 확인에 실패한다")
    void isOwner_returnsFalse_whenMessageHasNoAuthor() {
        UUID messageId = UUID.randomUUID();
        given(messageReader.getWithAuthor(messageId)).willReturn(message);
        given(message.getAuthor()).willReturn(null);

        boolean result = messageGuard.isOwner(messageId, UUID.randomUUID());

        assertThat(result).isFalse();
        then(messageReader).should().getWithAuthor(messageId);
    }

    @Test
    @DisplayName("메시지가 존재하지 않으면 조회 예외를 전달한다")
    void isOwner_throwsMessageNotFoundException_whenMessageDoesNotExist() {
        UUID messageId = UUID.randomUUID();
        given(messageReader.getWithAuthor(messageId))
                .willThrow(new MessageNotFoundException(messageId));

        assertThatThrownBy(() -> messageGuard.isOwner(messageId, UUID.randomUUID()))
                .isInstanceOf(MessageNotFoundException.class);

        then(messageReader).should().getWithAuthor(messageId);
    }
}
