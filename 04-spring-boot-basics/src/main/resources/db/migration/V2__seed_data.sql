-- Flyway migration V2: starter data so the API isn't empty on first run.
INSERT INTO authors (id, name, created_at) VALUES
    (1, 'Frank Herbert', '2026-01-01T00:00:00Z'),
    (2, 'Jane Austen', '2026-01-01T00:00:00Z'),
    (3, 'Martin Kleppmann', '2026-01-01T00:00:00Z');

INSERT INTO books (id, title, author_id, isbn, price_cents, stock, created_at, updated_at) VALUES
    (1, 'Dune', 1, '9780441172719', 1299, 12, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    (2, 'Dune Messiah', 1, '9780593098233', 1199, 2, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    (3, 'Pride and Prejudice', 2, '9780141439518', 799, 30, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    (4, 'Emma', 2, '9780141439587', 899, 0, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    (5, 'Designing Data-Intensive Applications', 3, '9781449373320', 4599, 4, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z');
