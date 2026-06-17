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
public class BasicMessageService implements MessageService {

    private final MessageRepository repository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserRepository userRepository;

    @Override
    public MessageResponse find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID가 없습니다.");
        }

        Message message = repository.find(id);

        if (message == null) {
            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
        }

        List<BinaryContent> attachments =
                binaryContentRepository.findAllByMessageId(id);

        return MessageResponse.from(message, attachments);
    }

    @Override
    public MessageResponse create(MessageRequest.CreateMessageRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("메시지 생성 요청은 필수입니다.");
        }

        Channel channel = channelRepository.find(request.channelId());
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        User author = userRepository.find(request.authorId());
        if (author == null) {
            throw new IllegalArgumentException("존재하지 않는 작성자입니다.");
        }

        Message message = new Message(
                request.content(),
                request.channelId(),
                request.authorId()
        );

        repository.create(message);

        return MessageResponse.from(message, List.of());
    }

    @Override
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (channelRepository.find(channelId) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        return repository.findAllByChannelId(channelId)
                .stream()
                .map(message -> {
                    List<BinaryContent> attachments =
                            binaryContentRepository.findAllByMessageId(message.getId());

                    return MessageResponse.from(message, attachments);
                })
                .toList();
    }
    @Override
    public MessageResponse update(UUID id, MessageRequest.UpdateMessageRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }

        if (!repository.exists(id)) {
            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
        }

        Message message = repository.find(id);

        message.updateContent(request.content());

        repository.update(id, message);

        List<BinaryContent> attachments =
                binaryContentRepository.findAllByMessageId(id);


        return MessageResponse.from(message, attachments);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }
        if (!repository.exists(id)) {
            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
        }
        repository.delete(id);
    }
}
