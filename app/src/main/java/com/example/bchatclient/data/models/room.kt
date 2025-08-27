
package com.example.bchatclient.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(
    val room_id: String,
    val room_name: String,
    val description: String,
    val created_by: String,
    val is_private: Boolean,
    val room_type: String,
    val created_at: String
) : Parcelable

@Parcelize
data class CreateRoomRequest(
    val room_name: String,
    val description: String = "",
    val is_private: Boolean = false,
    val room_pin: String = ""
) : Parcelable

@Parcelize
data class VerifyPinRequest(
    val room_pin: String
) : Parcelable
