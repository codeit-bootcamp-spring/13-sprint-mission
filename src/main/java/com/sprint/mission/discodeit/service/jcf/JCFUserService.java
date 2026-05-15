package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

public class JCFUserService implements UserService {

    //ctor
    public JCFUserService() {}

    //interface
    @Override
    public User createUser(String name, String email) {
        if (name == null || name.isBlank()) throw new RuntimeException("에러: 이름은 공백일 수 없습니다.");
        if (email == null || email.isBlank()) throw new RuntimeException("에러: 이메일은 공백일 수 없습니다.");

        User user = new User(name, email);

        System.out.println("유저: " + name + "가 생성됨.\n" );
        return user;
    }

    @Override
    public void printUserInfo(User user) {
        if (user == null) throw new RuntimeException("에러: 출력하려는 유저는 null이면 안됩니다.");

        System.out.println(user + "\n");
    }

    @Override
    public void changeName(User user, String newName) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");
        if (newName == null || newName.isBlank()) throw new RuntimeException("에러: 새 이름은 공백일 수 없습니다.\n");

        System.out.println("유저명: " + user.getName() + "가 수정됨.\n -> " + newName + "\n");
        user.changeName(newName);
    }

    @Override
    public void changeEmail(User user, String newEmail) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");
        if (newEmail == null || newEmail.isBlank()) throw new RuntimeException("에러: 새 이메일은 공백일 수 없습니다.\n");

        System.out.println("유저 이메일: " + user.getEmail() + "가 수정됨.\n -> " + newEmail + "\n");
        user.changeEmail(newEmail);
    }

    @Override
    public User deleteUser(User user) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");

        for (Channel channel : user.getChannels()) {
            channel.removeUser(user);
        }

        System.out.println("유저: " + user.getName() + "가 삭제됨.\n" );
        return null;
    }

    @Override
    public void joinChannel(User user, Channel channel) {
        if (channel == null || user == null) throw new RuntimeException("에러: 채널, 유저는 null이면 안됩니다.");
        if (channel.getUsers().contains(user)) throw new RuntimeException("에러: 이 채널에는 이미 해당 유저가 존재합니다.");

        channel.addUser(user);
        user.addChannel(channel);

        System.out.println("채널: " + channel.getName() + "에 " + user.getName() + "가 추가됨.\n" );
    }

    @Override
    public void printMyChannelsInfo(User user) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");

        System.out.println(user.getName() + "가 가입한 채널: ");
        for (Channel channel : user.getChannels()) {
            System.out.println(channel.getName());
        }
        System.out.println();
    }

    @Override
    public void leaveChannel(User user, Channel channel) {
        if (channel == null || user == null) throw new RuntimeException("에러: 채널, 탈퇴하려는 유저는 null이면 안됩니다.");
        if (!channel.getUsers().contains(user)) throw new RuntimeException("에러: 이 유저는 애초에 이 채널에 없습니다.");

        channel.removeUser(user);
        user.removeChannel(channel);
        System.out.println(channel.getName() + "채널에서 " + user.getName() + "가 퇴장했습니다.\n");
    }

    @Override
    public void printMessages(User user) {
        if (user == null) throw new RuntimeException("에러: 유저는 null이면 안됩니다.");

        System.out.println(user.getName() + "가 작성한 메세지들: ");
        for (Message message : user.getMessages()) {
            System.out.println(message.getMessage());
        }
        System.out.println();

    }
}
