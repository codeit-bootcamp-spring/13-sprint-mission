package com.sprint.mission.discodeit.entity;

// 이미지, 파일 등 바이너리 데이터를 표현하는 도메인 모델, 사용자의 프로필 이미지, 메시지에 첨부된 파일을 저장하기 위해 활용
// 수정 불가능한 도메인 모델로 간주합니다. 따라서 updatedAt 필드는 정의하지 않습니다.
// User, Message 도메인 모델과의 의존 관계 방향성을 잘 고려하여 id 참조 필드를 추가하세요.

import java.util.*;

public class BinaryContent extends BaseEntity {

    UUID id;

}
