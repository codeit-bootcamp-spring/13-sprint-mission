package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class FileMessageRepository implements MessageRepository {

  private Map<UUID, Message> database;
  private final Path filePath;

  public FileMessageRepository(String fileDirectory) {
    this.filePath = Path.of(fileDirectory, "message.ser");

    try {
      Files.createDirectories(filePath.getParent());
    } catch (IOException e) {
      throw new RuntimeException("데이터 디렉토리 생성 실패", e);
    }

    this.database = load();
  }

  @Override
  public Message save(Message message) {
    database.put(message.getId(), message);
    saveToFile();
    return message;
  }

  @Override
  public Optional<Message> findById(UUID id) {
    return Optional.ofNullable(database.get(id));
  }

  @Override
  public List<Message> findAll() {
    return new ArrayList<>(database.values());
  }

  @Override
  public void delete(UUID id) {
    database.remove(id);
    saveToFile();
  }

  @Override
  public void deleteByChannelId(UUID channelId) {
    boolean removed = database.values().removeIf(m -> m.getChannelId().equals(channelId));
    if (removed) {
      saveToFile();
    }
  }

  private void saveToFile() {
    try (ObjectOutputStream oos =
        new ObjectOutputStream(Files.newOutputStream(filePath))) {

      oos.writeObject(database);

    } catch (IOException e) {
      throw new RuntimeException("파일 저장 실패", e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<UUID, Message> load() {

    if (!Files.exists(filePath)) {
      return new HashMap<>();
    }

    try (ObjectInputStream ois =
        new ObjectInputStream(Files.newInputStream(filePath))) {

      return (Map<UUID, Message>) ois.readObject();

    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("파일 로드 실패", e);
    }
  }
}