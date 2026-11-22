# Lesson 03 — The Better-Auth Schema

| | |
|---|---|
| **Goal** | Contrast Spring Security's 2005 default schema (`users`, `authorities`) with the modern Better-Auth schema (`user`, `session`, `account`, `verification`) and see why Better-Auth is superior for production backends. |
| **Needs** | Lesson 02, Module 03 (SQL) |
| **Time** | ~30 min |
| **Next** | [04 — Session Management & Tokens](04-session-management-and-tokens.md) |
| **Exercise** | None (design study) |

---

## §1. The legacy Spring Security default schema

If you open an old tutorial from 2010, you will see Spring Security's default JDBC schema:

```sql
create table users(
    username varchar_ignorecase(50) not null primary key,
    password varchar_ignorecase(500) not null,
    enabled boolean not null
);

create table authorities (
    username varchar_ignorecase(50) not null,
    authority varchar_ignorecase(50) not null,
    constraint fk_authorities_users foreign key(username) references users(username)
);
```

### Why this schema is obsolete in modern systems:

1. **Username as the Primary Key**:
   - In modern apps, users log in using **email**. What happens when a user wants to change their email address?
   - Because `username` is the primary key and foreign key across every related table (`orders`, `reviews`, `comments`), updating an email requires cascading updates across the entire database!
   - Modern backends always use an immutable, stable identifier (`id TEXT PRIMARY KEY`, like a UUID).
2. **Coupled Credentials**:
   - The `password` column is trapped on the `users` table. What if a user wants to log in with Google OAuth, GitHub, Magic Link, or Passkeys?
   - They don't have a password. You either store nulls, dummy hashes, or write hacky workarounds. You cannot easily link multiple login providers to one person.
3. **No Session Tracking**:
   - Sessions are not in this schema! Spring historically stored sessions in Tomcat memory or required Spring Session JDBC with obscure serialized Java binary blobs.
   - You cannot answer basic questions: *"Which devices is Alice logged into?"*, *"What IP did this session come from?"*, or *"Revoke the login from Chrome on Windows"*.
4. **No Verification Lifecycle**:
   - No table for email verification tokens, OTPs, or password reset tokens with expiration timestamps.

---

## §2. The Better-Auth 4-table schema

Modern platforms (Better-Auth, Lucia, Supabase, Auth0) decouple identity from credentials and sessions.

```
       ┌────────────────────────┐
       │          user          │  (Identity & Profile)
       │  id (PK)               │
       │  email (UNIQUE)        │
       │  name, role            │
       └────┬──────────────┬────┘
            │ 1            │ 1
            │              │
          * │            * │
       ┌────▼────┐    ┌────▼────┐
       │ account │    │ session │
       │ provider│    │ token   │  (Active devices,
       │ password│    │ expires │   revocable tokens)
       └─────────┘    └─────────┘

       ┌──────────────┐
       │ verification │  (Password resets, email OTPs)
       │ identifier   │
       │ token, expires
       └──────────────┘
```

In [`src/main/resources/db/migration/V1__better_auth_tables.sql`](../src/main/resources/db/migration/V1__better_auth_tables.sql):

### 1. `user` — Who you are
```sql
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
```

### 2. `account` — How you prove it
```sql
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
```
- For email/password: `provider_id = 'credential'`, `password = '$2a$10$...'`
- For Google OAuth: `provider_id = 'google'`, `account_id = google_sub_id`, `password = NULL`
- A single user can have multiple accounts linked to the same profile.

### 3. `session` — Where you are logged in
```sql
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
```
- Holds active login sessions.
- Deleting a row instantly revokes that session without restarting the server or waiting for a JWT to expire.

### 4. `verification` — Short-lived tokens
```sql
CREATE TABLE verification (
    id         TEXT PRIMARY KEY,
    identifier TEXT NOT NULL,  -- e.g. user's email
    value      TEXT NOT NULL,  -- hashed token or OTP
    expires_at TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
```

🐘 **Postgres note:**
In PostgreSQL, use `uuid PRIMARY KEY DEFAULT gen_random_uuid()` and `timestamptz`. In SQLite, `TEXT` holds ISO-8601 timestamps (`2026-01-01T00:00:00Z`) and UUID strings.
