CREATE TABLE IF NOT EXISTS urltab (
    id              BIGSERIAL PRIMARY KEY ,
    short_key       VARCHAR(16),
    long_url        TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ NULL,

    -- Constraints
    CONSTRAINT uq_short_key UNIQUE (short_key),
    CONSTRAINT uq_long_url UNIQUE (long_url)
);