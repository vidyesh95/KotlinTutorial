-- Flyway migration V1: Better-Auth core schema (user, session, account, verification)
-- Decouples user identity from auth credentials and tracks revocable database sessions.

CREATE TABLE user (
    id             TEXT    PRIMARY KEY,
    name           TEXT    NOT NULL,
    email          TEXT    NOT NULL UNIQUE,
    email_verified INTEGER NOT NULL DEFAULT 0,
    image          TEXT,
    role           TEXT    NOT NULL DEFAULT 'USER',
    created_at     TEXT    NOT NULL,
    updated_at     TEXT    NOT NULL
);

CREATE INDEX idx_user_email ON user (email);

CREATE TABLE session (
    id         TEXT PRIMARY KEY,
    user_id    TEXT NOT NULL REFERENCES user (id) ON DELETE CASCADE,
    token      TEXT NOT NULL UNIQUE,
    expires_at TEXT NOT NULL,
    ip_address TEXT,
    user_agent TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX idx_session_token ON session (token);
CREATE INDEX idx_session_user_id ON session (user_id);

CREATE TABLE account (
    id          TEXT PRIMARY KEY,
    user_id     TEXT NOT NULL REFERENCES user (id) ON DELETE CASCADE,
    account_id  TEXT NOT NULL,
    provider_id TEXT NOT NULL,
    password    TEXT,
    created_at  TEXT NOT NULL,
    updated_at  TEXT NOT NULL,
    UNIQUE (provider_id, account_id)
);

CREATE INDEX idx_account_user_id ON account (user_id);

CREATE TABLE verification (
    id         TEXT PRIMARY KEY,
    identifier TEXT NOT NULL,
    value      TEXT NOT NULL,
    expires_at TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX idx_verification_identifier ON verification (identifier);
