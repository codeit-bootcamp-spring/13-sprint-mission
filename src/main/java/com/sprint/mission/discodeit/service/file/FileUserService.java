package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUserService implements UserService {

    private final Path filePath =  Paths.get("users.ser");//경로 생성
                              // Paths.get를 호출하면 파일 경로를 담은 Path 객체를 생성하여 Path filePath에 할당한다.
    private List<User> loadFromFile() {//리스트 가져오
        if (!Files.exists(filePath)) {//!Files.exists는 파일 존재 확인 있으면 true 없으면 false
            return new ArrayList<>();  //filePath 경로에 파일이 없으면 리스트 반환
                                        //비정상 종료 방지를 위해.
                                        //빈리스트를 새로 생성해서 반환
        }try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toString()))) {
        //try는 ()안에 메서드 또는 생성자가 외부 자원과         //FileInputStream객체는 파일에서 바이트를 읽어오는 객체이다.
        //연결 되어 작업을 실행 해야 할떄 통로를 열어주고        //ObjectInputStream객체는 버아트를 java객채로 변환 하는 객체이다.
        //작업이 끝나면 연결 통로를 닫아 메모리 낭비를           //new FileInputStream 파일을 읽기 위한 객체를 생성
        //효율 적으로 방지하는 키워드이다.                   //filePath.toString은 filePath안에 Path타입 파일 경로를
        //여기서는 객체 생성시 파일 경로를 따라 파일에          //문자열로 형 변환한다. 이를 FileInputStream객체에 할당.
        //파일에 접근을 해야 하는데 객체 생성후 통로를 닫아준다. // 이 객체를 ois 변수에 할당한다.
            return (List<User>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){ // | 파이프(pipe)라고 하며 멍티캐치 구분자로서 OR의 의미를 갖는다.
                                                        // e는 오류 발생시 java가 자동으로 생성한 해당 오류 객체를 담는 매개변수
            throw new RuntimeException("파일 로드 실패",e);// 객체를 생성해서 오류메세지와 e를 할당. main에서 try-catch로 출력함.
        }
    }

    private void saveToFile(List<User> users) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(users);// 객체를 바이트로 변환 후 파일에 저장
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패",e);
        }
    }



    @Override
    public void create(User user) {
        List<User> users = loadFromFile();//불러오기
        users.add(user);//추가하기
        saveToFile(users);//저장하기
    }

    @Override
    public User read(UUID id) {
        List<User> users = loadFromFile();
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }


    @Override
    public List<User> readAll() {
        return loadFromFile();
    }

    @Override
    public void update(UUID id, String name, String email, String password) {
        List<User> users = loadFromFile();
        User foundUser = read(id);
        if (foundUser != null) {
            foundUser.update(name, email, password);
            saveToFile(users);
        }


    }

    @Override
    public void delete(UUID id) {
        List<User> users = loadFromFile();
        users.remove(read(id));
        saveToFile(users);

    }
}
