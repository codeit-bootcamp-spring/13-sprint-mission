package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.UUID;

//파일 IO 기반 서비스처럼 보이지만,
//실제 동작은 JCFChannelService에 모두 "위임"하는 클래스
public class FileChannelService implements ChannelService {
    private final ChannelRepository repository;

    public FileChannelService() {
        this.repository = new FileChannelRepository();
    }
    @Override
    public void create(Channel channel) {
        repository.save(channel);
    }

    @Override
    public Channel read(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Channel> readAll() {
        return repository.findAll();
    }

    @Override
    public void update(Channel channel) {
        repository.save(channel);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }



    /* private final JCFChannelService jcfChannelService; //실제로 모든 작업을 담당하는 JCFChannelServic
    //생성자에서 JCFChannelService를 받아 옴
    public FileChannelService(JCFChannelService jcfChannelService) {
        this.jcfChannelService = jcfChannelService;
    }

    //채널 생성 시 JCFChannelService의 create를 호출해서 처리
    @Override
    public void create(Channel channel) {
        jcfChannelService.create(channel);
    }

    //채널 단일 조회 시 JCFChannelService의 read를 호출해서 처리
    @Override
    public Channel read(UUID id) {
        return jcfChannelService.read(id);
    }

    //모든 채널 조회 시 JCFChannelService의 readAll를 호출
    @Override
    public List<Channel> readAll() {return jcfChannelService.readAll();}

    //채널 수정 시 JCFChannelService의 update를 호출
    @Override
    public void update(Channel channel) {jcfChannelService.update(channel);}

    //채널 삭제 시 JCFChannelService의 delete를 호출
    @Override
    public void delete(UUID id) {
        jcfChannelService.delete(id);
    } */
}
