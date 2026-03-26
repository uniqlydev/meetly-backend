CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    external_auth_id VARCHAR(120) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    bio VARCHAR(240),
    profile_image_url VARCHAR(500),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    host_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    location_text VARCHAR(180) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    max_slots INTEGER NOT NULL CHECK (max_slots >= 2 AND max_slots <= 20),
    event_type VARCHAR(30) NOT NULL CHECK (event_type IN ('PUBLIC_PLACE', 'PRIVATE_PLACE')),
    event_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (event_status IN ('ACTIVE', 'CANCELLED', 'COMPLETED')),
    approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE event_participants (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    participation_status VARCHAR(20) NOT NULL CHECK (
        participation_status IN ('PENDING', 'APPROVED', 'REJECTED', 'ATTENDED', 'CANCELLED')
    ),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_event_user UNIQUE (event_id, user_id)
);

CREATE TABLE safety_reports (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    reporter_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reported_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason VARCHAR(120) NOT NULL,
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_reporter_not_reported CHECK (reporter_user_id <> reported_user_id)
);

CREATE TABLE event_feedback (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    reviewer_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reviewed_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    felt_safe BOOLEAN NOT NULL,
    would_join_again BOOLEAN NOT NULL,
    rating SMALLINT CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_event_feedback UNIQUE (event_id, reviewer_user_id, reviewed_user_id),
    CONSTRAINT chk_feedback_reviewer_not_reviewed CHECK (reviewer_user_id <> reviewed_user_id)
);

CREATE INDEX idx_users_external_auth_id ON users (external_auth_id);
CREATE INDEX idx_users_email ON users (email);

CREATE INDEX idx_events_host_id ON events (host_id);
CREATE INDEX idx_events_scheduled_at ON events (scheduled_at);
CREATE INDEX idx_events_status_scheduled_at ON events (event_status, scheduled_at);

CREATE INDEX idx_event_participants_event_id ON event_participants (event_id);
CREATE INDEX idx_event_participants_user_id ON event_participants (user_id);
CREATE INDEX idx_event_participants_status ON event_participants (participation_status);
CREATE INDEX idx_event_participants_event_status ON event_participants (event_id, participation_status);

CREATE INDEX idx_safety_reports_event_id ON safety_reports (event_id);
CREATE INDEX idx_safety_reports_reported_user_id ON safety_reports (reported_user_id);

CREATE INDEX idx_event_feedback_event_id ON event_feedback (event_id);
CREATE INDEX idx_event_feedback_reviewed_user_id ON event_feedback (reviewed_user_id);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_events_updated_at
BEFORE UPDATE ON events
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_event_participants_updated_at
BEFORE UPDATE ON event_participants
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();