package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@NoArgsConstructor
@ConditionalOnProperty(
        value = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFUserRepository implements UserRepository {

    //필드
    private final List<User> users = new ArrayList<>();

    //interface
    @Override
    public boolean existsUserById(UUID userId) {
        return users.stream()
                .anyMatch(user -> user.getId().equals(userId));
    }

    @Override
    public boolean existsUserByName(String name) {
        return users.stream()
                .anyMatch(user -> user.getUsername().equals(name));
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return users.stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public void createUser(User user) {
        users.add(user);
    }

    @Override
    public Optional<User> findUserById(UUID userId) {
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    @Override
    public Optional<User> findUserByNameAndPassword(String username, String password) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return users;
    }

    @Override
    public void save() {

    }

    @Override
    public void deleteUser(UUID id) {
        users.remove(
                users.stream()
                        .filter(user -> user.getId().equals(id))
                        .findFirst()
                        .get()
        );
    }
}
