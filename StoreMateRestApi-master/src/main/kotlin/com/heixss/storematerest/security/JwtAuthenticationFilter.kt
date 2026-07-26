package com.heixss.storematerest.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Only the credential-exchange endpoints are unauthenticated. /api/auth/me
        // deliberately is not: the app calls it to validate a stored token.
        if (request.servletPath in UNAUTHENTICATED_PATHS ||
            request.servletPath.startsWith("/h2-console")
        ) {
            filterChain.doFilter(request, response)
            return
        }

        val token = request.getHeader("Authorization")
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.trim()

        if (token != null && jwtUtil.validateToken(token)) {
            try {
                val userDetails =
                    userDetailsService.loadUserByUsername(jwtUtil.getUsernameFromToken(token))
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            } catch (ex: UsernameNotFoundException) {
                // Valid signature but the account is gone — leave the context
                // empty so the entry point renders a 401.
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "
        val UNAUTHENTICATED_PATHS = setOf("/api/auth/login", "/api/auth/register")
    }
}
