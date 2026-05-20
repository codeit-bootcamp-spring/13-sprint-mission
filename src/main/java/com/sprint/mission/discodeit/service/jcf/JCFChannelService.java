package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFChannelService implements ChannelService {

    private final Map<UUID, Channel> data;

    public JCFChannelService() {
        this.data = new HashMap<>();
    }


    @Override
    public void create(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("채널 정보가 없습니다.");
        }

        if (data.containsKey(channel.getId())) {
            throw new IllegalArgumentException("이미 존재하는 채널입니다.");
        }
        data.put(channel.getId(), channel);
    }


        @Override
        public Channel read (UUID id){
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("존재하지 않는 ID입니다.");
        }

        return data.get(id);
        }

        @Override
        public List<Channel> readAll () {
            return new ArrayList<>(data.values());
        }

        @Override
        public void update (UUID id, Channel channel){
           if (id == null) {
               throw new IllegalArgumentException("채널 ID는 필수입니다.");
           }

           if (channel == null) {
               throw new IllegalArgumentException("수정할 채널 정보가 없습니다.");
           }

           if (!data.containsKey(id)) {
               throw new IllegalArgumentException("존재하지 않은 ID입니다.");
           }

            data.put(id, channel);
        }

        @Override
        public void delete (UUID id){
        if (id == null) {
            throw new IllegalArgumentException("ID가 존재하지 않습니다.");
        }
        if (!data.containsKey(id)) {
            throw new IllegalArgumentException("삭제할 채널이 없습니다.");
        }
            data.remove(id);
        }
    }
