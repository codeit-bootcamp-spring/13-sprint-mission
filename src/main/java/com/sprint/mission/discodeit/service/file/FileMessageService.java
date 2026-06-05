package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.UUID;

//파일 IO 기반 서비스처럼 이름만 붇었고,
//실제 동작은 JCFMessageService에 모두 "위임"하는 클래스
public class FileMessageService implements MessageService {
    private final MessageRepository repository;
    public FileMessageService() {
        this.repository = new FileMessageRepository();
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


    /*
    //private final JCFMessageService jcfMessageService; //실제 모든 작업을 담당하는 JCFMessageService
    //생성자에게 JCFMessageService를 받아 옴
    public FileMessageService(JCFMessageService jcfMessageService) {
        this.jcfMessageService = jcfMessageService;
    }

    //메시지 생성 시 JCFMessageService의 create 호출
    @Override
    public void create(Message message) {jcfMessageService.create(message);}

    //메시지 단일 조회 시 JCFMessageService의 read 호출
    @Override
    public Message read(UUID id) {return jcfMessageService.read(id);}

    //모든 메시지 조회 시 JCFMessageService의 readAll 호출
    @Override
    public List<Message> readAll() {return jcfMessageService.readAll();}

    //메시지 수정 시 JCFMessageService의 update 호출
    @Override
    public void update(Message message) {jcfMessageService.update(message);}

    //메시지 삭제 시 JCFMessageService의 delete 호출
    @Override
    public void delete(UUID id) {jcfMessageService.delete(id);}
*/

}
