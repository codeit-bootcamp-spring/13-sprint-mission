CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS binary_contents (
                                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    file_name VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL
    );

CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    profile_id UUID,

    CONSTRAINT fk_users_profile
    FOREIGN KEY (profile_id)
    REFERENCES binary_contents (id)
    ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS user_statuses (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    user_id UUID NOT NULL UNIQUE,
    online BOOLEAN NOT NULL DEFAULT true,
    last_active_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_user_statuses_user
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS channels (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    name VARCHAR(100),
    description VARCHAR(500),
    type VARCHAR(10) NOT NULL,

    CONSTRAINT chk_channels_type
    CHECK (type IN ('PUBLIC', 'PRIVATE'))
    );

CREATE TABLE IF NOT EXISTS messages (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    content TEXT,
    channel_id UUID NOT NULL,
    author_id UUID,

    CONSTRAINT fk_messages_channel
    FOREIGN KEY (channel_id)
    REFERENCES channels (id)
    ON DELETE CASCADE,

    CONSTRAINT fk_messages_author
    FOREIGN KEY (author_id)
    REFERENCES users (id)
    ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS message_attachments (
                                                   message_id UUID NOT NULL,
                                                   attachment_id UUID NOT NULL,

                                                   PRIMARY KEY (message_id, attachment_id),

    CONSTRAINT fk_message_attachments_message
    FOREIGN KEY (message_id)
    REFERENCES messages (id)
    ON DELETE CASCADE,

    CONSTRAINT fk_message_attachments_attachment
    FOREIGN KEY (attachment_id)
    REFERENCES binary_contents (id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS read_statuses (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    user_id UUID NOT NULL,
    channel_id UUID NOT NULL,
    last_read_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_read_statuses_user
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE CASCADE,

    CONSTRAINT fk_read_statuses_channel
    FOREIGN KEY (channel_id)
    REFERENCES channels (id)
    ON DELETE CASCADE,

    CONSTRAINT uk_read_statuses_user_channel
    UNIQUE (user_id, channel_id)
    );

CREATE INDEX IF NOT EXISTS idx_messages_channel_created_at
    ON messages (channel_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_messages_author_id
    ON messages (author_id);

CREATE INDEX IF NOT EXISTS idx_read_statuses_user_id
    ON read_statuses (user_id);

CREATE INDEX IF NOT EXISTS idx_read_statuses_channel_id
    ON read_statuses (channel_id);

CREATE INDEX IF NOT EXISTS idx_message_attachments_attachment_id
    ON message_attachments (attachment_id);