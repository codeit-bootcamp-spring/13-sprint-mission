package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.ArrayList;
import java.util.List;
/*
public class JCFMessageService implements MessageService {

    //필드
    private final MessageRepository messageRepository;

    //ctor
    public JCFMessageService() {
        this.messageRepository = new JCFMessageRepository();
    }

    //interface
    @Override
    public Message createMessage(User user, Channel channel, String message) {
        if (message == null || message.isBlank()) throw new RuntimeException("에러: 메세지는 공백일 수 없습니다.");
        if (message == null || user == null) throw new RuntimeException("에러: 메세지, 유저는 null이면 안됩니다.");
        if (!validateUserExistsChannel(channel, user)){
            System.out.println("에러: 해당 유저는 이 채널에 존재하지 않습니다.");
            return null;
        }

        Message newMessage = new Message(message, user, channel);
        messageRepository.createMessage(newMessage);

        user.addMessage(newMessage);
        channel.addMessage(newMessage);
        System.out.println("메세지: \n" + newMessage + "\n가 생성됨.\n" );

        return newMessage;
    }

    @Override
    public void printMessage(Message message) {
        if (message == null) throw new RuntimeException("에러: 메세지는 null이면 안됩니다.");

        Message messageTemp = messageRepository.findMessage(message)
                .orElseThrow(() -> new RuntimeException("에러: 해당 메세지는 데이터파일에 존재하지 않습니다."));

        System.out.println(messageTemp + "\n");
    }

    @Override
    public void printAllMessages() {
        List<Message> messages = messageRepository.findAll();
        for (Message message : messages) {
            System.out.println(message + "\n");
        }
    }

    @Override
    public void editMessage(Message message, User user, String newMessage) {
        if (newMessage == null || newMessage.isBlank()) throw new RuntimeException("에러: 새 메세지는 공백일 수 없습니다.\n");
        if (message == null || user == null) throw new RuntimeException("에러: 메세지, 유저는 null이면 안됩니다.\n");
        if (!validateMessageWriter(message, user)){
            System.out.println("에러: 해당 메세지 작성자가 아니므로 수정 불가.");
            return;
        }

        Message messageTemp = messageRepository.findMessage(message)
                .orElseThrow(() -> new RuntimeException("에러: 해당 메세지는 데이터파일에 존재하지 않습니다."));

        System.out.println("메세지: \n{" + messageTemp + "}가 수정됨.\n -> [Message: " + newMessage + "]\n");
        messageTemp.updateMessage(newMessage);
    }

    @Override
    public Message deleteMessage(Message message, User user) {
        if (message == null || user == null) throw new RuntimeException("에러: 메세지, 유저는 null이면 안됩니다.");
        if (!validateMessageWriter(message, user)){
            System.out.println("에러: 해당 메세지 작성자가 아니므로 삭제 불가.");
            return message;
        }

        Message messageTemp = messageRepository.findMessage(message)
                .orElseThrow(() -> new RuntimeException("에러: 해당 메세지는 데이터파일에 존재하지 않습니다."));

        messageTemp.getUser().removeMessage(messageTemp);
        messageTemp.getChannel().removeMessage(messageTemp);

        System.out.println("메세지: " + messageTemp + "가 삭제됨.\n" );
        messageRepository.deleteMessage(messageTemp);

        return null;
    }

    @Override
    public void printWriter(Message message) {
        if (message == null) throw new RuntimeException("에러: 메세지는 null이면 안됩니다.");

        Message messageTemp = messageRepository.findMessage(message)
                .orElseThrow(() -> new RuntimeException("에러: 해당 메세지는 데이터파일에 존재하지 않습니다."));

        System.out.println("Writer: " + messageTemp.getUser().getName());
    }

    @Override
    public void printChannel(Message message) {
        if (message == null) throw new RuntimeException("에러: 메세지는 null이면 안됩니다.");

        Message messageTemp = messageRepository.findMessage(message)
                .orElseThrow(() -> new RuntimeException("에러: 해당 메세지는 데이터파일에 존재하지 않습니다."));

        System.out.println("Wrote Channel Name: " + messageTemp.getChannel().getName());
    }

    //들어온 user가 message의 작성자이면 true반환, 아니면 false 반환
    private boolean validateMessageWriter(Message message, User user) {
        return message.getUser().getId().equals(user.getId());
    }

    //들어온 user가 해당 channel에 존재하면 true반환, 아니면 false 반환
    private boolean validateUserExistsChannel(Channel channel, User user) {
        return channel.getUsers().stream().anyMatch(userTemp -> userTemp.getId().equals(user.getId()));
    }
}
