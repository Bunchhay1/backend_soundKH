ALTER TABLE tracks ADD COLUMN IF NOT EXISTS play_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tracks ADD COLUMN IF NOT EXISTS genre VARCHAR(50);
ALTER TABLE tracks ADD COLUMN IF NOT EXISTS waveform TEXT;

-- Full-text search: tsvector column updated by trigger
ALTER TABLE tracks ADD COLUMN IF NOT EXISTS search_vector TSVECTOR;

CREATE INDEX IF NOT EXISTS idx_tracks_search ON tracks USING GIN(search_vector);
CREATE INDEX IF NOT EXISTS idx_tracks_play_count ON tracks(play_count DESC);
CREATE INDEX IF NOT EXISTS idx_tracks_genre ON tracks(genre);

-- Trigger to auto-update search_vector on insert/update
CREATE OR REPLACE FUNCTION tracks_search_vector_update() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := to_tsvector('english', coalesce(NEW.title, '') || ' ' || coalesce(NEW.genre, ''));
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tracks_search_vector_trigger ON tracks;
CREATE TRIGGER tracks_search_vector_trigger
  BEFORE INSERT OR UPDATE ON tracks
  FOR EACH ROW EXECUTE FUNCTION tracks_search_vector_update();

-- Backfill existing rows
UPDATE tracks SET search_vector = to_tsvector('english', coalesce(title, '') || ' ' || coalesce(genre, ''));
