package bookstore.auth.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val sessionTokenFilter: SessionTokenAuthenticationFilter,
    private val authEntryPoint: ProblemDetailsAuthenticationEntryPoint,
    private val accessDeniedHandler: ProblemDetailsAccessDeniedHandler,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // REST API uses bearer tokens & DB sessions, so disable standard Spring CSRF
            .csrf { it.disable() }
            // Do not store session state in HTTP server memory; sessions are tracked in SQLite
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(authEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            }
            .authorizeHttpRequests { auth ->
                auth
                    // Public authentication endpoints
                    .requestMatchers(HttpMethod.POST, "/api/auth/sign-up", "/api/auth/sign-in").permitAll()
                    // Public book browsing
                    .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()
                    // Admin restricted endpoints
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                    // Ops and system endpoints
                    .requestMatchers("/error", "/actuator/**").permitAll()
                    // Exercises and solutions endpoints have their own specific access rules
                    .requestMatchers("/exercises/**", "/solutions/**").permitAll()
                    // All other endpoints require an active session
                    .anyRequest().authenticated()
            }
            .addFilterBefore(sessionTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
