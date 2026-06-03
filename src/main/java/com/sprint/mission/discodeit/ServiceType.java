package com.sprint.mission.discodeit;

public enum ServiceType {
    MAIN(
            """
                    ===== Main =====
                    select number for start service
                    1 - channel
                    2 - massage
                    3 - user
                    exit - quit
                    other word with out list will be accept invalid value."""
    ),
    USER(
            """
                    ===== User =====
                    select number for job
                    1 - create User
                    2 - get list of User
                    3 - get User by id
                    4 - update User
                    5 - remove User
                    exit - return to main"""
    ),
    CHANNEL(
            """
                    ===== Channel =====
                    select number for job
                    1 - create Channel
                    2 - get list of Channel
                    3 - get Channel by id
                    4 - update Channel
                    5 - remove Channel
                    exit - return to main"""
    ),
    MESSAGE(
            """
                    ===== Message =====
                    select number for job
                    1 - create Message
                    2 - get list of Message
                    3 - get Message by id
                    4 - update Message
                    5 - remove Message
                    exit - return to main"""
    )
    ;

    private final String initMsg;
    ServiceType(String initMsg) {
        this.initMsg = initMsg;
    }

    public String getMsg() {
        return initMsg;
    }
}
