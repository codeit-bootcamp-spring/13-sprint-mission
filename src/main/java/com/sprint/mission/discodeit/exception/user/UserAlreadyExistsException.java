package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.*;

import java.util.*;

public class UserAlreadyExistsException extends UserException {

    public UserAlreadyExistsException(String field) {
        super(
                ErrorCode.DUPLICATE_USER,
                Map.of("field", field)
        );
    }

}
