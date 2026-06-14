CREATE INDEX IF NOT EXISTS idx_tracks_channel_id  ON tracks(channel_id);
CREATE INDEX IF NOT EXISTS idx_tracks_visibility   ON tracks(visibility);
CREATE INDEX IF NOT EXISTS idx_access_requests_status ON access_requests(status);
CREATE INDEX IF NOT EXISTS idx_access_requests_user   ON access_requests(user_id);

ALTER TABLE access_requests
    ADD CONSTRAINT uq_access_request UNIQUE (track_id, user_id);
