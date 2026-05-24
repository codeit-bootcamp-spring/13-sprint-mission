package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileUserService implements UserService {

    //필드
    private final UserRepository userRepository;

    //ctor
    public FileUserService() {
        this.userRepository = new FileUserRepository();
    }

    //interface
    @Override
    public User createUser(String name, String email) {
        if (name == null || name.isBlank()) throw new RuntimeException("에러: 이름은 공백일 수 없습니다.");
        if (email == null || email.isBlank()) throw new RuntimeException("에러: 이메일은 공백일 수 없습니다.");

        if (userRepository.existsUser(email)){
            System.out.println("에러: 이메일: " + email + "은 이미 사용중인 이메일입니다. 유저 생성 거부.\n" );
            return null;
        }

        User user = new User(name, email);
        userRepository.createUser(user);
        System.out.println("유저: " + name + "가 생성됨.\n" );

        return user;
    }

    @Override
    public void printUserInfo(User user) {
        if (user == null) throw new RuntimeException("에러: 출력하려는 유저는 null이면 안됩니다.");

        User userTemp = userRepository.findUser(user)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        System.out.println(userTemp + "\n");
    }

    @Override
    public void printAllUsersInfo() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            System.out.println(user + "\n");
        }
    }

    @Override
    public void changeName(User user, String newName) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");
        if (newName == null || newName.isBlank()) throw new RuntimeException("에러: 새 이름은 공백일 수 없습니다.\n");

        User userTemp = userRepository.findUser(user)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        System.out.println("유저명: " + userTemp.getName() + "가 수정됨.\n -> " + newName + "\n");
        userTemp.changeName(newName);

        userRepository.save();
    }

    @Override
    public void changeEmail(User user, String newEmail) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");
        if (newEmail == null || newEmail.isBlank()) throw new RuntimeException("에러: 새 이메일은 공백일 수 없습니다.\n");

        if (userRepository.existsUser(newEmail)){
            System.out.println("에러: 이메일: " + newEmail + "은 이미 사용중인 이메일입니다. 이메일 업데이트 거부.\n" );
            return;
        }

        User userTemp = userRepository.findUser(user)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        System.out.println("유저 이메일: " + userTemp.getEmail() + "가 수정됨.\n -> " + newEmail + "\n");
        userTemp.changeEmail(newEmail);

        userRepository.save();
    }

    @Override
    public User deleteUser(User user) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");

        User userTemp = userRepository.findUser(user)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        for (Channel channel : userTemp.getChannels()) {
            channel.removeUser(userTemp);
        }

        System.out.println("유저: " + userTemp.getName() + "가 삭제됨.\n" );
        userRepository.deleteUser(userTemp);

        return null;
    }

    @Override
    public void joinChannel(User user, Channel channel) {
        if (channel == null || user == null) throw new RuntimeException("에러: 채널, 유저는 null이면 안됩니다.");
        if (channel.getUsers().contains(user)) throw new RuntimeException("에러: 이 채널에는 이미 해당 유저가 존재합니다.");

        User userTemp = userRepository.findUser(user)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        channel.addUser(userTemp);
        userTemp.addChannel(channel);
        System.out.println("채널: " + channel.getName() + "에 " + userTemp.getName() + "가 추가됨.\n" );

        userRepository.save();
    }

    @Override
    public void printMyChannelsInfo(User user) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");

        User userTemp = userRepository.findUser(user)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        System.out.println(user.getName() + "가 가입한 채널: ");
        for (Channel channel : userTemp.getChannels()) {
            System.out.println(channel.getName());
        }
        System.out.println();
    }

    @Override
    public void leaveChannel(User user, Channel channel) {
        if (channel == null || user == null) throw new RuntimeException("에러: 채널, 탈퇴하려는 유저는 null이면 안됩니다.");
        if (!channel.getUsers().contains(user)) throw new RuntimeException("에러: 이 유저는 애초에 이 채널에 없습니다.");

        User userTemp = userRepository.findUser(user)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        channel.removeUser(userTemp);
        userTemp.removeChannel(channel);
        System.out.println(channel.getName() + "채널에서 " + userTemp.getName() + "가 퇴장했습니다.\n");

        userRepository.save();
    }

    @Override
    public void printMessages(User user) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");

        User userTemp = userRepository.findUser(user)
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        System.out.println(userTemp.getName() + "가 작성한 메세지들: ");
        for (Message message : userTemp.getMessages()) {
            System.out.println(message.getMessage());
        }
        System.out.println();
    }
}
