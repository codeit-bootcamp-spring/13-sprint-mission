package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.ArrayList;
import java.util.List;

public class JCFChannelService implements ChannelService {

    //필드
    private final List<Channel> channels;

    //ctor
    public JCFChannelService() {
        channels = new ArrayList<>();
    }

    //interface
    @Override
    public Channel createChannel(String name, User channelHost) {
        if (name == null || name.isBlank()) throw new RuntimeException("에러: 채널명은 공백일 수 없습니다.");
        if (channelHost == null) throw new RuntimeException("에러: 채널 호스트는 null이면 안됩니다.");

        Channel channel = new Channel(name, channelHost);
        channels.add(channel);

        System.out.println("채널: " + name + "가 생성됨.\n" );

        addUserToChannel(channel, channelHost);

        return channel;
    }

    @Override
    public void printChannelInfo(Channel channel) {
        if (channel == null) throw new RuntimeException("에러: 채널은 null이면 안됩니다.");

        System.out.println(channel + "\n");
    }

    @Override
    public void printAllChannelsInfo() {
        for (Channel channel : channels) {
            System.out.println(channel + "\n");
        }
    }

    @Override
    public void editChannelName(Channel channel, User user, String newName) {
        if (newName == null || newName.isBlank()) throw new RuntimeException("에러: 새 채널명은 공백일 수 없습니다.\n");
        if (channel == null || user == null) throw new RuntimeException("에러: 채널, 수정하려는 유저는 null이면 안됩니다.");
        if (channel.getChannelHost() != user) throw new RuntimeException("에러: 채널 이름을 수정하려는 유저는 이 채널 호스트여야 합니다.\n");

        System.out.println("채널명: " + channel.getName() + "가 수정됨.\n -> " + newName + "\n");
        channel.changeName(newName);
    }

    @Override
    public Channel deleteChannel(Channel channel, User user) {
        if (user == null || channel == null) throw new RuntimeException("에러: 채널, 유저는 null이면 안됩니다.");
        if (channel.getChannelHost() != user) throw new RuntimeException("에러: 이 채널의 호스트가 아니므로 채널 삭제 불가.");

        for (User users : channel.getUsers()) {
            users.removeChannel(channel);
        }
        for (Message messages : channel.getMessages()) {
            messages.getUser().removeMessage(messages);
        }
        channels.remove(channel);
        System.out.println("채널: " + channel.getName() + "가 삭제됨.\n" );
        return null;
    }

    @Override
    public void addUserToChannel(Channel channel, User user) {
        if (channel == null || user == null) throw new RuntimeException("에러: 채널, 유저는 null이면 안됩니다.");

        user.addChannel(channel);
        channel.addUser(user);
        System.out.println("채널: " + channel.getName() + "에 " + user.getName() + "가 추가됨.\n" );
    }

    @Override
    public void printUsersInfo(Channel channel) {
        if (channel == null) throw new RuntimeException("에러: 채널은 null이면 안됩니다.");

        System.out.println(channel.getName() + "채널 유저: ");
        for (User user : channel.getUsers()) {
            System.out.println(user.getName());
        }
        System.out.println();
    }

    @Override
    public void printChannelHostInfo(Channel channel) {
        if (channel == null) throw new RuntimeException("에러: 채널은 null이면 안됩니다.");

        System.out.println("채널 호스트: " + channel.getChannelHost());

    }

    @Override
    public void changeChannelHost(Channel channel, User user) {
        if (user == null || channel == null) throw new RuntimeException("에러: 채널, 유저는 null이면 안됩니다.");
        if (!channel.getUsers().contains(user)) throw new RuntimeException("에러: 해당 유저는 이 채널에 존재하지 않습니다.\n");

        System.out.println(channel.getName() + "채널 호스트가 " + channel.getChannelHost().getName() + "에서 " + user.getName() + "으로 변경됨.\n" );
        channel.changeChannelHost(user);
    }

    @Override
    public void deleteUserFromChannel(Channel channel, User user) {
        if (user == null || channel == null) throw new RuntimeException("에러: 채널, 유저는 null이면 안됩니다.");
        if (!channel.getUsers().contains(user)) throw new RuntimeException("에러: 이 채널에는 이 유저가 존재하지 않습니다.");

        channel.removeUser(user);
        user.removeChannel(channel);
        System.out.println(channel.getName() + "채널에서 " + user.getName() + "가 퇴장했습니다.\n");
    }

    @Override
    public void printMessages(Channel channel) {
        if (channel == null) throw new RuntimeException("에러: 채널은 null이면 안됩니다.");

        System.out.println(channel.getName() + "채널 메세지: ");
        for (Message message : channel.getMessages()) {
            System.out.println(message + "\n");
        }
    }
}
