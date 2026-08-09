package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.*;

import java.util.*;

public class UserNotFoundException extends UserException {
   public UserNotFoundException(UUID userId) {
       super(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
   }

   public UserNotFoundException(String email) {
       super(ErrorCode.USER_NOT_FOUND, Map.of("email", email));
   }
}
