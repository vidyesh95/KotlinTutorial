package exercises.ex10

/*
 * Exercise 10 — Normalized schema design      (lesson: lesson10_normalization_and_design)
 *
 * Design a small LIBRARY database. Tests run your DDL on an EMPTY database (foreign keys ON),
 * insert data and check that the rules hold. Replace every TODO(...) then run:
 *     ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex10*'
 */
interface Ex10 {
    /**
     * 10.1 — DDL (several statements) creating exactly these tables and columns:
     *   members   (id INTEGER PRIMARY KEY, email TEXT required & unique, name TEXT required)
     *   books     (id INTEGER PRIMARY KEY, title TEXT required, isbn TEXT required & unique)
     *   loans     (id INTEGER PRIMARY KEY, member_id → members.id required, book_id → books.id required,
     *              loaned_at TEXT required, returned_at TEXT nullable)
     *   tags      (id INTEGER PRIMARY KEY, name TEXT required & unique)
     *   book_tags (book_id → books.id, tag_id → tags.id, the PAIR is the primary key)
     */
    val createLibrarySchema: String

    /**
     * 10.2 — Loans that are NOT returned yet. Columns: name (member), title (book). ORDER BY loaned_at.
     */
    val openLoans: String

    /**
     * 10.3 — Every book with its tags as ONE comma-separated text, tags sorted by name (group_concat … ORDER BY);
     * books without tags show NULL. Columns: title, tags. ORDER BY title.
     */
    val booksWithTags: String
}

object Ex10Exercise : Ex10 {
    override val createLibrarySchema: String get() = TODO("10.1")
    override val openLoans: String get() = TODO("10.2")
    override val booksWithTags: String get() = TODO("10.3")
}
