-- ═══════════════════════════════════════════════════════════════════════════
--  The bookstore database — shared by lessons 03+ and all exercises.
-- ═══════════════════════════════════════════════════════════════════════════
--
--   authors 1───* books *───1 categories ─┐ (parent_id: a category tree)
--                   │  │                   └──┘
--                   │  └───* reviews *───1 customers
--                   │                          │
--             order_items *───1 orders *────────1
--
--  * = "many", 1 = "one". E.g. one author has many books.
--  Money is stored as whole CENTS in INTEGER columns (never floating point!).
--  Timestamps are ISO-8601 TEXT in UTC, e.g. '2025-06-01T10:00:00Z'.
--
--  Edge cases on purpose (you'll need them for LEFT JOIN, NULL and EXISTS lessons):
--   • author 9 (Octavia E. Butler) has no books in the shop
--   • book 14 (Collected Notes) has no category, no year, and was never ordered
--   • customer 6 (Edsger) has no city and no orders
--   • book 3 (Emma) and 13 (Dune Messiah) are out of stock

PRAGMA foreign_keys = ON;

CREATE TABLE authors (
    id      INTEGER PRIMARY KEY,
    name    TEXT NOT NULL,
    country TEXT
);

CREATE TABLE categories (
    id        INTEGER PRIMARY KEY,
    name      TEXT NOT NULL UNIQUE,
    parent_id INTEGER REFERENCES categories (id)
);

CREATE TABLE books (
    id             INTEGER PRIMARY KEY,
    title          TEXT    NOT NULL,
    author_id      INTEGER NOT NULL REFERENCES authors (id),
    category_id    INTEGER REFERENCES categories (id),
    price_cents    INTEGER NOT NULL CHECK (price_cents >= 0),
    published_year INTEGER,
    stock          INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0)
);

CREATE TABLE customers (
    id         INTEGER PRIMARY KEY,
    email      TEXT NOT NULL UNIQUE,
    name       TEXT NOT NULL,
    city       TEXT,
    created_at TEXT NOT NULL
);

CREATE TABLE orders (
    id          INTEGER PRIMARY KEY,
    customer_id INTEGER NOT NULL REFERENCES customers (id),
    status      TEXT    NOT NULL CHECK (status IN ('pending', 'paid', 'shipped', 'cancelled')),
    created_at  TEXT    NOT NULL
);

CREATE TABLE order_items (
    order_id         INTEGER NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    book_id          INTEGER NOT NULL REFERENCES books (id),
    quantity         INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_cents INTEGER NOT NULL,
    PRIMARY KEY (order_id, book_id)
);

CREATE TABLE reviews (
    id          INTEGER PRIMARY KEY,
    book_id     INTEGER NOT NULL REFERENCES books (id),
    customer_id INTEGER NOT NULL REFERENCES customers (id),
    rating      INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body        TEXT,
    UNIQUE (book_id, customer_id)
);

INSERT INTO authors (id, name, country) VALUES
    (1, 'Frank Herbert', 'USA'),
    (2, 'Jane Austen', 'UK'),
    (3, 'Ursula K. Le Guin', 'USA'),
    (4, 'Isaac Asimov', 'USA'),
    (5, 'J.R.R. Tolkien', 'UK'),
    (6, 'Martin Kleppmann', 'UK'),
    (7, 'Brian Kernighan', 'USA'),
    (8, 'Mary Beard', 'UK'),
    (9, 'Octavia E. Butler', 'USA');

INSERT INTO categories (id, name, parent_id) VALUES
    (1, 'Fiction', NULL),
    (2, 'Science Fiction', 1),
    (3, 'Fantasy', 1),
    (4, 'Classics', 1),
    (5, 'Non-fiction', NULL),
    (6, 'Programming', 5),
    (7, 'History', 5),
    (8, 'Databases', 6);

