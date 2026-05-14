package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

public class JCFUserService implements UserService {


    //interface
    @Override
    public User createUser(String name, String email) {
        if (name == null || name.isBlank()) throw new RuntimeException("이름은 공백일 수 없습니다.");
        if (email == null || email.isBlank()) throw new RuntimeException("이메일은 공백일 수 없습니다.");

        User user = new User(name, email);

        return user;
    }

    @Override
    public void printUserInfo(User user) {
        if (user == null) throw new RuntimeException("출력하려는 유저는 null이면 안됩니다.");

        System.out.println(user);
    }

    @Override
    public void changeName(User user, String newName) {
        if (user == null) throw new RuntimeException("유저는 null이면 안됩니다.");
        if (newName == null || newName.isBlank()) throw new RuntimeException("새 이름은 공백일 수 없습니다.");

        user.changeName(newName);
    }

    @Override
    public void changeEmail(User user, String newEmail) {
        if (user == null) throw new RuntimeException("유저는 null이면 안됩니다.");
        if (newEmail == null || newEmail.isBlank()) throw new RuntimeException("새 이메일은 공백일 수 없습니다.");

        user.changeEmail(newEmail);
    }

    @Override
    public User deleteUser(User user) {
        if (user == null) throw new RuntimeException("유저는 null이면 안됩니다.");

        for (Channel channel : user.getChannels()) {
            channel.removeUser(user);
        }

        return null;
    }

    @Override
    public void joinChannel(User user, Channel channel) {
        if (channel == null || user == null) throw new RuntimeException("채널, 유저는 null이면 안됩니다.");
        if (channel.getUsers().contains(user)) throw new RuntimeException("이 채널에는 이미 해당 유저가 존재합니다.");

        channel.addUser(user);
        user.addChannel(channel);
    }

    @Override
    public void printMyChannelsInfo(User user) {
        if (user == null) throw new RuntimeException("유저는 null이면 안됩니다.");

        for (Channel channel : user.getChannels()) {
            System.out.println(channel);
        }
    }

    @Override
    public void LeaveChannel(User user, Channel channel) {
        if (channel == null || user == null) throw new RuntimeException("채널, 탈퇴하려는 유저는 null이면 안됩니다.");
        if (!channel.getUsers().contains(user)) throw new RuntimeException("이 유저는 애초에 이 채널에 없습니다.");

        channel.removeUser(user);
        user.removeChannel(channel);
    }

    @Override
    public void printMessages(User user) {
        if (user == null) throw new RuntimeException("유저는 null이면 안됩니다.");

        for (Message message : user.getMessages()) {
            System.out.println(message);
        }

    }
}
