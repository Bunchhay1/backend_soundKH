CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'LISTENER',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS channels (
    id          BIGSERIAL PRIMARY KEY,
    creator_id  BIGINT       NOT NULL REFERENCES users(id),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    is_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tracks (
    id            BIGSERIAL PRIMARY KEY,
    channel_id    BIGINT       NOT NULL REFERENCES channels(id),
    title         VARCHAR(255) NOT NULL,
    duration      INTEGER,
    s3_object_key VARCHAR(500) NOT NULL,
    visibility    VARCHAR(20)  NOT NULL DEFAULT 'PRIVATE',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS access_requests (
    id           BIGSERIAL PRIMARY KEY,
    track_id     BIGINT    NOT NULL REFERENCES tracks(id),
    user_id      BIGINT    NOT NULL REFERENCES users(id),
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP   NOT NULL DEFAULT NOW()
);
