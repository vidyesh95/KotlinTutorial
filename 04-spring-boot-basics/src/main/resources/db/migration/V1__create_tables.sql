-- Flyway migration V1 (lesson 05). Never edit after it has run anywhere — add V3, V4… instead.
CREATE TABLE authors (
    id         INTEGER PRIMARY KEY,
    name       TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE books (
    id          INTEGER PRIMARY KEY,
    title       TEXT    NOT NULL,
    author_id   INTEGER NOT NULL REFERENCES authors (id),
    isbn        TEXT    NOT NULL UNIQUE,
    price_cents INTEGER NOT NULL CHECK (price_cents >= 0),
    stock       INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    created_at  TEXT    NOT NULL,
    updated_at  TEXT    NOT NULL
);

CREATE INDEX books_author_id_idx ON books (author_id);

CREATE TABLE orders (
    id             INTEGER PRIMARY KEY,
    customer_email TEXT    NOT NULL,
    total_cents    INTEGER NOT NULL,
    created_at     TEXT    NOT NULL
);

CREATE TABLE order_items (
    order_id         INTEGER NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    book_id          INTEGER NOT NULL REFERENCES books (id),
    quantity         INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_cents INTEGER NOT NULL,
    PRIMARY KEY (order_id, book_id)
);
