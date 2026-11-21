package solutions.ex08

import exercises.ex08.Ex08

object Ex08Solution : Ex08 {
    override val indexOrdersByCustomer = "CREATE INDEX idx_orders_customer_id ON orders (customer_id)"

    override val uniqueEmailIgnoringCase = "CREATE UNIQUE INDEX ux_customers_email_lower ON customers (lower(email))"

    override val coveringIndexForCategoryListing =
        "CREATE INDEX idx_books_category_title_price ON books (category_id, title, price_cents)"

    override val partialIndexInStock = "CREATE INDEX idx_books_in_stock_price ON books (price_cents) WHERE stock > 0"
}
