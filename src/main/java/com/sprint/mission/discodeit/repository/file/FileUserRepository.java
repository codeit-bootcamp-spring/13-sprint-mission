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
    static final String FilePath = "users.json";

    @Override
    public void save(User user) {
        List<User> users = findAll(); // 기존 파일 불러오기
        users.add(user);
        saveAll(users); // 다시 저장
    }

    // 파일 전체 확인
    @Override
    public List<User> findAll() {
        File file = new File(FilePath);
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
            objectMapper.writeValue(new File(FilePath), users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 나머지 findById, update, delete는 findAll() 후 처리
    @Override
    public Optional<User> findById(String id) {
        return findAll().stream().filter(u -> u.getId().equals(id)).findFirst(); }


    @Override
    public void update(User user) {
        List<User> users = findAll(); // 전체 목록 호출
        for (int i = 0; i < users.size(); i++) {
            // 같은 id를 가진 대상 확인
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user); // 해당 위치의 데이터를 새 정보로 교체
                break;
            }
        }
        saveAll(users); // 수정된 리스트 저장
    }
    @Override
    public void delete(String id) {
        List<User> users = findAll(); // 전체 목록 호출
        // 입력받은 id와 일치하지 않는 사용자만 남기기
        boolean removed = users.removeIf(user -> user.getId().equals(id));

        if (removed) {
            saveAll(users); // 수정된 리스트 저장
        }
    }
}