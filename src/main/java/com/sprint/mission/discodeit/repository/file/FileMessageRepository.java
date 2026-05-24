package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileMessageRepository implements MessageRepository {

    //messages 저장 객체 및 저장 경로
    private final List<Message> messages = new ArrayList<>();
    private final Path binPath = Path.of("data/messages.ser");

    //ctor
    public FileMessageRepository() {
        File file = new File("data/messages.ser");
        // 파일 저장 위치에 파일이 존재한다면 로드하도록.
        if (file.exists())
            loadFromBinary();
    }

    //method
    //직렬화 메서드
    private void saveToBinary() {
        Path parent = binPath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성에 실패했습니다.");
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(binPath)))) {
            oos.writeObject(new ArrayList<>(messages));
        } catch (IOException e) {
            throw new RuntimeException("직렬화에 실패했습니다.");
        }
    }

    //역직렬화 메서드
    private void loadFromBinary() {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(binPath)))) {
            List<Message> messagesTemp = (List<Message>) ois.readObject();
            // 일단 messages 초기화
            if (messages != null)
                messages.clear();
            // 유저에 역직렬화한 List<Message> 넣기
            messages.addAll(messagesTemp);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("역직렬화에 실패했습니다.");
        } catch (IOException e) {
            throw new RuntimeException("역직렬화에 실패했습니다22.");
        }
    }

    //interface
    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public void createMessage(Message message) {
        messages.add(message);
        saveToBinary();
    }

    @Override
    public Optional<Message> findMessage(Message message) {
        if (messages.contains(message)){
            return Optional.of(message);
        }
        return Optional.empty();
    }

    @Override
    public List<Message> findAll() {
        return messages;
    }

    @Override
    public void deleteMessage(Message message) {
        messages.remove(message);
        saveToBinary();
    }
}
