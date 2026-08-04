CREATE TABLE "read_statuses" (
	"id"	uuid		NOT NULL,
	"created_at"	timestamptz		NOT NULL,
	"updated_at"	timestamptz		NULL,
	"user_id"	uuid		NOT NULL,
	"channel_id"	uuid		NOT NULL,
	"last_read_at"	timestamptz		NOT NULL
);

CREATE TABLE "users" (
	"id"	uuid 	NOT NULL,
	"created_at"	timestamptz		NOT NULL,
	"updated_at"	timestamptz		NULL,
	"username"	varchar(50)		NOT NULL,
	"email"	varchar(100)		NOT NULL,
	"password"	varchar(60)		NOT NULL,
	"profile_id"	uuid		NULL
);

CREATE TABLE "user_statuses" (
	"id"	uuid		NOT NULL,
	"created_at"	timestamptz		NOT NULL,
	"updated_at"	timestamptz		NULL,
	"user_id"	uuid		NOT NULL,
	"last_active_at"	timestamptz		NOT NULL
);

CREATE TABLE "messages" (
	"id"	uuid		NOT NULL,
	"created_at"	timestamptz		NOT NULL,
	"updated_at"	timestamptz		NULL,
	"content"	text		NULL,
	"channel_id"	uuid		NOT NULL,
	"author_id"	uuid		NULL
);

CREATE TABLE "message_attachments" (
	"message_id"	uuid		NOT NULL,
	"attachment_id"	uuid		NOT NULL
);

CREATE TABLE "binary_contents" (
	"id"	uuid		NOT NULL,
	"created_at"	timestamptz		NOT NULL,
	"file_name"	varchar(255)		NOT NULL,
	"size"	bigint		NOT NULL,
	"content_type"	varchar(100)	NOT NULL
);

CREATE TABLE "channels" (
	"id"	uuid		NOT NULL,
	"created_at"	timestamptz		NOT NULL,
	"updated_at"	timestamptz		NULL,
	"name"	varchar(100)		NULL,
	"description"	varchar(500)		NULL,
	"type"	varchar(10)		NOT NULL
);

ALTER TABLE "read_statuses"
ADD CONSTRAINT "PK_READ_STATUSES"
PRIMARY KEY ("id");

ALTER TABLE "users"
ADD CONSTRAINT "PK_USERS"
PRIMARY KEY ("id");

ALTER TABLE "user_statuses"
ADD CONSTRAINT "PK_USER_STATUSES"
PRIMARY KEY ("id");

ALTER TABLE "messages"
ADD CONSTRAINT "PK_MESSAGES"
PRIMARY KEY ("id");

ALTER TABLE "message_attachments"
ADD CONSTRAINT "PK_MESSAGE_ATTACHMENTS"
PRIMARY KEY ("message_id", "attachment_id");

ALTER TABLE "binary_contents"
ADD CONSTRAINT "PK_BINARY_CONTENTS"
PRIMARY KEY ("id");

ALTER TABLE "channels"
ADD CONSTRAINT "PK_CHANNELS"
PRIMARY KEY ("id");

ALTER TABLE "read_statuses"
ADD CONSTRAINT "FK_users_TO_read_statuses_1"
FOREIGN KEY ("user_id")
REFERENCES "users" ("id")
ON DELETE CASCADE;

ALTER TABLE "read_statuses"
ADD CONSTRAINT "FK_channels_TO_read_statuses_1"
FOREIGN KEY ("channel_id")
REFERENCES "channels" ("id")
ON DELETE CASCADE;

ALTER TABLE "users"
ADD CONSTRAINT "FK_binary_contents_TO_users_1"
FOREIGN KEY ("profile_id")
REFERENCES "binary_contents" ("id")
ON DELETE SET NULL;


ALTER TABLE "user_statuses"
ADD CONSTRAINT "FK_users_TO_user_statuses_1"
FOREIGN KEY ("user_id")
REFERENCES "users" ("id")
ON DELETE CASCADE;

ALTER TABLE "messages"
ADD CONSTRAINT "FK_channels_TO_messages_1"
FOREIGN KEY ("channel_id")
REFERENCES "channels" ("id")
ON DELETE CASCADE;

ALTER TABLE "messages"
ADD CONSTRAINT "FK_users_TO_messages_1"
FOREIGN KEY ("author_id")
REFERENCES "users" ("id")
ON DELETE SET NULL;

ALTER TABLE "message_attachments"
ADD CONSTRAINT "FK_messages_TO_message_attachments_1"
FOREIGN KEY ("message_id")
REFERENCES "messages" ("id")
ON DELETE CASCADE;

ALTER TABLE "message_attachments"
ADD CONSTRAINT "FK_binary_contents_TO_message_attachments_1"
FOREIGN KEY ("attachment_id")
REFERENCES "binary_contents" ("id")
ON DELETE CASCADE;

ALTER TABLE "users"
ADD CONSTRAINT "UK_USERS_USERNAME"
UNIQUE ("username");

ALTER TABLE "users"
ADD CONSTRAINT "UK_USERS_EMAIL"
UNIQUE ("email");

ALTER TABLE "users"
ADD CONSTRAINT "UK_USERS_PROFILE_ID"
UNIQUE ("profile_id");

ALTER TABLE "user_statuses"
ADD CONSTRAINT "UK_USER_STATUSES_USER_ID"
UNIQUE ("user_id");

ALTER TABLE "read_statuses"
ADD CONSTRAINT "UK_READ_STATUSES_USER_CHANNEL"
UNIQUE ("user_id", "channel_id");

ALTER TABLE "channels"
ADD CONSTRAINT "CK_CHANNELS_TYPE"
CHECK ("type" IN ('PUBLIC', 'PRIVATE'));