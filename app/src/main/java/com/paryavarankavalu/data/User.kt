package com.paryavarankavalu.data

data class User(
    val name: String,
    val password: String,
    val role: String,
    val karma: Int
)

object UserRole {
    const val CITIZEN = "Ecolove Citizen"
    const val WORKER = "Community Worker"
}
