-- binary_contents
CREATE TABLE IF NOT EXISTS binary_contents
(
    id           uuid PRIMARY KEY,
    created_at   timestamptz  NOT NULL,
    file_name    varchar(255) NOT NULL,
    size         bigint       NOT NULL,
    content_type varchar(100) NOT NULL,
    bytes        bytea        NOT NULL
);

-- users
CREATE TABLE IF NOT EXISTS users
(
    id         uuid PRIMARY KEY,
    created_at timestamptz NOT NULL,
    updated_at timestamptz,
    username   varchar(50)  NOT NULL,
    email      varchar(100) NOT NULL,
    password   varchar(60)  NOT NULL,
    profile_id uuid,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_profile_id UNIQUE (profile_id),
    CONSTRAINT fk_users_profile_id FOREIGN KEY (profile_id)
        REFERENCES binary_contents (id) ON DELETE SET NULL
);

-- channels
CREATE TABLE IF NOT EXISTS channels
(
    id          uuid PRIMARY KEY,
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz,
    name        varchar(100),
    description varchar(500),
    type        varchar(10) NOT NULL,
    CONSTRAINT ck_channels_type CHECK (type IN ('PUBLIC', 'PRIVATE'))
);

-- user_statuses
CREATE TABLE IF NOT EXISTS user_statuses
(
    id             uuid PRIMARY KEY,
    created_at     timestamptz NOT NULL,
    updated_at     timestamptz,
    user_id        uuid        NOT NULL,
    last_active_at timestamptz NOT NULL,
    CONSTRAINT uk_user_statuses_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_statuses_user_id FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

-- read_statuses
CREATE TABLE IF NOT EXISTS read_statuses
(
    id            uuid PRIMARY KEY,
    created_at    timestamptz NOT NULL,
    updated_at    timestamptz,
    user_id       uuid        NOT NULL,
    channel_id    uuid        NOT NULL,
    last_read_at  timestamptz NOT NULL,
    CONSTRAINT uk_read_statuses_user_id_channel_id UNIQUE (user_id, channel_id),
    CONSTRAINT fk_read_statuses_user_id FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_read_statuses_channel_id FOREIGN KEY (channel_id)
        REFERENCES channels (id) ON DELETE CASCADE
);

-- messages
CREATE TABLE IF NOT EXISTS messages
(
    id         uuid PRIMARY KEY,
    created_at timestamptz NOT NULL,
    updated_at timestamptz,
    content    text,
    channel_id uuid NOT NULL,
    author_id  uuid,
    CONSTRAINT fk_messages_channel_id FOREIGN KEY (channel_id)
        REFERENCES channels (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_author_id FOREIGN KEY (author_id)
        REFERENCES users (id) ON DELETE SET NULL
);

-- message_attachments
CREATE TABLE IF NOT EXISTS message_attachments
(
    message_id    uuid NOT NULL,
    attachment_id uuid NOT NULL,
    PRIMARY KEY (message_id, attachment_id),
    CONSTRAINT fk_message_attachments_message_id FOREIGN KEY (message_id)
        REFERENCES messages (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_attachments_attachment_id FOREIGN KEY (attachment_id)
        REFERENCES binary_contents (id) ON DELETE CASCADE
);
