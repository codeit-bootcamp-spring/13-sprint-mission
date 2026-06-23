package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.input.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {
    private final UserStatusRepository usr;

    @Override
    public UserStatus updateByUserId(UUID userId, UserStatusUpdateRequest usur){
        UserStatus ust = usr.findByUserID(userId).orElseThrow(
                () -> new DiscodeitException("no UserStatus by User id" + userId,"UserStatus",404)
        );
        ust.setLastActiveAt(usur.newLastActiveAt());
        return ust;
    }
}

