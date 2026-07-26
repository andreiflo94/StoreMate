package com.heixss.storematerest.controller

import com.heixss.storematerest.model.AuthRequest
import com.heixss.storematerest.model.AuthResponse
import com.heixss.storematerest.model.CurrentUserResponse
import com.heixss.storematerest.security.JwtUtil
import com.heixss.storematerest.service.UserService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) {

    /** Registers the user and logs them straight in, so the app needs one round trip. */
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: AuthRequest): AuthResponse {
        val user = userService.register(req.username, req.password)
        return AuthResponse(
            token = jwtUtil.generateToken(user.username),
            username = user.username,
            storeName = user.store.name,
            expiresInSeconds = jwtUtil.expiresInSeconds()
        )
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: AuthRequest): AuthResponse {
        val user = userService.authenticate(req.username, req.password)
        return AuthResponse(
            token = jwtUtil.generateToken(user.username),
            username = user.username,
            storeName = user.store.name,
            expiresInSeconds = jwtUtil.expiresInSeconds()
        )
    }

    /** Lets the app validate a stored token on launch and show the store name. */
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: UserDetails): CurrentUserResponse {
        val user = userService.getByUsername(principal.username)
        return CurrentUserResponse(
            username = user.username,
            storeId = user.store.id,
            storeName = user.store.name
        )
    }
}
