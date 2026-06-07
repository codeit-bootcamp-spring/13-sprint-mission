package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JCFChannelService implements ChannelService {

    private final List<Channel> data;

    public JCFChannelService(){
        this.data = new ArrayList<>();
    }

    // 매개변수 변경으로 인한 오류로 빈 메서드 생성
    @Override
    public Channel create(String name, Channel.ChannelType channelType, String description) {
        return new Channel(name, channelType, description);
    }

    // 생성
//    @Override
//    public void create(Channel ch) {
//        data.add(ch);
//    }

    // 조회
    @Override
    public Channel find(UUID id) {
        for (Channel ch : data) {
            if (ch.getId().equals(id)) {
                return ch;
            }
        }
        throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
    }

    // 모두 조회
    @Override
    public List<Channel> findAll() {
        if (!data.isEmpty()) {
            return data;
        }
        return Collections.emptyList();
    }

    // 수정
    @Override
    public void update(UUID id, String name, Channel.ChannelType type, String description) {
        for (Channel ch : data) {
            if (ch.getId().equals(id)) {
                ch.update(name, type, description);
                return;
            }
        }
        throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
    }

    // 삭제
    @Override
    public void delete(UUID id) {
        for (Channel channel : data) {
            if (channel.getId().equals(id)) {
                data.remove(channel);
                return;
            }
        }
        throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
    }

}
