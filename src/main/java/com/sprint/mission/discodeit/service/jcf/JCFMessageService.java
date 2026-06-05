package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.UUID;

//MessageService를 실제로 동작시키는 JCF(컬렉션) 기반 구현체
public class JCFMessageService implements MessageService {

    private final MessageRepository repository;

    public JCFMessageService() {
        this.repository = new JCFMessageRepository();
    }

    @Override
    public void create(Message message) {
        repository.save(message);
    }
    @Override
    public Message read(UUID id) {
        return repository.findById(id);
    }
    @Override
    public List<Message> readAll() {
        return repository.findAll();
    }

    @Override
    public void update(Message message) {
        repository.save(message);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }

   /* //UUID(키)-User(값) 쌍을 저장하는 Map. 반드시 final로 선언
    private final Map<UUID, Message> data;

    //생성자에서 date(Map) 객체를 조회
    public JCFMessageService(){
        this.data = new HashMap<>();
    }

    //메시지 추가
    @Override
    public void create(Message message){
        data.put(message.getId(), message);
    }

    //메시지 한 개 (id로) 조회
    @Override
    public Message read(UUID id){
        return data.get(id);
    }

    //전체 메시지 목록 반환
    @Override
    public List<Message> readAll() {
        return new ArrayList<>(data.values());
    }

    //메시지 정보 수정 (id로 덮어쓰기)
    @Override
    public void update(Message message){
        data.put(message.getId(), message);
    }

    //메시지 삭제(id로)
    @Override
    public void delete(UUID id){
        data.remove(id);
    } */
}
