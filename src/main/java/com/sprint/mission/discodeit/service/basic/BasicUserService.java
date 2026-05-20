package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFSelectFilter;
import com.sprint.mission.discodeit.service.UserService;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class BasicUserService implements UserService {
    private final FileUserRepository fur = new FileUserRepository(Paths.get("user.ser"));

    @Override
    public void createUser(String name, String id, String pw){
        fur.create(name, id, pw);
    }

    @Override
    public ArrayList<User> readUser(UUID id){
        JCFSelectFilter<Channel> flt = (c) -> c.getId().equals(id);
        return fur.select(flt);
    }

    @Override
    public ArrayList<User> readUserAll(){
        JCFSelectFilter<Channel> flt = ((c) -> true);
        return fur.select(flt);
    }

    @Override
    public void updateUser(UUID id, String name, String userID, String pw){
        fur.update(id, name, userID, pw);
    }

    @Override
    public void deleteUser(UUID id){
        fur.delete(id);
    }
}
