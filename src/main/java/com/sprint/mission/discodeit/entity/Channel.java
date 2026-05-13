package com.sprint.mission.discodeit.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Channel {

    //공통 필드
    private UUID id;
    private Long createdAt;
    private Long updatedAt;

    //필드
    private String name;
    private List<User> users;
    private List<Message> messages;

    //ctor
    public Channel(String name) {
        id = UUID.randomUUID();
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();

        this.name = name;
        users = new ArrayList<>();
        messages = new ArrayList<>();
    }

    //getter
    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getName() {
        return name;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Message> getMessages() {
        return messages;
    }

    //setter
    public void setName(String name) {
        if (name == null || name.isBlank())
            return;

        this.name = name;
    }

    //method
    public void addUser(User user) {
        if (users.contains(user) || user == null)
            return;

        users.add(user);
    }
    public void removeUser(User user) {
        if (!users.contains(user) || user == null)
            return;

        users.remove(user);
    }
    public void addMessage(Message message) {
        if (messages.contains(message) || message == null)
            return;

        messages.add(message);
    }
    public void removeMessage(Message message) {
        if (!messages.contains(message) || message == null)
            return;

        messages.remove(message);
    }

}
