-- V3: a DATA migration (fill the new column for existing rows), then enforce uniqueness.
UPDATE books SET isbn = 'unknown-' || id WHERE isbn IS NULL;
CREATE UNIQUE INDEX books_isbn_unique ON books (isbn);
