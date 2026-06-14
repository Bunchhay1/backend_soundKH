-- Subscriptions (SuperStar tier)
CREATE TABLE IF NOT EXISTS subscriptions (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL UNIQUE REFERENCES users(id),
    plan       VARCHAR(20)  NOT NULL DEFAULT 'SUPER_STAR',
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Playlists
CREATE TABLE IF NOT EXISTS playlists (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Playlist <-> Track join table with ordering
CREATE TABLE IF NOT EXISTS playlist_tracks (
    playlist_id BIGINT  NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    track_id    BIGINT  NOT NULL REFERENCES tracks(id)    ON DELETE CASCADE,
    position    INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (playlist_id, track_id)
);

CREATE INDEX IF NOT EXISTS idx_playlists_user ON playlists(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_user ON subscriptions(user_id);
