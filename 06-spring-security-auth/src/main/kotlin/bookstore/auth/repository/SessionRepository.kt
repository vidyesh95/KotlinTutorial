package bookstore.auth.repository

import bookstore.auth.domain.Session
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class SessionRepository(private val jdbcClient: JdbcClient) {

    fun findByToken(token: String): Session? =
        jdbcClient.sql(
            """
            SELECT id, user_id, token, expires_at, ip_address, user_agent, created_at, updated_at
            FROM session
            WHERE token = :token
            """.trimIndent()
        )
            .param("token", token)
            .query { rs, _ ->
                Session(
                    id = rs.getString("id"),
                    userId = rs.getString("user_id"),
                    token = rs.getString("token"),
                    expiresAt = Instant.parse(rs.getString("expires_at")),
                    ipAddress = rs.getString("ip_address"),
                    userAgent = rs.getString("user_agent"),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    updatedAt = Instant.parse(rs.getString("updated_at")),
                )
            }
            .optional()
            .orElse(null)

    fun findByUserId(userId: String): List<Session> =
        jdbcClient.sql(
            """
            SELECT id, user_id, token, expires_at, ip_address, user_agent, created_at, updated_at
            FROM session
            WHERE user_id = :userId
            ORDER BY created_at DESC
            """.trimIndent()
        )
            .param("userId", userId)
            .query { rs, _ ->
                Session(
                    id = rs.getString("id"),
                    userId = rs.getString("user_id"),
                    token = rs.getString("token"),
                    expiresAt = Instant.parse(rs.getString("expires_at")),
                    ipAddress = rs.getString("ip_address"),
                    userAgent = rs.getString("user_agent"),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    updatedAt = Instant.parse(rs.getString("updated_at")),
                )
            }
            .list()

    fun insert(session: Session): Session {
        jdbcClient.sql(
            """
            INSERT INTO session (id, user_id, token, expires_at, ip_address, user_agent, created_at, updated_at)
            VALUES (:id, :userId, :token, :expiresAt, :ipAddress, :userAgent, :createdAt, :updatedAt)
            """.trimIndent()
        )
            .param("id", session.id)
            .param("userId", session.userId)
            .param("token", session.token)
            .param("expiresAt", session.expiresAt.toString())
            .param("ipAddress", session.ipAddress)
            .param("userAgent", session.userAgent)
            .param("createdAt", session.createdAt.toString())
            .param("updatedAt", session.updatedAt.toString())
            .update()
        return session
    }

    fun deleteByToken(token: String): Int =
        jdbcClient.sql("DELETE FROM session WHERE token = :token")
            .param("token", token)
            .update()

    fun deleteByUserId(userId: String): Int =
        jdbcClient.sql("DELETE FROM session WHERE user_id = :userId")
            .param("userId", userId)
            .update()

    fun deleteByUserIdExcept(userId: String, keepToken: String): Int =
        jdbcClient.sql("DELETE FROM session WHERE user_id = :userId AND token <> :keepToken")
            .param("userId", userId)
            .param("keepToken", keepToken)
            .update()

    fun deleteExpired(now: Instant = Instant.now()): Int =
        jdbcClient.sql("DELETE FROM session WHERE expires_at < :now")
            .param("now", now.toString())
            .update()

    fun updateExpiresAt(token: String, newExpiresAt: Instant): Int =
        jdbcClient.sql("UPDATE session SET expires_at = :expiresAt, updated_at = :updatedAt WHERE token = :token")
            .param("expiresAt", newExpiresAt.toString())
            .param("updatedAt", Instant.now().toString())
            .param("token", token)
            .update()
}
