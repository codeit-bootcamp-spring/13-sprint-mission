-- 새 PostgreSQL DB를 초기화할 때 실행하는 전체 스키마입니다.
CREATE TABLE users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    password VARCHAR(60) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(30) UNIQUE,
    role VARCHAR(255)
);

CREATE TABLE channel (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    type VARCHAR(255) NOT NULL,
    name VARCHAR(20),
    description VARCHAR(20),
    allowed_user_list BYTEA
);

CREATE TABLE message (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    content VARCHAR(255),
    channel_id UUID NOT NULL,
    author_id UUID,
    CONSTRAINT fk_message_channel
        FOREIGN KEY (channel_id) REFERENCES channel (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_author
        FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE binary_content (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    file_name VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    user_id UUID UNIQUE,
    message_id UUID,
    CONSTRAINT fk_binary_content_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_binary_content_message
        FOREIGN KEY (message_id) REFERENCES message (id) ON DELETE CASCADE
);

CREATE TABLE read_status (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    user_id UUID NOT NULL,
    channel_id UUID NOT NULL,
    last_read_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    notification_enabled BOOLEAN NOT NULL,
    CONSTRAINT uk_read_status_user_channel UNIQUE (user_id, channel_id),
    CONSTRAINT fk_read_status_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_read_status_channel
        FOREIGN KEY (channel_id) REFERENCES channel (id) ON DELETE CASCADE
);

CREATE INDEX idx_message_channel_id ON message (channel_id);
CREATE INDEX idx_message_author_id ON message (author_id);
CREATE INDEX idx_binary_content_message_id ON binary_content (message_id);
CREATE INDEX idx_read_status_user_id ON read_status (user_id);

CREATE INDEX idx_read_status_channel_id ON read_status (channel_id);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL
);
CREATE INDEX idx_notifications_user_id ON notifications (user_id);
