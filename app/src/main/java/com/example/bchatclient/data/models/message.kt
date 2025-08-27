
package com.example.bchatclient.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val message_id: String,
    val user_id: String,
    val username: String,
    val room_id: String,
    val content: String,
    val message_type: String,
    val timestamp: String,
    val created_at: String? = null,
    val file_url: String? = null,
    val blockchain_hash: String? = null,
    val is_edited: Boolean = false,
    val edited_at: String? = null
) : Parcelable

@Parcelize
data class SendMessageRequest(
    val content: String,
    val message_type: String = "text"
) : Parcelable
