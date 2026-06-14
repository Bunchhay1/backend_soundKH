-- Fix orphaned tracks when channel is deleted
ALTER TABLE tracks DROP CONSTRAINT IF EXISTS tracks_channel_id_fkey;
ALTER TABLE tracks ADD CONSTRAINT tracks_channel_id_fkey
    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE;

-- Fix orphaned access_requests when track is deleted
ALTER TABLE access_requests DROP CONSTRAINT IF EXISTS access_requests_track_id_fkey;
ALTER TABLE access_requests ADD CONSTRAINT access_requests_track_id_fkey
    FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE;
