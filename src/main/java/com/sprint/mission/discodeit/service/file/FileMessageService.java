package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileMessageService implements MessageService, Serializable{

   private final MessageRepository repository;

   public FileMessageService(MessageRepository repository) {
       this.repository = repository;
   }

    @Override
    public Message read(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID가 없습니다.");
        }
        Message message = repository.read(id);
        if (message == null) {
            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
        }
        return message;
    }

    @Override
    public void create(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("메세지 정보가 없습니다.");
        }
        repository.create(message);
    }

    @Override
    public List<Message> readAll() {
        return repository.readAll();
    }

    @Override
    public void update(UUID id, Message message) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }

        if (message == null || message.getId() == null) {
            throw new IllegalArgumentException("메세지 정보가 없습니다.");
        }
        if (!repository.exists(id)) {
            throw new IllegalArgumentException("존재하지 않는 메세지 ID입니다.");
        }
        repository.update(id, message);
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