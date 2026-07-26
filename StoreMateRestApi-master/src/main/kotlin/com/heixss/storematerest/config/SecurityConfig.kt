package com.heixss.storematerest.config

import com.heixss.storematerest.security.JwtAuthenticationFilter
import com.heixss.storematerest.security.RestAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtFilter: JwtAuthenticationFilter,
    private val authenticationEntryPoint: RestAuthenticationEntryPoint
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .headers { it.frameOptions { frame -> frame.disable() } }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/h2-console/**", "/api/auth/login", "/api/auth/register")
                    .permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // Return a JSON 401 instead of Spring's default HTML login redirect,
            // so the Android client can detect an expired token reliably.
            .exceptionHandling { it.authenticationEntryPoint(authenticationEntryPoint) }

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
