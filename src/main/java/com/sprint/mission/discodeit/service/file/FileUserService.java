package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.IOException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUserService implements UserService {

    // 주소 설정
    private final Path directory = Paths.get(System.getProperty("user.dir"), "data");
    private final Path filePath = directory.resolve("users.ser");

    public FileUserService() {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 사용자 목록을 파일에 저장
    private void saveFile(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream
                (new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(users);
            } catch (IOException e) {
            throw new RuntimeException(e);
            }
        }

    // 파일에서 사용자 목록을 구출
    private  List<User> readFile() {
        if (!Files.exists(filePath)) {
            return new  ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream
                (new FileInputStream(filePath.toFile()))){
            return (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new  ArrayList<>();
        }
    }

    // 기능 1. 회원가입 (새로운 사용자 등록)
    @Override
    public User create(User user) {
        // 파일 창고에 있는 사용자 목록을 읽어와서
        List<User> users = readFile();
        // 매개변수로 들어온 user 객체 상자를 리스트에 추가
        users.add(user);
        // 사용자가 추가된 리스트(목록)을 다시 파일에 덮어씌워서 저장
        saveFile(users);
        // 가입(등록) 완료된 사용자 리턴
        return user;
    }

    @Override
    public User findById(UUID id) {
        // 파일에서 전체 목록을 가져온 뒤, 하나씩 돌면서 ID가 같은 걸 찾음
        List<User> users = readFile();
        for (User foundUser : users) {
            if (foundUser.getId().equals(id)) {
                return foundUser;
            }
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        // 파일에서 읽어온 목록을 그대로 화면에 리턴
        return readFile();
    }

    @Override
    public void update(User requestUser) {
        // 일단 파일에서 전체 사용자 목록을 가져온다
        List<User> users = readFile();
        // 리스트(목록) 안을 돌면서 수정하고 싶은 사용자 직접 찾기
        for (User foundUser : users) {
            if (foundUser.getId().equals(requestUser.getId())) {
                // 기존(User.java)에 생성한 이름 수정 규칙 실행
                foundUser.updateName(requestUser);
                break;
            }
        }
        // 이름과 수정 시간이 갱신된 목록을 파일에 다시 저장
        saveFile(users);
    }

    @Override
    public void delete(UUID id) {
        // 일단 파일에서 전체 사용자 목록 가져옴
        List<User> users = readFile();
        // 목록을 돌면서 지우고 싶은 ID를 가진 유저를 찾음
        User targetUser = null;
        for (User foundUser : users) {
            if (foundUser.getId().equals(id)) {
                targetUser = foundUser;
                break;
            }
        }
        // 대상을 찾으면 목록에서 삭제
        if (targetUser != null) {
            users.remove(targetUser);
        }
        // 한 명이 삭제된 최종 목록을 파일에 다시 덮어씌운다 (영속화)
        saveFile(users);
    }
}

/*
기본 요구사항
File IO를 통한 데이터 영속화
[ ]  JCF 대신 FileIO와 객체 직렬화를 활용해 메소드를 구현하세요.
서비스 구현체 분석
[ ] JCF*Service 구현체와 File*Service 구현체를 비교하여 공통점과 차이점을 발견해보세요.
[ ] "비즈니스 로직"과 관련된 코드를 식별해보세요.
[ ] "저장 로직"과 관련된 코드를 식별해보세요.
 */