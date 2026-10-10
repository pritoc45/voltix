package com.example.voltix.core

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.isNotBlank() && emailRegex.matches(email.trim())
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}