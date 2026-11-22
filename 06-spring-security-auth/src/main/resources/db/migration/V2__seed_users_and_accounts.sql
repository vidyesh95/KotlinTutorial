-- Flyway migration V2: Seed starter admin and customer accounts.
-- Uses DelegatingPasswordEncoder with Argon2id (matching Better-Auth standard).
-- Admin password: admin123
-- Alice password: password123

INSERT INTO user (id, name, email, email_verified, role, created_at, updated_at) VALUES
    ('usr_admin', 'Bookstore Admin', 'admin@bookstore.test', 1, 'ADMIN', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    ('usr_alice', 'Alice Customer', 'alice@bookstore.test', 1, 'USER', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z');

INSERT INTO account (id, user_id, account_id, provider_id, password, created_at, updated_at) VALUES
    ('acc_admin', 'usr_admin', 'admin@bookstore.test', 'credential', '{argon2}$argon2id$v=19$m=16384,t=2,p=1$xBzyho9/dM0Qf80WDUfKWw$l4sVqhjboAXPtQZyjqi55CPUZmFRsai9dB2hmkISDuk', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    ('acc_alice', 'usr_alice', 'alice@bookstore.test', 'credential', '{argon2}$argon2id$v=19$m=16384,t=2,p=1$DI+Ohn0u9IBNv9Ml4bGJVQ$TKIT970MRb8blo2FF8IkYq2hLmvdDxC04pdljhW8CLQ', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z');

-- Starter session for Alice so tests / curls can immediately use this bearer token
INSERT INTO session (id, user_id, token, expires_at, ip_address, user_agent, created_at, updated_at) VALUES
    ('ses_alice_seed', 'usr_alice', 'seed-alice-session-token-12345', '2099-01-01T00:00:00Z', '127.0.0.1', 'curl/8.0.0', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z');
