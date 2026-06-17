package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public MessageResponse create(
            MessageCreateRequest request
    ) {

        if (!channelRepository.existsById(
                request.getChannelId()
        )) {

            throw new NoSuchElementException(
                    "Channel not found with id "
                            + request.getChannelId()
            );
        }

        if (!userRepository.existsById(
                request.getAuthorId()
        )) {

            throw new NoSuchElementException(
                    "Author not found with id "
                            + request.getAuthorId()
            );
        }

        Message message = new Message(
                request.getContent(),
                request.getChannelId(),
                request.getAuthorId()
        );

        message = messageRepository.save(message);

        if (request.getDatas() != null) {

            for (int i = 0;
                 i < request.getDatas().size();
                 i++) {

                BinaryContent attachment =
                        new BinaryContent(
                                null,
                                message.getId(),
                                request.getFileNames().get(i),
                                request.getContentTypes().get(i),
                                request.getDatas().get(i)
                        );

                binaryContentRepository.save(
                        attachment
                );
            }
        }

        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getChannelId(),
                message.getAuthorId()
        );
    }

    @Override
    public MessageResponse find(UUID messageId) {

        Message message = messageRepository
                .findById(messageId)

                .orElseThrow(() ->

                        new NoSuchElementException(

                                "Message with id "
                                        + messageId
                                        + " not found"
                        )
                );

        return new MessageResponse(

                message.getId(),

                message.getContent(),

                message.getChannelId(),

                message.getAuthorId()
        );
    }

    @Override
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public Message update(UUID messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message with id " + messageId + " not found"));
        message.update(newContent);
        return messageRepository.save(message);
    }

    @Override
    public void delete(UUID messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new NoSuchElementException("Message with id " + messageId + " not found");
        }
        messageRepository.deleteById(messageId);
    }
}
