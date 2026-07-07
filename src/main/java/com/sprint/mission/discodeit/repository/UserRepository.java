package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

//    boolean existsUserById(UUID userId);
//    boolean existsUserByName(String name);
//    boolean existsUserByEmail(String email);
//    void createUser(User user);
//    Optional<User> findUserById(UUID userId);
//    Optional<User> findUserByNameAndPassword(String username, String password);
//    List<User> findAll();
//    void save();
//    void deleteUser(UUID id);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"profile", "status"})
    List<User> findAll();
    Optional<User> findByUsernameAndPassword(String username, String password);


}