INSERT INTO books (id, title, author_id, category_id, price_cents, published_year, stock) VALUES
    (1, 'Dune', 1, 2, 1299, 1965, 12),
    (2, 'Pride and Prejudice', 2, 4, 799, 1813, 30),
    (3, 'Emma', 2, 4, 899, 1815, 0),
    (4, 'The Left Hand of Darkness', 3, 2, 1099, 1969, 5),
    (5, 'A Wizard of Earthsea', 3, 3, 999, 1968, 8),
    (6, 'Foundation', 4, 2, 1199, 1951, 15),
    (7, 'I, Robot', 4, 2, 899, 1950, 3),
    (8, 'The Hobbit', 5, 3, 1499, 1937, 20),
    (9, 'The Lord of the Rings', 5, 3, 2999, 1954, 7),
    (10, 'Designing Data-Intensive Applications', 6, 8, 4599, 2017, 4),
    (11, 'The C Programming Language', 7, 6, 3999, 1978, 2),
    (12, 'SPQR', 8, 7, 1899, 2015, 6),
    (13, 'Dune Messiah', 1, 2, 1199, 1969, 0),
    (14, 'Collected Notes', 4, NULL, 500, NULL, 1);

INSERT INTO customers (id, email, name, city, created_at) VALUES
    (1, 'ada@example.com', 'Ada Lovelace', 'London', '2025-01-05T10:00:00Z'),
    (2, 'alan@example.com', 'Alan Turing', 'Manchester', '2025-02-11T09:30:00Z'),
    (3, 'grace@example.com', 'Grace Hopper', 'New York', '2025-03-20T14:15:00Z'),
    (4, 'linus@example.com', 'Linus Torvalds', 'Portland', '2025-04-02T08:00:00Z'),
    (5, 'margaret@example.com', 'Margaret Hamilton', 'Boston', '2025-05-17T16:45:00Z'),
    (6, 'edsger@example.com', 'Edsger Dijkstra', NULL, '2025-06-30T12:00:00Z');

INSERT INTO orders (id, customer_id, status, created_at) VALUES
    (1, 1, 'paid', '2025-06-01T10:00:00Z'),
    (2, 1, 'shipped', '2025-07-15T11:30:00Z'),
    (3, 2, 'paid', '2025-06-03T09:00:00Z'),
    (4, 3, 'cancelled', '2025-06-10T15:00:00Z'),
    (5, 3, 'shipped', '2025-08-01T10:15:00Z'),
    (6, 4, 'pending', '2025-08-20T18:00:00Z'),
    (7, 5, 'paid', '2025-09-05T13:00:00Z'),
    (8, 1, 'pending', '2025-09-10T08:45:00Z'),
    (9, 2, 'shipped', '2025-09-12T17:30:00Z'),
    (10, 5, 'shipped', '2025-10-01T09:00:00Z');

INSERT INTO order_items (order_id, book_id, quantity, unit_price_cents) VALUES
    (1, 1, 1, 1299), (1, 10, 1, 4599),
    (2, 8, 2, 1499),
    (3, 6, 1, 1199), (3, 7, 1, 899), (3, 1, 1, 1299),
    (4, 9, 1, 2999),
    (5, 2, 3, 799), (5, 3, 1, 899),
    (6, 11, 1, 3999),
    (7, 10, 1, 4599), (7, 11, 1, 3999),
    (8, 12, 1, 1899),
    (9, 1, 2, 1299), (9, 13, 1, 1199),
    (10, 5, 1, 999), (10, 4, 1, 1099), (10, 8, 1, 1499);

INSERT INTO reviews (id, book_id, customer_id, rating, body) VALUES
    (1, 1, 1, 5, 'A masterpiece'),
    (2, 1, 2, 4, 'Long but great'),
    (3, 10, 1, 5, 'Every backend dev should read this'),
    (4, 10, 5, 5, NULL),
    (5, 8, 1, 4, 'Cozy'),
    (6, 2, 3, 3, 'Classic'),
    (7, 11, 4, 5, 'Still relevant'),
    (8, 6, 2, 4, NULL),
    (9, 9, 3, 2, 'Too long for me'),
    (10, 12, 1, 4, 'Fascinating');
