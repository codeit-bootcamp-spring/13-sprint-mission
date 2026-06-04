package com.sprint.mission.discodeit.repository.file;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Repository
public class FileUserRepository extends FileBaseRepository implements UserRepository {
    private final Path DIRECTORY = Paths.get(System.getProperty("user.dir"),"data","user");

    public FileUserRepository() {
        super();
    }

    @Override
    public void save(User user){
        this.save(DIRECTORY.resolve(user.getId()+ ".ser"),user);
    }

    @Override
    public List<User> find(Predicate<User> fn){
        try {
            List<User> d = Files.list(DIRECTORY)
                    .map(c -> (User) load(DIRECTORY.resolve(c)))
                    .toList();
            return d.stream()
                    .filter(fn)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }


    @Override
    public void update(UUID id, String name, String pw){
        try {
            Files.list(DIRECTORY)
                    .filter(path -> path.equals(DIRECTORY.resolve(id.toString() + ".ser")))
                    .map(c -> {
                        User user = load(DIRECTORY.resolve(c));
                        user.setName(name);
                        user.setPassword(pw);
                        user.setUpdatedAt();
                        save(DIRECTORY.resolve(user.getId().toString() + ".ser"),user);
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
