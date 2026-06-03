package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {


    Message createOne(Message message) throws IOException;

    Optional<Message> readOne(UUID id) throws IOException;

    List<Message> readAll() throws IOException;

    void deleteOne(UUID id) throws IOException;
}
