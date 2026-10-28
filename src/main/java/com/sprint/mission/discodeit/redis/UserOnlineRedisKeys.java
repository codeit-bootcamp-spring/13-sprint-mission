package com.sprint.mission.discodeit.redis;

/**
 * JWT Lua 실행과 온라인 추적 저장소가 함께 사용하는 Redis 키를 정의한다.
 * 키 문자열을 바꾸면 기존 Redis 데이터와의 호환성이 달라지므로 패키지 이동과 구분한다.
 */
public final class UserOnlineRedisKeys {

    public static final String TRACKED_USERS_KEY =
            "discodeit:online-status:tracked-users";

    private UserOnlineRedisKeys() {
    }
}
