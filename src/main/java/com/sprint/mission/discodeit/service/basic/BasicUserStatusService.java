package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.JPAUserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {
    private final JPAUserStatusRepository userStatusService;

    @Override
    @Transactional
    public UserStatus updateByUserId(UUID userId, UserStatusUpdateRequest usur){
        UserStatus userStatus = userStatusService.findByUserId(userId).stream().findFirst().orElseThrow(
                () -> new DiscodeitException("no UserStatus by User id" + userId,"UserStatus",404)
        );
        userStatus.setLastActiveAt(usur.newLastActiveAt());
        return userStatus;
    }
}

