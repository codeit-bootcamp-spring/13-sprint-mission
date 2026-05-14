package com.sprint.mission.discodeit.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Channel extends EntityRoot{

    //필드
    private String name;
    private User channelHost;
    private List<User> users;
    private List<Message> messages;

    //ctor
    public Channel(String name, User channelHost) {
        super();

        this.name = name;
        this.channelHost = channelHost;
        users = new ArrayList<>();
        messages = new ArrayList<>();
    }

    //getter
    public String getName() {
        return name;
    }

    public User getChannelHost() {
        return channelHost;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Message> getMessages() {
        return messages;
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

}
