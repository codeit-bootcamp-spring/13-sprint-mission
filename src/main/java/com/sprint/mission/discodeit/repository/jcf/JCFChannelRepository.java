package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
@Repository
//ChannelRepository 인터페이스의 JCF(java collection Framwork) 구현체
//실제 데이터베이스(DB)를 사용하지 않고 java메모리(map)에 Channel 객체를 저장
//프로그램이 실행되는 동안만 데이터가 유지되며 프로그램 종료 시 모든 데이터가 사라짐.
public class JCFChannelRepository implements ChannelRepository {
   private final Map<UUID, Channel> data; //실제 채널 데이터를 저장하는 Map(메모리 저장소)
   public JCFChannelRepository() {this.data = new HashMap<>();} //생성자. Repository 생성 시 HashMap 초기화

    @Override //채널 저장. 전달 받은 Channel 객체를 map에 저장함
    public Channel save(Channel channel) {
        this.data.put(channel.getId(), channel);
        return channel;
    }

    @Override //id로 채널 조회. map에서 UUID를  이용하면 Channel를 조회함
    public Optional<Channel> findById(UUID id) {return Optional.ofNullable(this.data.get(id));}

    @Override //채널 전체 조회. Repository 내부에 저장된 모든 Channel 객체를 조회함
    public List<Channel> findAll() { return this.data.values().stream().toList();}

    @Override //특정 채널 존재 여부 확인. 특정 UUID가 Repository에 존재하는지 확인함
    public boolean existsById(UUID id) {return this.data.containsKey(id);}

    @Override //채널 삭제. 전달받은 UUID에 해당하는 Channel을 제거함
    public void deleteById(UUID id) {this.data.remove(id);}
}
