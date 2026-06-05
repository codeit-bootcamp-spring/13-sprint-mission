package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;

import java.util.List;
import java.util.UUID;

//FileUserService: 이름은 파일 기반처럼 보이지만,
//실체 CURD 기능은 JCFUserService에 "위임"하는 서비스 클래스
public class FileUserService implements UserService {
    //private final JCFUserService jcfUserService; //JCFUserService 인스턴스를 필드로 가짐(실제 동작은 얘가 처리)
    private final UserRepository repository;

    public FileUserService() {
        this.repository = new JCFUserRepository();
    }
    @Override
    public void create(User user) {
        repository.save(user);
    }
    @Override
    public User read(UUID id) {
        return repository.findById(id);
    }
    @Override
    public List<User> readAll() {
        return repository.findAll();
    }
    @Override
    public void update(User user) {
        repository.save(user);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }


    //JCFUserService를 생성자에서 주고받음
    /*public FileUserService(JCFUserService jcfUserService) {
        this.jcfUserService = jcfUserService;
    }

    //유저 생성: JCFUserService에게 요청만 던짐
    @Override
    public void create(User user) {
        jcfUserService.create(user);
    }

    //유저 단일 조회: JCFUserService의 기능 활용
    @Override
    public User read(UUID id) {
        return jcfUserService.read(id);
    }

    //전체 유저 리스트 조회: JCFUserService의 기능 활용
    @Override
    public List<User> readAll() {
        return jcfUserService.readAll();
    }

    //유저 정보 수정: JCFUserService의 기능 활용
    @Override
    public void update(User user) {
        jcfUserService.update(user);
    }

    //유저 삭제: JCFUserService의 기능 활용
    @Override
    public void delete(UUID id) {
        jcfUserService.delete(id);
    }*/

}