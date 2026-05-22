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
    private final HashMap<UUID, Message> data = new HashMap<>();
    private final Path path;

    public FileMessageRepository(Path path) {
        super();
        this.path = path;
        HashMap<UUID, Message> fData = load(path);
        for (UUID k : fData.keySet()){
            this.data.put(k, fData.get(k));
        }
    }

    @Override
    public void create(User user, Channel channel, String data){
        for (int i = 0; i < 3; i ++){
            Message msg = new Message(user.getId(), channel.getId(), data);
            if (!this.data.containsKey(msg.getId())) {
                this.data.put(msg.getId(), msg);
                break;
            }
        }
    }

    @Override
    public ArrayList<Message> select(Predicate<Message> fn){
        return new ArrayList<> (this.data.values().stream()
                .filter(fn)
                .toList());
    }


    @Override
    public void update(Message pmsg, String data){
        Message msg = this.data.remove(pmsg.getId());

        msg.setMessages(data);
        msg.setUpdatedAt(System.currentTimeMillis());

    }

    @Override
    public void delete(Message usr){
        Message msg = this.data.remove(usr.getId());
    }

    public static FileMessageRepository open(Path path){
        return new FileMessageRepository(path);
    }

    public void close(){
        save(data,path);
    }
}
