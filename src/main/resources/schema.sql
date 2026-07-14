CREATE TABLE binary_contents
(
    id           UUID PRIMARY KEY,    -- PRIMARY KEY는 NOT NULL,UNIQE포함
    created_at   TIMESTAMPTZ  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    size         BIGINT       NOT NULL,
    content_type VARCHAR(100) NOT NULL--여기까지가 테이블의 구조
);

CREATE TABLE users
(

    id         UUID PRIMARY KEY,                  -- id컬럼을 기본키로 지정.
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    PASSWORD   VARCHAR(60)  NOT NULL,
    profile_id UUID UNIQUE,

    CONSTRAINT fk_profile FOREIGN KEY (profile_id)--제냑조건 이름 fk_profile/
        --users(이 테이)의 profile_id컬럼을 외레키로 지정.
        REFERENCES binary_contents (id) ON DELETE SET NULL
    --REFERENCES binary_contents(id) : binary_contents테이블의 id컬럼을 가리킴
    --binary_contents에서 해당 row(행/iprofile_id가 참조하는)가 삭제되면
    ---profile_id를 NULL로 바꾼다.

);

CREATE TABLE user_statuses
(
    id             UUID PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ,
    user_id        UUID        NOT NULL UNIQUE,
    last_active_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_user_statuses FOREIGN KEY (user_id)----제약 조건 이름:fk_user_statuses
        --user_statuses(이 테이블)의 FK = user_id
        REFERENCES users (id) ON DELETE CASCADE
    --user테이블의 id 컬럼을 참조/참조하는 행(user에서 user_statuses가 참조하는 id컬럼을 갖는)
    --삭제 되면 같이 삭제.(null이 아니라 행자체를 삭제함. )
);


-- channels
CREATE TABLE channels
(
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ,
    name        varchar(100),
    description varchar(500),
    type        varchar(10) NOT NULL CHECK (type IN ('PUBLIC', 'PRIVATE'))
);

-- read_statuses
CREATE TABLE read_statuses
(
    id           UUID PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ,
    user_id      UUID        NOT NULL,
    channel_id   UUID        NOT NULL,
    last_read_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_read_statuses_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_read_statuses_channels FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    CONSTRAINT uk_read_statuses UNIQUE (user_id, channel_id)
);
-- messages
CREATE TABLE messages
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    content    TEXT,
    channel_id UUID        NOT NULL,
    author_id  UUID,
    CONSTRAINT fk_messages_channels FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_users FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL
);
-- message_attachments
CREATE TABLE message_attachments
(
    message_id    UUID NOT NULL,
    attachment_id UUID NOT NULL,
    CONSTRAINT fk_message_attachments_messages FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_attachments_binary_contents FOREIGN KEY (attachment_id) REFERENCES binary_contents (id) ON DELETE CASCADE,
    PRIMARY KEY (message_id, attachment_id)
);