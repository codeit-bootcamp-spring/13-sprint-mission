package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;


public class FileUserService implements UserService {
    private final Path DIRECTORY; //User 파일들이 저장될 디렉토리 경로
    private final String EXTENSION = ".ser"; //직렬화 파일 확장자

    public FileUserService() { //생성자. 서비스객체 생성 시 폴더가 존재하는지 확인.존재하지 않으면 자동생성
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), //저장 디렉토리 생성
                "file-data-map", User.class.getSimpleName()); //현재 프로젝트 실행 위치
        if (Files.notExists(DIRECTORY)) { //디렉토리가 존재하지 않으면 생성
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //UUID를 실제 파일 경로로 변환(예: UUID 1234-abcd -> file-date-map/User/1234-abcd.ser)
    private Path resolvePath(UUID id) { return DIRECTORY.resolve(id + EXTENSION); }

    @Override //사용자 생성
    public User create(String name, String email, String password) {
        User user = new User(name, email, password); //새로운 User 객체 생성
        Path path = resolvePath(user.getId()); //저장할 파일 경로 생성
        try (
            FileOutputStream fos = new FileOutputStream(path.toFile()); //파일 출력 스트림. 지정한 파일에 데이터를 쓸 수 있음
            ObjectOutputStream oos = new ObjectOutputStream(fos); //객체 출력 스트림. java 객체를 파일로 저장 가능
        ) {
            oos.writeObject(user); //User 객체를 파일에 저장
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return user;
    }

    @Override //사용자 단건조회
    public User find(UUID userId) {
        User userNullable = null; //조회 결과 저장 변수. 파일을 읽지 못하면 null유지
        Path path = resolvePath(userId); //uuid에 대응되는 파일 경로 생성
        if (Files.exists(path)) { //파일 존재 여주 확인
            try (
                    FileInputStream fis = new FileInputStream(path.toFile()); //파일 입력 스트림
                    ObjectInputStream ois = new ObjectInputStream(fis) //객체 입력 스트림. 파일 -> 객체 복원
            ) { //역직렬화. 파일에 저장된 user 객체 복원
                userNullable = (User) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } //userNullable == null 이면 예외 발생
        return Optional.ofNullable(userNullable)
                .orElseThrow(()->new NoSuchElementException("User with id " + userId + " not found"));
    }

    @Override //전체 사용자 조회
    public List<User> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION)) //.ser 파일만 선택
                    .map(path -> { // 파일->객체 변환
                        try(
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                                ){ //역질력화 수행
                            return (User) ois.readObject();
                        }catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList(); // stream->list 변환
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override //사용자 수정
    public User update(UUID userId, String newName, String newEmail, String newPassword) {
        User userNullable = null;
        Path path = resolvePath(userId); //수정 대상 파일 경로
        if (Files.exists(path)) { //파일 존재 시 읽기
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) { //파일->User 객체 복원
                userNullable = (User) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        User user = Optional.ofNullable(userNullable) //사용자 존재 여부 확인
                .orElseThrow(()->new NoSuchElementException("User with id " + userId + " not found"));
        user.update(newName,newEmail, newPassword);

        try ( // 수정된 객체를 파일에 다시 저장
                FileOutputStream fis = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fis)
                ){
                    oos.writeObject(user);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        return user;
    }

    @Override //사용자 삭제
    public void delete(UUID userId) {
        Path path = resolvePath(userId); //삭제 대상 파일 경로 생성
        if (Files.notExists(path)) { //파일이 존재하지 않으면 예외 발생
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        try {
            Files.delete(path); //실제 파일 삭제
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }


}