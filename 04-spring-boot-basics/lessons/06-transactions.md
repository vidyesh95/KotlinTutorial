# Lesson 06 — Transactions with `@Transactional`

| | |
|---|---|
| **Goal** | Make multi-step database work all-or-nothing with `@Transactional`, understand how Spring does it (proxies), and avoid the classic traps. |
| **Needs** | Lesson 05; module 03, lesson 12 (transactions in raw JDBC) |
| **Time** | ~40 min |
| **Next** | [07 — Testing](07-testing.md) |
| **Exercise** | `src/main/kotlin/exercises/ex06/` |

## §1. The problem

[`orders/OrderService.kt`](../src/main/kotlin/bookstore/orders/OrderService.kt): placing an order
1. takes stock for **each** line;
2. inserts the order row;
3. inserts every order line.

Say line 1 (2 × Dune) succeeds and line 2 (Emma) is out of stock. Without a transaction, Dune's stock has
**already been decremented** and stays wrong forever: books vanish from the inventory without being sold.

> 🧒 **Like I'm 5:**
> At the supermarket till the cashier scans everything, then you pay. If your card is declined, the cashier
> cancels the **whole receipt**. You don't walk out with half your shopping, and the shop doesn't lose the
> scanned items. A transaction is that receipt: everything on it happens, or nothing does.

## §2. One annotation

```kotlin
@Service
class OrderService(private val books: BookRepository, private val jdbc: JdbcClient, private val clock: Clock) {

    @Transactional
    fun placeOrder(customerEmail: String, lines: List<OrderLine>): Order {
        for (line in lines) {
            val book = books.findById(line.bookId) ?: throw NotFoundException("Book", line.bookId)
            if (books.takeStock(book.id, line.quantity) == 0) {
                throw OutOfStockException(book.id, line.quantity, book.stock)   // → ROLLBACK
            }
            …
        }
        // insert order + lines …
        return Order(…)                                                         // → COMMIT
    }
}
```

The rules:
- the method returns normally → **COMMIT**;
- the method throws a `RuntimeException` (or an `Error`) → **ROLLBACK**, and every statement inside is undone.

Every `JdbcClient` call inside the method, including the ones in `BookRepository`, runs on the **same
connection, in the same transaction**. Spring keeps the transaction's connection in a thread-bound holder, and
`JdbcClient` asks for it. That's the "joins the current transaction" from lesson 05.

### Try it

```bash
# Dune has 12 in stock. Order 2 × Dune + 1 × Pride and Prejudice → 201
curl -s -X POST 'localhost:8080/api/orders' -H 'Content-Type: application/json' \
  -d '{"customerEmail":"ada@example.com","lines":[{"bookId":1,"quantity":2},{"bookId":3,"quantity":1}]}'
# {"id":1,"customerEmail":"ada@example.com","totalCents":3597,"createdAt":"2026-11-21T19:48:51.578450Z"}

# 1 × Dune + 1 × Emma (stock 0) → 409, and the Dune line is rolled back
curl -s -X POST 'localhost:8080/api/orders' -H 'Content-Type: application/json' \
  -d '{"customerEmail":"bob@example.com","lines":[{"bookId":1,"quantity":1},{"bookId":4,"quantity":1}]}'
# {"detail":"Book 4: requested 1, only 0 in stock","status":409,"title":"Out of stock","bookId":4}

curl -s "localhost:8080/api/books/1"
# {"id":1,"title":"Dune",…,"stock":10,…}   ← 12 − 2 from the first order. Bob's failed order took nothing.
```

The automated proof is in
[`OrderTransactionTest`](../src/test/kotlin/bookstore/OrderTransactionTest.kt) (lesson 07).

## §3. How it works: proxies

Spring doesn't edit your method. When it creates the `OrderService` bean, it notices `@Transactional` and
hands everyone a **proxy**: a generated subclass that wraps your object.

```text
OrderController ──calls──▶ [ proxy: OrderService$$SpringCGLIB ]
                               │ 1. begin transaction (borrow connection, autoCommit = false)
                               │ 2. call the real OrderService.placeOrder(...)
                               │ 3a. returned?   → commit
                               │ 3b. threw?      → rollback, rethrow
                               ▼
                           [ your OrderService object ]
```

> 🧒 **Like I'm 5:** you don't walk into the doctor's room directly. A receptionist opens your file before
> you go in, and closes it properly when you come out, or marks it "cancelled" if something went wrong. You
> never see the receptionist work, but they're always there.

That's also why we need `kotlin("plugin.spring")` (applied by our `tutorial.spring-boot` convention
plugin). A proxy is a **subclass**, and Kotlin classes are `final` by default. The plugin opens classes
annotated with `@Service`, `@Component`, `@Transactional` and so on. This is module 02, lesson 05's "proxies
for @Transactional", now for real.

