-- Channel visibility (PUBLIC / PRIVATE)
ALTER TABLE channels ADD COLUMN IF NOT EXISTS visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';

-- Access code generated when a channel-access-request is approved
ALTER TABLE channel_access_requests ADD COLUMN IF NOT EXISTS access_code VARCHAR(8);
