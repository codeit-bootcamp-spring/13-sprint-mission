package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//메시지 정보를 파일(message.dat)에 저장하는 Repository
public class FileMessageRepository implements MessageRepository {
    private final String filePath = "messages.dat"; //메시지 저장 파일
    private List<Message> messages = new ArrayList<>(); //메모리 메시지 목록

    public FileMessageRepository() {
        load();
    } //생성 시 파일 데이터 로드

    //메시지 파일
    @Override
    public Message save(Message message) {
        messages.add(message);
        saveToFile();
        return message;
    }

    //id로 메시지 조회
    @Override
    public Message findById(UUID id){
        for (Message message : messages) {
            if (message.getId().equals(id))
                return message;
        }
        return null;
    }

    //파일 저장
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))){
            oos.writeObject(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //파일 데이터 읽기
    @SuppressWarnings("unchecked")
    private void load() {
        File file = new File(filePath);
        if (!file.exists()) {
            messages = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            messages = (List<Message>) ois.readObject();
        } catch (Exception e) {
            messages = new ArrayList<>();
        }
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(messages);
    }

    @Override
    public void delete(UUID id) {

        messages.removeIf(
                user -> user.getId().equals(id)
        );

        saveToFile();
    }
}