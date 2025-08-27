
package com.example.bchatclient.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val user_id: String,
    val username: String,
    val email: String? = null,
    val avatar_url: String? = null,
    val is_online: Boolean = false,
    val created_at: String? = null
) : Parcelable

@Parcelize
data class LoginRequest(
    val username: String,
    val password: String
) : Parcelable

@Parcelize
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
) : Parcelable

@Parcelize
data class AuthResponse(
    val token: String,
    val user: User
) : Parcelable
