CREATE TABLE IF NOT EXISTS binary_contents
(
    id           UUID         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    size         BIGINT       NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    CONSTRAINT pk_binary_contents PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users
(
    id         UUID         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(60)  NOT NULL,
    profile_id UUID,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_profile_id UNIQUE (profile_id),
    CONSTRAINT fk_users_profile_id
        FOREIGN KEY (profile_id)
            REFERENCES binary_contents (id)
            ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_statuses
(
    id             UUID        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ,
    user_id        UUID        NOT NULL,
    last_active_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_user_statuses PRIMARY KEY (id),
    CONSTRAINT uk_user_statuses_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_statuses_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS channels
(
    id          UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ,
    name        VARCHAR(100),
    description VARCHAR(500),
    type        VARCHAR(10) NOT NULL,
    CONSTRAINT pk_channels PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS messages
(
    id         UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    content    TEXT,
    channel_id UUID        NOT NULL,
    author_id  UUID,
    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT fk_messages_channel_id
        FOREIGN KEY (channel_id)
            REFERENCES channels (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_messages_author_id
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS message_attachments
(
    message_id    UUID NOT NULL,
    attachment_id UUID NOT NULL,
    CONSTRAINT pk_message_attachments
        PRIMARY KEY (message_id, attachment_id),
    CONSTRAINT fk_message_attachments_message_id
        FOREIGN KEY (message_id)
            REFERENCES messages (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_message_attachments_attachment_id
        FOREIGN KEY (attachment_id)
            REFERENCES binary_contents (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS read_statuses
(
    id           UUID        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ,
    user_id      UUID        NOT NULL,
    channel_id   UUID        NOT NULL,
    last_read_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_read_statuses PRIMARY KEY (id),
    CONSTRAINT uk_read_statuses_user_channel
        UNIQUE (user_id, channel_id),
    CONSTRAINT fk_read_statuses_user_id
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_read_statuses_channel_id
        FOREIGN KEY (channel_id)
            REFERENCES channels (id)
            ON DELETE CASCADE
);