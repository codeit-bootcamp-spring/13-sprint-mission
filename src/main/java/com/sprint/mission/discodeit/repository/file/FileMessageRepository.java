package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

public class FileMessageRepository extends FileBaseRepository implements MessageRepository {

    public FileMessageRepository(Path path) {
        super(path);
    }

    @Override
    public void create(User user, Channel channel, String data){
        HashMap<UUID, Message> userList = this.load();
        for (int i = 0; i < 3; i ++){
            Message msg = new Message(user.getId(), channel.getId(), data);
            if (!userList.containsKey(msg.getId())) {
                userList.put(msg.getId(), msg);
                break;
            }
        }
        this.save(userList);
    }

    @Override
    public ArrayList<Message> select(Predicate<Message> fn){
        HashMap<UUID,Message> msgList = this.load();
        return new ArrayList<> (msgList.values().stream()
                .filter(fn)
                .toList());
    }


    @Override
    public void update(Message pmsg, String data){
        HashMap<UUID,Message> msgList = this.load();
        Message msg = msgList.remove(pmsg.getId());

        msg.setMessages(data);
        msg.setUpdatedAt(System.currentTimeMillis());

        this.save(msgList);
    }

    @Override
    public void delete(Message usr){
        HashMap<UUID,Message> msgList = this.load();
        Message msg = msgList.remove(usr.getId());
    }
}
