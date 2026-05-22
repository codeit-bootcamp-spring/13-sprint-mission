package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUserService implements UserService {

    //필드
    private final List<User> users = new ArrayList<>();
    private final Path binPath = Path.of("data/users.ser");

    //생성자
    public FileUserService() {
        File file = new File("data/users.ser");
        // 파일 저장 위치에 파일이 존재한다면 로드하도록.
        if (file.exists())
            loadFromBinary();

    }

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
            oos.writeObject(new ArrayList<>(users));
        } catch (IOException e) {
            throw new RuntimeException("직렬화에 실패했습니다.");
        }
    }

    //역직렬화 메서드
    private void loadFromBinary() {

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(binPath)))) {
            List<User> usersTemp = (List<User>) ois.readObject();
            // 일단 users 초기화
            if (users != null)
                users.clear();
            // 유저에 역직렬화한 List<User> 넣기
            users.addAll(usersTemp);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("역직렬화에 실패했습니다.");
        } catch (IOException e) {
            throw new RuntimeException("역직렬화에 실패했습니다22.");
        }
    }


    @Override
    public User createUser(String name, String email) {
        return null;
    }

    @Override
    public void printUserInfo(User user) {

    }

    @Override
    public void printAllUsersInfo() {

    }

    @Override
    public void changeName(User user, String newName) {

    }

    @Override
    public void changeEmail(User user, String newEmail) {

    }

    @Override
    public User deleteUser(User user) {
        return null;
    }

    @Override
    public void joinChannel(User user, Channel channel) {

    }

    @Override
    public void printMyChannelsInfo(User user) {

    }

    @Override
    public void leaveChannel(User user, Channel channel) {

    }

    @Override
    public void printMessages(User user) {

    }
}
