package com.sprint.mission.discodeit.repository.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Repository
public class FileUserRepository implements UserRepository {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String filePath = "users.json";

    @Override
    public void save(User user) {
        List<User> users = findAll(); // 기존 파일 불러오기
        users.add(user);
        saveAll(users); // 다시 저장
    }

    // 파일 전체 확인
    @Override
    public List<User> findAll() {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();
        try {
            // 파일을 User 리스트 객체로 변환
            return objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // 파일로 저장
    private void saveAll(List<User> users) {
        try {
            objectMapper.writeValue(new File(filePath), users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 나머지 findById, update, delete는 findAll() 후 처리
    // findById 수정완료(멘토님 코드리뷰)
    @Override
    public Optional<User> findById(String id) {
        return findAll().stream()
                .filter(u -> id.equals(u.getId())) // c.getId().equals(id) 에서 변경
                .findFirst();
    }

    // stream.map.filter로 표현(멘토님 코드리뷰)
    @Override
    public void update(User user) {
        List<User> users = findAll();
        // 스트림을 사용해 조건에 맞는 데이터만 변경 후 다시 리스트로 수집
        List<User> updatedUsers = users.stream()
                .map(u -> user.getId().equals(u.getId()) ? user : u)
                .toList();
        saveAll(updatedUsers);
    }

    // delete 수정(멘토님 코드리뷰)
    @Override
    public void delete(String id) {
        List<User> users = findAll();
        // channel.getId().equals(id) 에서 변경
        boolean removed = users.removeIf(user -> id.equals(user.getId()));

        if (removed) {
            saveAll(users);
        }
    }
}