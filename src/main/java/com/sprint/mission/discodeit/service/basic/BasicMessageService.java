package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFSelectFilter;
import com.sprint.mission.discodeit.service.MessageService;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class BasicMessageService implements MessageService {
    private final FileMessageRepository fms = new FileMessageRepository(Paths.get("msg.ser"));
    private final FileUserRepository fus = new FileUserRepository(Paths.get("user.ser"));
    private final FileChannelRepository fcs = new FileChannelRepository(Paths.get("channel.ser"));

    @Override
    public void createMessage(UUID user, UUID channel, String data){

        JCFSelectFilter<User> fltu =  (c) -> c.getId().equals(user);
        JCFSelectFilter<Channel> fltc =  (c) -> c.getId().equals(channel);
        try {
            User u = fus.select(fltu).get(0);
            Channel c = fcs.select(fltc).get(0);
            fms.create(u,c,data);
        }
        catch(Exception e){
            System.out.println("User/Channel UUID error");
        }
    }

    @Override
    public ArrayList<Message> readMessage(UUID id){
        JCFSelectFilter<Message> fltm = (c) -> c.getId().equals(id);
        return fms.select(fltm);
    }

    @Override
    public ArrayList<Message> readMessageAll(){
        JCFSelectFilter<Message> flt = (c) -> true;
        return fms.select(flt);
    }

    @Override
    public void updateMessage(UUID id, String data){
        JCFSelectFilter<Message> fltm = (c) -> c.getId().equals(id);
        fms.update(fms.select(fltm).get(0), data);
    }

    @Override
    public void deleteMessage(UUID id){
        JCFSelectFilter<Message> fltm = (c) -> c.getId().equals(id);
        fms.delete(fms.select(fltm).get(0));
    }

}
