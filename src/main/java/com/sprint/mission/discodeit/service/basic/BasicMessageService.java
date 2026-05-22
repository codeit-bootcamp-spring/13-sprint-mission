package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class BasicMessageService implements MessageService {
    private final FileMessageRepository fms = new FileMessageRepository(Paths.get("data/msg.ser"));
    private final FileUserRepository fus = new FileUserRepository(Paths.get("data/user.ser"));
    private final FileChannelRepository fcs = new FileChannelRepository(Paths.get("data/channel.ser"));

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
    public ArrayList<Message> readMessage(UUID id){
        return fms.select((c) -> c.getId().equals(id));
    }

    @Override
    public ArrayList<Message> readMessageAll(){
        return fms.select((c) -> true);
    }

    @Override
    public void updateMessage(UUID id, String data){
        fms.update(
                fms.select((c) -> c.getId().equals(id))
                        .get(0),
                data);
    }

    @Override
    public void deleteMessage(UUID id){
        fms.delete(
                fms.select((c) -> c.getId().equals(id))
                        .get(0));
    }

}
