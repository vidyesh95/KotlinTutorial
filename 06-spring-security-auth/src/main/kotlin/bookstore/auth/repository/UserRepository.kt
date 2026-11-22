package bookstore.auth.repository

import bookstore.auth.domain.Role
import bookstore.auth.domain.User
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class UserRepository(private val jdbcClient: JdbcClient) {

    fun findById(id: String): User? =
        jdbcClient.sql(
            """
            SELECT id, name, email, email_verified, image, role, created_at, updated_at
            FROM user
            WHERE id = :id
            """.trimIndent()
        )
            .param("id", id)
            .query { rs, _ ->
                User(
                    id = rs.getString("id"),
                    name = rs.getString("name"),
                    email = rs.getString("email"),
                    emailVerified = rs.getInt("email_verified") == 1,
                    image = rs.getString("image"),
                    role = Role.valueOf(rs.getString("role")),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    updatedAt = Instant.parse(rs.getString("updated_at")),
                )
            }
            .optional()
            .orElse(null)

    fun findByEmail(email: String): User? =
        jdbcClient.sql(
            """
            SELECT id, name, email, email_verified, image, role, created_at, updated_at
            FROM user
            WHERE lower(email) = lower(:email)
            """.trimIndent()
        )
            .param("email", email)
            .query { rs, _ ->
                User(
                    id = rs.getString("id"),
                    name = rs.getString("name"),
                    email = rs.getString("email"),
                    emailVerified = rs.getInt("email_verified") == 1,
                    image = rs.getString("image"),
                    role = Role.valueOf(rs.getString("role")),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    updatedAt = Instant.parse(rs.getString("updated_at")),
                )
            }
            .optional()
            .orElse(null)

    fun insert(user: User): User {
        jdbcClient.sql(
            """
            INSERT INTO user (id, name, email, email_verified, image, role, created_at, updated_at)
            VALUES (:id, :name, :email, :emailVerified, :image, :role, :createdAt, :updatedAt)
            """.trimIndent()
        )
            .param("id", user.id)
            .param("name", user.name)
            .param("email", user.email)
            .param("emailVerified", if (user.emailVerified) 1 else 0)
            .param("image", user.image)
            .param("role", user.role.name)
            .param("createdAt", user.createdAt.toString())
            .param("updatedAt", user.updatedAt.toString())
            .update()
        return user
    }

    fun updateRole(id: String, role: Role): Int =
        jdbcClient.sql("UPDATE user SET role = :role, updated_at = :updatedAt WHERE id = :id")
            .param("role", role.name)
            .param("updatedAt", Instant.now().toString())
            .param("id", id)
            .update()
}
