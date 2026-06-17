//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.*;
//import com.sprint.mission.discodeit.repository.*;
//import com.sprint.mission.discodeit.service.*;
//
//import java.util.*;
//
//public class JCFUserService implements UserService {
//
//    private final UserRepository repository;
//
//    public JCFUserService(UserRepository repository) {
//        this.repository = repository;
//    }
//
//    @Override
//    public User create(String userName, String email, String passWord) {
//        User user = new User(userName, email, passWord);
//
//        repository.create(user);
//
//        return user;
//    }
//
//
//    @Override
//    public User read(UUID id) {
//
//        if (id == null) {
//            throw new IllegalArgumentException("유저 ID는 필수입니다.");
//        }
//
//        User user = repository.find(id);
//
//        if (user == null) {
//            throw new IllegalArgumentException("유저 정보가 없습니다.");
//        }
//
//        return user;
//    }
//
//    @Override
//    public List<User> readAll() {
//        return repository.findAll();
//    }
//
//    @Override
//    public User update(
//            UUID id,
//            String userName,
//            String email,
//            String passWord
//    ) {
//
//        User user = repository.find(id);
//
//        user.updateUserName(userName);
//        user.updateEmail(email);
//        user.updatePassWord(passWord);
//
//        repository.update(id, user);
//
//        return user;
//    }
//
//    @Override
//    public void delete(UUID id) {
//        if (id == null) {
//            throw new IllegalArgumentException("유저 ID는 필수입니다.");
//        }
//
//        if (!repository.exists(id)) {
//            throw new IllegalArgumentException("삭제할 유저가 존재하지 않습니다.");
//        }
//        repository.delete(id);
//    }
//}
//
