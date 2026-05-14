package com.sprint.mission.discodeit.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Channel extends EntityRoot{

    //필드
    private String name;
    private List<User> users;
    private List<Message> messages;

    //ctor
    public Channel(String name) {
        super();

        this.name = name;
        users = new ArrayList<>();
        messages = new ArrayList<>();
    }

    //getter
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
