package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.userstatus.UserStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.userstatus.UserStatusUpdateCommand;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

    UserStatusDto create(UserStatusCreateCommand command);
    UserStatusDto find(UUID id);
    List<UserStatusDto> findAll();
    UserStatusDto update(UUID id, UserStatusUpdateCommand command);
    UserStatusDto updateByUserId(UUID userId, UserStatusUpdateCommand command);
    void delete(UUID id);
}
