
package com.example.bchatclient.network

import com.example.bchatclient.data.models.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>

    @GET("api/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>

    @GET("api/rooms")
    suspend fun getRooms(@Header("Authorization") token: String): Response<List<Room>>

    @POST("api/rooms")
    suspend fun createRoom(
        @Header("Authorization") token: String,
        @Body createRoomRequest: CreateRoomRequest
    ): Response<Room>

    @DELETE("api/rooms/{roomId}")
    suspend fun deleteRoom(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: String
    ): Response<ResponseBody>

    @POST("api/rooms/{roomId}/verify-pin")
    suspend fun verifyRoomPin(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: String,
        @Body verifyPinRequest: VerifyPinRequest
    ): Response<ResponseBody>

    @GET("api/rooms/{roomId}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: String
    ): Response<List<Message>>

    @Multipart
    @POST("api/upload")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("room_id") roomId: String
    ): Response<ResponseBody>

    @GET("api/files/{roomId}/{fileName}")
    suspend fun downloadFile(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: String,
        @Path("fileName") fileName: String
    ): Response<ResponseBody>
}
