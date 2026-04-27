package com.example.foss101.model

data class User(
    val id: String,
    val email: String,
    val displayName: String
)

data class AuthSession(
    val token: String,
    val user: User
)
