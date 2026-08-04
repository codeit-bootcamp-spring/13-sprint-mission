DROP TABLE IF EXISTS users, channels, messages, user_statuses, read_statuses, binary_contents, message_attachments CASCADE;
DROP TYPE IF EXISTS type CASCADE;
BEGIN;


--- create Tables ---
CREATE TABLE users(
	id uuid CONSTRAINT id_pk PRIMARY KEY
	, created_at timestamptz NOT NULL
	, updated_at timestamptz
	, username varchar(50) NOT NULL CONSTRAINT username_uq UNIQUE
	, email varchar(100) NOT NULL CONSTRAINT email_uq UNIQUE
	, password varchar(60) NOT NULL
	, profile_id uuid UNIQUE  --- 외래키
);


--- enum for channels type
CREATE TYPE type AS ENUM(
	'public'
	, 'private'
);

CREATE TABLE channels(
	id uuid PRIMARY KEY
	, created_at timestamptz NOT NULL
	, updated_at timestamptz
	, name varchar(100)
	, description varchar(500)
	, type varchar(10) NOT NULL
);

CREATE TABLE messages(
	id uuid PRIMARY KEY
	, created_at timestamptz NOT NULL
	, updated_at timestamptz
	, content text
	, channel_id uuid NOT NULL
	, author_id uuid
);




CREATE TABLE read_statuses(
	id uuid PRIMARY KEY
	, created_at timestamptz NOT NULL
	, updated_at timestamptz
	, user_id uuid NOT NULL
	, channel_id uuid NOT NULL
	, last_read_at timestamptz NOT NULL

	, CONSTRAINT read_status_uq UNIQUE (user_id, channel_id)
);

CREATE TABLE user_statuses(
	id uuid PRIMARY KEY
	, created_at timestamptz NOT NULL
	, updated_at timestamptz
	, user_id uuid NOT NULL UNIQUE
	, last_active_at timestamptz NOT NULL
);

CREATE TABLE binary_contents(
	id uuid PRIMARY KEY
	, created_at timestamptz NOT NULL
	, file_name varchar(255) NOT NULL
	, size bigint
	, content_type varchar(100) NOT NULL
	, bytes bytea NOT NULL
);


CREATE TABLE message_attachments(
	message_id uuid NOT NULL
	, attachment_id uuid NOT NULL
);



ALTER TABLE users
	ADD CONSTRAINT users_fk
		FOREIGN KEY (profile_id)
		REFERENCES binary_contents(id) ON DELETE SET NULL;

ALTER TABLE messages
	ADD CONSTRAINT msg_c_fk
		FOREIGN KEY (channel_id)
		REFERENCES channels(id) ON DELETE CASCADE,
	ADD CONSTRAINT msg_a_fk
		FOREIGN KEY (author_id)
		REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE read_statuses
	ADD CONSTRAINT read_status_user_fk
		FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
	ADD CONSTRAINT read_status_channel_fk
		FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE;


ALTER TABLE user_statuses
	ADD CONSTRAINT ust_fk
		FOREIGN KEY (user_id)
		REFERENCES users(id) ON DELETE CASCADE;


ALTER TABLE message_attachments
	ADD CONSTRAINT mattr_fk
		FOREIGN KEY (attachment_id)
		REFERENCES binary_contents(id) ON DELETE CASCADE,
	ADD CONSTRAINT mmsg_fk
		FOREIGN KEY (message_id)
		REFERENCES messages(id) ON DELETE CASCADE;

COMMIT;



ROLLBACK;
