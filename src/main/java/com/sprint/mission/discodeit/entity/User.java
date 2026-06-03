package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class User extends EntityRoot implements Serializable {

    //필드
    private String name;
    private String email;
    private String password;
    private UUID profileId;
    private final List<Channel> channels;
    private final List<Message> messages;

    //ctor
    public User(String name, String email, String password, UUID profileId) {
        super();

        this.name = name;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
        channels = new ArrayList<>();
        messages = new ArrayList<>();
    }

    //update Method
    public void changeName(String name) {
        if (name == null || name.isBlank())
            return;

        this.name = name;
        updateUpdatedAt();
    }
    public void changeEmail(String email) {
        if (email == null || email.isBlank())
            return;

        this.email = email;
        updateUpdatedAt();
    }
    public void changePassword(String password) {
        if (password == null || password.isBlank())
            return;

        this.password = password;
        updateUpdatedAt();
    }
    public void updateProfileId(UUID profileId) {
        if (password == null)
            return;

        this.profileId = profileId;
        updateUpdatedAt();
    }


    //method
    public void addChannel(Channel channel) {
        if (channels.contains(channel) || channel == null)
            return;

        channels.add(channel);
        updateUpdatedAt();
    }
    public void removeChannel(Channel channel) {
        if (!channels.contains(channel) || channel == null)
            return;

        channels.remove(channel);
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
        return "[User: " + name + ", Email: " + email + "]";
    }
}
