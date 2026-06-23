package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.IDRequest;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    void create(IDRequest cusi);
    UserStatus find(UUID id);
    List<UserStatus> findAll();
    void update(IDRequest uusi);
    void updateByUserID(UUID id);
    void delete(UUID id);
}
