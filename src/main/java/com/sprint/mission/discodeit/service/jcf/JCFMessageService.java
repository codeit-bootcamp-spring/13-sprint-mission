package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.MessageService;

public class JCFMessageService implements MessageService {

    //ctor
    public JCFMessageService() {}

    // interface
    @Override
    public Message createMessage(User user, Channel channel, String message) {
        if (message == null || message.isBlank()) throw new RuntimeException("메세지는 공백일 수 없습니다.");
        if (message == null || user == null) throw new RuntimeException("메세지, 유저는 null이면 안됩니다.");
        if (!user.getChannels().contains(channel)) throw new RuntimeException("해당 유저는 이 채널에 존재하지 않습니다.");

        Message newMessage = new Message(message, user, channel);

        user.addMessage(newMessage);
        channel.addMessage(newMessage);
        System.out.println("메세지: " + message + " 가 생성됨." );
        return newMessage;
    }

    @Override
    public void printMessage(Message message) {
        if (message == null) throw new RuntimeException("메세지는 null이면 안됩니다.");

        System.out.println(message);
    }

    @Override
    public void editMessage(Message message, User user, String newMessage) {
        if (newMessage == null || newMessage.isBlank()) throw new RuntimeException("새 메세지는 공백일 수 없습니다.");
        if (message == null || user == null) throw new RuntimeException("메세지, 유저는 null이면 안됩니다.");
        if (user != message.getUser()) throw new RuntimeException("해당 메세지 작성자가 아니므로 수정 불가.");

        message.updateMessage(newMessage);
        System.out.println("메세지: " + message + "가 수정됨.\n -> " + newMessage);
    }

    @Override
    public Message deleteMessage(Message message, User user) {
        if (message == null || user == null) throw new RuntimeException("메세지, 유저는 null이면 안됩니다.");
        if (user != message.getUser()) throw new RuntimeException("해당 메세지 작성자가 아니므로 삭제 불가.");

        message.getUser().removeMessage(message);
        message.getChannel().removeMessage(message);
        System.out.println("메세지: " + message + "가 삭제됨." );
        return null;
    }

    @Override
    public void printWriter(Message message) {
        if (message == null) throw new RuntimeException("메세지는 null이면 안됩니다.");

        System.out.println("Writer: " + message.getUser().getName());
    }

    @Override
    public void printChannel(Message message) {
        if (message == null) throw new RuntimeException("메세지는 null이면 안됩니다.");

        System.out.println("Wrote Channel Name: " + message.getChannel().getName());
    }
}
