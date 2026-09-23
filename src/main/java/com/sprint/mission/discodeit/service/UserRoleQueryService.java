package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Role;

public interface UserRoleQueryService {
    boolean existsByRole(Role role);
}
