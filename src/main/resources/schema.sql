BEGIN;

CREATE TABLE IF NOT EXISTS binary_contents (
                                               id           uuid PRIMARY KEY,
                                               created_at   timestamptz NOT NULL,
                                               file_name    varchar(255) NOT NULL,
    content_type varchar(100) NOT NULL,
    size         bigint NOT NULL,
    status       varchar(255),

    CONSTRAINT binary_contents_status_ck
    CHECK (status IN ('PROCESSING', 'SUCCESS', 'FAIL'))
    );


CREATE TABLE IF NOT EXISTS channels (
                                        id          uuid PRIMARY KEY,
                                        created_at  timestamptz NOT NULL,
                                        updated_at  timestamptz,
                                        name        varchar(100),
    description varchar(500),
    type        varchar(10) NOT NULL,

    CONSTRAINT channels_type_ck
    CHECK (type IN ('PUBLIC', 'PRIVATE'))
    );


CREATE TABLE IF NOT EXISTS users (
                                     id         uuid PRIMARY KEY,
                                     created_at timestamptz NOT NULL,
                                     updated_at timestamptz,
                                     username   varchar(255) NOT NULL,
    email      varchar(255) NOT NULL,
    password   varchar(255) NOT NULL,
    profile_id uuid,
    role       varchar(255),

    CONSTRAINT users_username_uq
    UNIQUE (username),

    CONSTRAINT users_email_uq
    UNIQUE (email),

    CONSTRAINT users_profile_uq
    UNIQUE (profile_id),

    CONSTRAINT users_profile_fk
    FOREIGN KEY (profile_id)
    REFERENCES binary_contents (id)
    ON DELETE SET NULL
    );


CREATE TABLE IF NOT EXISTS messages (
                                        id         uuid PRIMARY KEY,
                                        created_at timestamptz NOT NULL,
                                        updated_at timestamptz,
                                        content    varchar(255),
    channel_id uuid,
    author_id  uuid,

    CONSTRAINT messages_channel_fk
    FOREIGN KEY (channel_id)
    REFERENCES channels (id)
    ON DELETE CASCADE,

    CONSTRAINT messages_author_fk
    FOREIGN KEY (author_id)
    REFERENCES users (id)
    ON DELETE SET NULL
    );


CREATE TABLE IF NOT EXISTS read_statuses (
                                             id                  uuid PRIMARY KEY,
                                             created_at          timestamptz NOT NULL,
                                             updated_at          timestamptz,
                                             user_id             uuid NOT NULL,
                                             channel_id          uuid NOT NULL,
                                             last_read_at        timestamptz NOT NULL,
                                             notification_enable boolean NOT NULL,

                                             CONSTRAINT read_statuses_user_fk
                                             FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE NO ACTION,

    CONSTRAINT read_statuses_channel_fk
    FOREIGN KEY (channel_id)
    REFERENCES channels (id)
    ON DELETE NO ACTION
    );


CREATE TABLE IF NOT EXISTS notification (
                                            id          uuid PRIMARY KEY,
                                            created_at  timestamptz NOT NULL,
                                            receiver_id uuid,
                                            title       varchar(255),
    content     varchar(255)
    );


CREATE TABLE IF NOT EXISTS message_attachments (
                                                   message_id    uuid NOT NULL,
                                                   attachment_id uuid NOT NULL,

                                                   CONSTRAINT message_attachments_message_fk
                                                   FOREIGN KEY (message_id)
    REFERENCES messages (id)
    ON DELETE NO ACTION,

    CONSTRAINT message_attachments_attachment_fk
    FOREIGN KEY (attachment_id)
    REFERENCES binary_contents (id)
    ON DELETE NO ACTION
    );

COMMIT;