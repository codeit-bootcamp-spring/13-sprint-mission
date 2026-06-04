package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class BaseEntity implements Serializable {
    public static final long serialVersionUID = 1L;


    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;

    // 수정 시간 관리 (행동 수행 목적의 일반 메서드, 직접 호출하여 실행)
    protected void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    // 생성자
    public BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    // getter
        public UUID getId () {
            return id;
        }
        public Long getCreatedAt () {
            return createdAt;
        }
        public Long getUpdatedAt () {
            return updatedAt;
        }

        protected String formatDate(Long time) {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            return Instant.ofEpochMilli(time)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(formatter);
        }

    }