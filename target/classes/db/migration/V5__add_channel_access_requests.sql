-- Channel-level private access requests
CREATE TABLE IF NOT EXISTS channel_access_requests (
    id           BIGSERIAL PRIMARY KEY,
    channel_id   BIGINT      NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    user_id      BIGINT      NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (channel_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_car_channel ON channel_access_requests(channel_id);
CREATE INDEX IF NOT EXISTS idx_car_user    ON channel_access_requests(user_id);
