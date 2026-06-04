package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMessageRepository implements MessageRepository {

    private final Path filePath = Paths.get("messages.ser");

    private List<Message> loadFromFile(){
        if(!Files.exists(filePath)){
            return new ArrayList<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toString()))){
            return (List<Message>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("파일 로드 실패", e);
        }
    }

    private void saveToFile(List<Message> messages){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))){
            oos.writeObject(messages);
        }catch (IOException e){
            throw new RuntimeException("파일 저장 실패", e);
        }

    }

    @Override
    public void save(Message message) {
        List<Message> messages = loadFromFile();
        messages.add(message);
        saveToFile(messages);

    }

    @Override
    public Message findById(UUID id) {
        List<Message> messages = loadFromFile();
        for(Message message : messages){
            if(message.getId().equals(id)){
                return message;
            }
        }
        return null;
    }

    @Override
    public List<Message> findAll() {
        return loadFromFile();
    }

    @Override
    public void delete(UUID id) {
        List<Message> messages = loadFromFile();
        messages.remove(findById(id));
        saveToFile(messages);

    }
}
