package com.example.foss101.data.repository

import com.example.foss101.data.auth.AuthApiException
import com.example.foss101.data.auth.AuthApiService
import com.example.foss101.data.auth.TokenStorage
import com.example.foss101.model.AuthSession
import com.example.foss101.model.User

interface AuthRepository {
    suspend fun signup(email: String, password: String, displayName: String): AuthSession
    suspend fun login(email: String, password: String): AuthSession
    suspend fun refreshSession(): User?
    fun currentUser(): User?
    fun isLoggedIn(): Boolean
    fun token(): String?
    fun logout()
}

class ApiAuthRepository(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun signup(
        email: String,
        password: String,
        displayName: String
    ): AuthSession {
        val session = authApiService.signup(email, password, displayName)
        persist(session)
        return session
    }

    override suspend fun login(email: String, password: String): AuthSession {
        val session = authApiService.login(email, password)
        persist(session)
        return session
    }

    override suspend fun refreshSession(): User? {
        val token = tokenStorage.getToken() ?: return null
        return try {
            val user = authApiService.fetchMe(token)
            tokenStorage.saveDisplayName(user.displayName)
            tokenStorage.saveEmail(user.email)
            tokenStorage.saveUserId(user.id)
            user
        } catch (error: AuthApiException) {
            if (error.statusCode == 401) {
                tokenStorage.clear()
            }
            null
        }
    }

    override fun currentUser(): User? {
        val id = tokenStorage.getUserId() ?: return null
        val email = tokenStorage.getEmail() ?: return null
        val displayName = tokenStorage.getDisplayName() ?: return null
        return User(id = id, email = email, displayName = displayName)
    }

    override fun isLoggedIn(): Boolean = tokenStorage.getToken() != null

    override fun token(): String? = tokenStorage.getToken()

    override fun logout() {
        tokenStorage.clear()
    }

    private fun persist(session: AuthSession) {
        tokenStorage.saveToken(session.token)
        tokenStorage.saveDisplayName(session.user.displayName)
        tokenStorage.saveEmail(session.user.email)
        tokenStorage.saveUserId(session.user.id)
    }
}
