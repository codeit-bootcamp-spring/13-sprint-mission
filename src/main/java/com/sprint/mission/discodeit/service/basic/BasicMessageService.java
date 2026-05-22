package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;


import java.util.ArrayList;
import java.util.UUID;

public class BasicMessageService implements MessageService {
    private final MessageRepository fms;
    private final UserRepository fus;
    private final ChannelRepository fcs;
    public BasicMessageService(ChannelRepository chn, UserRepository usr, MessageRepository msg) {
        fms = msg;
        fus = usr;
        fcs = chn;
    }

    @Override
    public void createMessage(UUID user, UUID channel, String data){
        try {
            User u = fus.select((c) -> c.getId().equals(user)).get(0);
            Channel c = fcs.select((ch) -> ch.getId().equals(channel)).get(0);

            fms.create(u,c,data);
        }
        catch(Exception e){
            System.out.println("User/Channel UUID error");
        }
    }

    @Override
    public ArrayList<Message> getMessageById(UUID id){
        return fms.select((c) -> c.getId().equals(id));
    }

    @Override
    public ArrayList<Message> getMessageList(){
        return fms.select((c) -> true);
    }

    @Override
    public void updateMessageData(UUID id, String data){
        fms.update(id, data);
    }

    @Override
    public void deleteMessage(UUID id){
        fms.delete(id);
    }

}
