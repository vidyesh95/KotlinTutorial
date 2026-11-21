package solutions.ex10

import exercises.ex10.Ex10

object Ex10Solution : Ex10 {
    override val createLibrarySchema = """
        CREATE TABLE members (
            id    INTEGER PRIMARY KEY,
            email TEXT NOT NULL UNIQUE,
            name  TEXT NOT NULL
        );
        CREATE TABLE books (
            id    INTEGER PRIMARY KEY,
            title TEXT NOT NULL,
            isbn  TEXT NOT NULL UNIQUE
        );
        CREATE TABLE loans (
            id          INTEGER PRIMARY KEY,
            member_id   INTEGER NOT NULL REFERENCES members (id),
            book_id     INTEGER NOT NULL REFERENCES books (id),
            loaned_at   TEXT    NOT NULL,
            returned_at TEXT
        );
        CREATE TABLE tags (
            id   INTEGER PRIMARY KEY,
            name TEXT NOT NULL UNIQUE
        );
        CREATE TABLE book_tags (
            book_id INTEGER NOT NULL REFERENCES books (id),
            tag_id  INTEGER NOT NULL REFERENCES tags (id),
            PRIMARY KEY (book_id, tag_id)
        );
    """.trimIndent()

    override val openLoans = """
        SELECT m.name, b.title
        FROM loans l
        JOIN members m ON m.id = l.member_id
        JOIN books b ON b.id = l.book_id
        WHERE l.returned_at IS NULL
        ORDER BY l.loaned_at
    """.trimIndent()

    override val booksWithTags = """
        SELECT b.title, group_concat(t.name, ',' ORDER BY t.name) AS tags
        FROM books b
        LEFT JOIN book_tags bt ON bt.book_id = b.id
        LEFT JOIN tags t ON t.id = bt.tag_id
        GROUP BY b.id
        ORDER BY b.title
    """.trimIndent()
}
