package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileMessageService implements MessageService {


    //private final Path messagePath =Path.of("data/messages.csv"); -> FileMessageRepository
    private final MessageRepository messageRepository=new FileMessageRepository();

    @Override
    public Message createMessage(UUID channelId, String content, Long createdAt) throws IOException {

        Message message=new Message(channelId, content, createdAt);

        return messageRepository.saveMessage(message);

    }

    @Override
    public Optional<Message> readMessage(UUID id) throws IOException {

        return messageRepository.findMessage(id);
    }


    @Override
    public List<Message> readMessages() throws IOException {

        return messageRepository.findMessages();
    }

    @Override
    public Message editMessage(UUID id, UUID newChannelId, String newContent, Long updatedAt) throws IOException {

        Message message=messageRepository.findMessage(id)
                .orElseThrow();
        message.updateMessage(newChannelId, newContent, updatedAt);
        messageRepository.saveMessage(message);

        return message;


    }

    @Override
    public void deleteMessage(UUID id) throws IOException {

        messageRepository.deleteMessage(id);

    }

}
