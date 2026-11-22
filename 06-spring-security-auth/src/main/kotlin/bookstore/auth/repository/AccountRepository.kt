package bookstore.auth.repository

import bookstore.auth.domain.Account
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class AccountRepository(private val jdbcClient: JdbcClient) {

    fun findByUserIdAndProvider(userId: String, providerId: String): Account? =
        jdbcClient.sql(
            """
            SELECT id, user_id, account_id, provider_id, password, created_at, updated_at
            FROM account
            WHERE user_id = :userId AND provider_id = :providerId
            """.trimIndent()
        )
            .param("userId", userId)
            .param("providerId", providerId)
            .query { rs, _ ->
                Account(
                    id = rs.getString("id"),
                    userId = rs.getString("user_id"),
                    accountId = rs.getString("account_id"),
                    providerId = rs.getString("provider_id"),
                    passwordHash = rs.getString("password"),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    updatedAt = Instant.parse(rs.getString("updated_at")),
                )
            }
            .optional()
            .orElse(null)

    fun findByProviderAndAccountId(providerId: String, accountId: String): Account? =
        jdbcClient.sql(
            """
            SELECT id, user_id, account_id, provider_id, password, created_at, updated_at
            FROM account
            WHERE provider_id = :providerId AND account_id = :accountId
            """.trimIndent()
        )
            .param("providerId", providerId)
            .param("accountId", accountId)
            .query { rs, _ ->
                Account(
                    id = rs.getString("id"),
                    userId = rs.getString("user_id"),
                    accountId = rs.getString("account_id"),
                    providerId = rs.getString("provider_id"),
                    passwordHash = rs.getString("password"),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    updatedAt = Instant.parse(rs.getString("updated_at")),
                )
            }
            .optional()
            .orElse(null)

    fun insert(account: Account): Account {
        jdbcClient.sql(
            """
            INSERT INTO account (id, user_id, account_id, provider_id, password, created_at, updated_at)
            VALUES (:id, :userId, :accountId, :providerId, :password, :createdAt, :updatedAt)
            """.trimIndent()
        )
            .param("id", account.id)
            .param("userId", account.userId)
            .param("accountId", account.accountId)
            .param("providerId", account.providerId)
            .param("password", account.passwordHash)
            .param("createdAt", account.createdAt.toString())
            .param("updatedAt", account.updatedAt.toString())
            .update()
        return account
    }

    fun updatePassword(id: String, newPasswordHash: String): Int =
        jdbcClient.sql("UPDATE account SET password = :password, updated_at = :updatedAt WHERE id = :id")
            .param("password", newPasswordHash)
            .param("updatedAt", Instant.now().toString())
            .param("id", id)
            .update()
}
