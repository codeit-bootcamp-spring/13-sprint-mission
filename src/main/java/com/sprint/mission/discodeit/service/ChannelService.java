package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import java.util.List;
import java.util.UUID;

public abstract class ChannelService {
    public abstract void create(Channel channel); // (C) 만들기
    public abstract Channel read(UUID id); // (R) 한 명 조회
    public abstract List<Channel> readAll(); // (R) 모두 조회
    public abstract void update(Channel channel); // (U) 수정
    public abstract void delete(UUID id); // (D)삭제
}
