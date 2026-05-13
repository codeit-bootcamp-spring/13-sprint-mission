package com.sprint.mission.discodeit.entity;

import java.util.ArrayList;
import java.util.UUID;

public class User {

    //공통 필드
    private UUID id;
    private Long createdAt;
    private Long updatedAt;

    //필드
    private String name;
    private String email;
    private ArrayList<User> friends;
    private ArrayList<Channel> channels;
    private ArrayList<Message> messages;

    //ctor
    public User(String name, String email, ArrayList<User> friends, ArrayList<Channel> channels, ArrayList<Message> messages) {
        id = UUID.randomUUID();
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();

        this.name = name;
        this.email = email;
        this.friends = friends;
        this.channels = channels;
        this.messages = messages;
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

    public String getEmail() {
        return email;
    }

    public ArrayList<User> getFriends() {
        return friends;
    }

    public ArrayList<Channel> getChannels() {
        return channels;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    //setter
    public void setName(String name) {
        if (name == null || name.isBlank())
            return;

        this.name = name;
    }
    public void setEmail(String email) {
        if (email == null || email.isBlank())
            return;

        this.email = email;
    }

    //method
    public void addFriend(User friend) {
        if (friends.contains(friend) || friend == null)
            return;

        friends.add(friend);
    }
    public void removeFriend(User friend) {
        if (!friends.contains(friend) || friend == null)
            return;

        friends.remove(friend);
    }
    public void addChannel(Channel channel) {
        if (channels.contains(channel) || channel == null)
            return;

        channels.add(channel);
    }
    public void removeChannel(Channel channel) {
        if (!channels.contains(channel) || channel == null)
            return;

        channels.remove(channel);
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