Compare with module 03, lesson 12, where you wrote `transaction { }` by hand: `autoCommit = false`,
try/commit, catch/rollback. `@Transactional` is that helper, applied from outside.

## §4. The traps

### ⚠️ Gotcha 1: self-invocation skips the proxy

```kotlin
@Service
class BookService(…) {
    fun importAll(books: List<NewBook>) = books.forEach { create(it) }   // ❌ NOT transactional!

    @Transactional
    fun create(newBook: NewBook): Book { … }
}
```

`importAll` calls `this.create(...)`: a direct call on the real object, not through the proxy, so the
receptionist never sees it. Fixes: put `@Transactional` on the *outer* method that's called from another bean,
or move `create` into a different bean.

### ⚠️ Gotcha 2: checked exceptions don't roll back

Spring's default (inherited from Java) is to roll back on `RuntimeException` and `Error`, **not** on
*checked* exceptions. Kotlin has no checked exceptions, but Java libraries still throw them: `IOException`,
`SQLException` if you use raw JDBC, `java.util.concurrent.TimeoutException`. If one escapes a `@Transactional`
method, Spring **commits** whatever ran before it.

```kotlin
@Transactional(rollbackFor = [Exception::class])   // roll back on everything
fun importFromFile(path: Path) { … }
```

That's why our domain exceptions extend `RuntimeException` (see
[`BookstoreExceptions.kt`](../src/main/kotlin/bookstore/BookstoreExceptions.kt)), and why `DataAccessException`
is a `RuntimeException` too.

### ⚠️ Gotcha 3: catching the exception yourself

```kotlin
@Transactional
fun placeOrder(…) {
    try { books.takeStock(…) } catch (e: Exception) { log.warn("ignored") }   // swallowed → commits!
}
```

If the exception never leaves the method, the proxy sees a normal return and **commits**.

### ⚠️ Gotcha 4: private or final methods

Proxies can only wrap methods they can override, so `@Transactional` on a `private` function does nothing.

## §5. Check-then-act: let the database decide

`OrderService` doesn't read the stock, compare it in Kotlin, and then write. It does both in **one**
statement ([`BookRepository.takeStock`](../src/main/kotlin/bookstore/books/BookRepository.kt)):

```sql
UPDATE books SET stock = stock - :quantity WHERE id = :id AND stock >= :quantity
```

`update()` returns `1` if the stock was taken and `0` if there wasn't enough. Why not read first?

```text
Customer A: SELECT stock → 1        Customer B: SELECT stock → 1
Customer A: 1 >= 1 ✓ UPDATE → 0     Customer B: 1 >= 1 ✓ UPDATE → -1   ❌ sold the same copy twice
```

This is a **race condition**. A transaction alone doesn't prevent it at every isolation level. The atomic
`UPDATE … WHERE stock >= :quantity`, backed by the `CHECK (stock >= 0)` constraint as a last line of defence,
does.

> 🐘 **Postgres note:** Postgres uses MVCC with **READ COMMITTED** isolation by default. Concurrent
> transactions run in parallel, and the atomic UPDATE pattern above is still the simplest correct choice.
> Alternatives are `SELECT … FOR UPDATE` (lock the row while you decide) or
> `@Transactional(isolation = Isolation.SERIALIZABLE)` plus a retry on serialization failure. SQLite is
> simpler: one writer at a time, so write transactions never interleave, but they queue up (`busy_timeout`).

## §6. Options worth knowing

```kotlin
@Transactional(readOnly = true)         // hint: no writes; some databases optimise, and it documents intent
@Transactional(timeout = 5)             // seconds
@Transactional(propagation = Propagation.REQUIRES_NEW)   // always start a NEW transaction (e.g. audit log that must survive a rollback)
```

**Propagation** answers "what if a transaction is already open?". The default, `REQUIRED`, means join it.
So when `BookService.create` (transactional) is called from another transactional method, it all becomes
**one** transaction.

> 🏗️ **In the real world:** put `@Transactional` on **service** methods, the unit of business work. Not on
> controllers (too wide, and it holds a connection during JSON serialisation). Not on repositories (too
> narrow: each call would commit separately). Keep transactions short: never call a slow external HTTP API
> while holding one.

## Recap

- `@Transactional` = begin → run → commit, or roll back on a `RuntimeException`.
- It works through a proxy, which is why `kotlin("plugin.spring")` is needed, and why self-calls and private
  methods aren't intercepted.
- Checked Java exceptions and swallowed exceptions **commit**. Use `rollbackFor`, or don't catch.
- Prefer atomic SQL (`UPDATE … WHERE stock >= n`) over read-then-write.
- Annotate service methods, and keep transactions short.
