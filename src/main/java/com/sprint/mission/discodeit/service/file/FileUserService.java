package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class FileUserService implements Serializable, UserService {

   private final UserRepository repository;

   public FileUserService(UserRepository repository) {
       this.repository = repository;
   }

    @Override
    public void create(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("유저이름이 없습니다.");
        }
        repository.create(user);
    }

    @Override
    public User read(UUID id) {
       if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수가 없습니다.");
        }

       User user = repository.read(id);

       if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }
       return user;
    }

    @Override
    public List<User> readAll() {
        return repository.readAll();
    }

    @Override
    public void update(UUID id, User user) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수 없습니다.");
        }
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("유저가 없습니다.");
        }
        if (repository.read(id) == null){
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
            }
        repository.update(id, user);
        }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }
        if (!repository.exists(id)) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }
        repository.delete(id);
    }

}

