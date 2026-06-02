package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class FileMessageRepository extends FileBaseRepository implements MessageRepository {
    private final Path DIRECTORY;

    public FileMessageRepository() {
        super();
        DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","message");
    }

    @Override
    public void create(User user, Channel channel, String data){
        Message msg = new Message(user.getId(), channel.getId(), data);
        this.save(DIRECTORY.resolve(msg.getId().toString() + ".ser"),msg);
    }

    @Override
    public ArrayList<Message> select(Predicate<Message> fn){
        try {
            List<Message> d = Files.list(DIRECTORY)
                    .map(c -> (Message) load(DIRECTORY.resolve(c)))
                    .toList();
            return new ArrayList<>(d.stream()
                    .filter(fn)
                    .toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    @Override
    public void update(UUID id, String data){
        try {
            Files.list(DIRECTORY)
                    .filter(path -> path.equals(DIRECTORY.resolve(id.toString() + ".ser")))
                    .map(c -> {
                        Message msg= load(DIRECTORY.resolve(c));
                        msg.setMessages(data);
                        msg.setUpdatedAt(System.currentTimeMillis());
                        save(DIRECTORY.resolve(msg.getId().toString() + ".ser"),msg);
                        return null;
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(UUID id){
        try {
            Files.delete(DIRECTORY.resolve(id.toString() + ".ser"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
