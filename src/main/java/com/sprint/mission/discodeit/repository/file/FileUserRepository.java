package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUserRepository implements UserRepository {
    private final Path filePath = Paths.get("users.ser");

    private List<User> loadFromFile(){
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toString()))) {
           return (List<User>) ois.readObject();
        }catch (IOException | ClassNotFoundException  e){
            throw new RuntimeException("파일 로드 실패", e);
        }
    }

    private void saveToFile(List<User> users){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))){
            oos.writeObject(users);
        }catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }


    @Override
    public void save(User user) {
        List<User> users = loadFromFile();
        users.add(user);
        saveToFile(users);

    }

    @Override
    public User findById(UUID id) {
        List<User> users = loadFromFile();
        for (User user : users){
            if(user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        return loadFromFile();
    }

    @Override
    public void delete(UUID id) {
        List<User> users = loadFromFile();
        users.remove(findById(id));
        saveToFile(users);

    }
}
