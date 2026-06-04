package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class FileUserService implements UserService {

   private final UserRepository repository;

   public FileUserService(UserRepository repository) {
       this.repository = repository;
   }

    @Override
    public User create(String userName, String email, String passWord) {
        User user = new User(userName, email, passWord);

        repository.create(user);

        return user;
    }

    @Override
    public User read(UUID id) {
       if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수가 없습니다.");
        }

       User user = repository.find(id);

       if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }
       return user;
    }

    @Override
    public List<User> readAll() {
        return repository.findAll();
    }

    @Override
    public User update(
            UUID id,
            String userName,
            String email,
            String passWord
    ) {

        User user = repository.find(id);

        user.updateUserName(userName);
        user.updateEmail(email);
        user.updatePassWord(passWord);

        repository.update(id, user);

        return user;
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

