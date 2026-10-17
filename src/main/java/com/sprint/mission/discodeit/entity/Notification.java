package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    public Notification(User receiver, String title, String content) {
        this.receiver = Objects.requireNonNull(
                receiver,
                "알림 수신자는 필수입니다."
        );

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "알림 제목은 필수입니다."
            );
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "알림 내용은 필수입니다."
            );
        }

        this.title = title;
        this.content = content;
    }


}
