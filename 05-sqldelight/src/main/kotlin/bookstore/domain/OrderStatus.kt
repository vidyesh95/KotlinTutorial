package bookstore.domain

/** Stored as TEXT ('PENDING', 'PAID', …) via SQLDelight's EnumColumnAdapter. */
enum class OrderStatus { PENDING, PAID, SHIPPED, CANCELLED }
