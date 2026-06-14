-- Channel follows: user follows a channel
CREATE TABLE IF NOT EXISTS channel_follows (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT    NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (channel_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_channel_follows_channel ON channel_follows(channel_id);
CREATE INDEX IF NOT EXISTS idx_channel_follows_user    ON channel_follows(user_id);

-- Channel managers: admin-assigned users who can manage a channel's tracks
CREATE TABLE IF NOT EXISTS channel_managers (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT    NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (channel_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_channel_managers_channel ON channel_managers(channel_id);

-- Channel stats view: plays, likes, followers per channel
CREATE OR REPLACE VIEW channel_stats AS
SELECT
    c.id                                        AS channel_id,
    COALESCE(SUM(t.play_count), 0)              AS total_plays,
    COUNT(DISTINCT l.id)                        AS total_likes,
    COUNT(DISTINCT f.id)                        AS follower_count
FROM channels c
LEFT JOIN tracks   t ON t.channel_id = c.id
LEFT JOIN likes    l ON l.track_id   = t.id
LEFT JOIN channel_follows f ON f.channel_id = c.id
GROUP BY c.id;
