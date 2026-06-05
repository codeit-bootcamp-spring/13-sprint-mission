package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.*;

//ChannelService를 실제로 동작시키는 JCF(컬렉션) 기반 구현체
public class JCFChannelService implements ChannelService {

    private final ChannelRepository repository;

    public JCFChannelService() {
        this.repository = new JCFChannelRepository();
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

    /* //UUID(키)-Channel(값) 쌍을 저장하는 Map. 반드시 final로 선언
    private final Map <UUID, Channel> data;

    //생성자에서 date(Map) 객체를 초기화
    public JCFChannelService(){
        this.data = new HashMap<>();
    }

    //채널 추가
    @Override
    public void create(Channel channel){
        data.put(channel.getId(),channel);
    }

    //채널 한 개(id로) 조회
    @Override
    public Channel read(UUID id){
        return data.get(id);
    }

    //전체 채널 목록 반환
    @Override
    public List<Channel>readAll(){
        return new ArrayList <> (data.values());
    }

    //채널 정보 수정 (id로 덮어쓰기)
    @Override
    public void update(Channel channel){
        data.put(channel.getId(), channel);
    }

    //채널 삭제 (id로)
    @Override
    public void delete(UUID id){
        data.remove(id);
    } */
}