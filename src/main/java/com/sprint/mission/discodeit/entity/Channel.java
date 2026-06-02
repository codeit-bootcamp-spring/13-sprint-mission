package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel extends EntityRoot implements Serializable {

    //필드
    private String name;
    private User channelHost;
    private final List<User> users;
    private final List<Message> messages;

    //ctor
    public Channel(String name, User channelHost) {
        super();

        this.name = name;
        this.channelHost = channelHost;
        users = new ArrayList<>();
        messages = new ArrayList<>();
    }

    //update Method
    public void changeName(String name) {
        if (name == null || name.isBlank())
            return;

        this.name = name;
        updateUpdatedAt();
    }
    public void changeChannelHost(User channelHost) {
        if (channelHost == null || channelHost == this.channelHost)
            return;

        this.channelHost = channelHost;
        updateUpdatedAt();
    }

    //method
    public void addUser(User user) {
        if (users.contains(user) || user == null)
            return;

        users.add(user);
        updateUpdatedAt();
    }
    public void removeUser(User user) {
        if (!users.contains(user) || user == null)
            return;

        users.remove(user);
        updateUpdatedAt();
    }
    public void addMessage(Message message) {
        if (messages.contains(message) || message == null)
            return;

        messages.add(message);
        updateUpdatedAt();
    }
    public void removeMessage(Message message) {
        if (!messages.contains(message) || message == null)
            return;

        messages.remove(message);
        updateUpdatedAt();
    }

    //method override
    @Override
    public String toString() {
        return "[Channel: " + name + ", Channel Host: " + channelHost.getName() + "]";
    }
}
