package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.StorageProperties;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
//파일(users.dat)에 사용자 데이터를 저장하는 파일 저장방식 구현체 (프로그램이 종료되어도 데이터가 유지됨)
public class FileUserRepository implements UserRepository {
    private final Path DIRECTORY; //User 데이터 저장 디렉토리
    private final String EXTENSION; //파일 확장자
    private final FileLockProvider fileLockProvider;

    public FileUserRepository(StorageProperties properties,
                              @Value(".discodeit") String fileDirectory,
                              FileLockProvider fileLockProvider) { //생성자. storageProperties에서 roorPath,extension 설정 주입
        this.EXTENSION = properties.getExtension();
        //저장 경로
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"),
                fileDirectory, properties.getRootPath(), User.class.getSimpleName());

        if (Files.notExists(DIRECTORY)) { //디렉토리가 없으면 생성
            try {
                Files.createDirectories(DIRECTORY);
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
        this.fileLockProvider = fileLockProvider;
    }

    //UUID->파일 경로 변환
    private Path resolvePath(UUID id){ return DIRECTORY.resolve(id + EXTENSION); }

    @Override //사용자 저장
    public User save(User user) {
        Path path = resolvePath(user.getId());
        ReentrantLock lock = fileLockProvider.getLock(path);
        lock.lock();
       try(
               FileOutputStream fos = new FileOutputStream(path.toFile()); //파일 출력 스트림
               ObjectOutputStream oos = new ObjectOutputStream(fos) //객체 직렬화 출력 스트림
               ) {
           oos.writeObject(user); //User 객체를 파일로 저장
       }catch (IOException e) {throw new RuntimeException(e);}
       finally {lock.unlock();}
        return user;
    }

    @Override //id로 사용자 조회
    public Optional<User> findById(UUID id) {
        User userNullable = null;
        Path path = resolvePath(id);
        ReentrantLock lock = fileLockProvider.getLock(path);
        lock.lock();
        if (Files.exists(path)) {
            try (
                    FileInputStream fis = new FileInputStream(path.toFile()); //파일 읽기 스트림
                    ObjectInputStream ois = new ObjectInputStream(fis) //객체 역질렬화 스트림
                    ) {
                userNullable = (User) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {throw new RuntimeException(e);}
            finally {lock.unlock();}
        }
        return Optional.ofNullable(userNullable);
    }

    @Override //username 으로 조회
    public Optional<User> findByUsername(String username) {
        return this.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return this.findAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override //전체 사용자 조회
    public List<User> findAll(){
        try (Stream<Path> paths = Files.list(DIRECTORY)) {
            return paths
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        ReentrantLock lock = fileLockProvider.getLock(path);
                        lock.lock();
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ){
                            return (User) ois.readObject();
                        }catch (IOException | ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                        }finally {lock.unlock();}
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //사용자 존재 여부 확인
    public boolean existsById(UUID id) {
        Path path = resolvePath(id);
        return Files.exists(path);
    }

    @Override //사용자 삭제
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //username 존재 여부 확인. 전체 탐색 O(n)
    public boolean existsByUsername(String username) {
        return this.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    @Override //email 존재 여부 확인. 여기 로직은 정상(UserRepository 인터페이스 기준)
    public boolean existsByEmail(String email) {
        return this.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }
}