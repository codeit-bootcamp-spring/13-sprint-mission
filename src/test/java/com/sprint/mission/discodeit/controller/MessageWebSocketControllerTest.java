package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MessageWebSocketControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageWebSocketController controller;

    @Test
    void createMessage_첨부파일_없이_메시지를_생성한다() {
        MessageCreateRequest request =
                new MessageCreateRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "웹소켓 메시지",
                        List.of()
                );

        controller.createMessage(request);

        then(messageService)
                .should()
                .create(
                        request,
                        List.of()
                );
    }
}