-- Flyway migration V3: Bookstore business domain
-- Orders are now tied to authenticated users (user_id REFERENCES user(id)).

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
    id          INTEGER PRIMARY KEY,
    user_id     TEXT    NOT NULL REFERENCES user (id),
    total_cents INTEGER NOT NULL CHECK (total_cents >= 0),
    created_at  TEXT    NOT NULL
);

CREATE INDEX orders_user_id_idx ON orders (user_id);

CREATE TABLE order_items (
    order_id         INTEGER NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    book_id          INTEGER NOT NULL REFERENCES books (id),
    quantity         INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_cents INTEGER NOT NULL CHECK (unit_price_cents >= 0),
    PRIMARY KEY (order_id, book_id)
);
