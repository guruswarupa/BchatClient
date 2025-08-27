
package com.example.bchatclient.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bchatclient.data.models.Message
import com.example.bchatclient.data.models.Room
import com.example.bchatclient.databinding.ActivityChatBinding
import com.example.bchatclient.network.ApiClient
import com.example.bchatclient.network.SocketManager
import com.example.bchatclient.ui.adapters.MessageAdapter
import com.example.bchatclient.utils.SharedPrefsManager
import com.github.dhaval2404.imagepicker.ImagePicker
import io.socket.client.Socket
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var room: Room
    private val messages = mutableListOf<Message>()
    private var socket: Socket? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                uploadFile(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPrefsManager(this)
        room = intent.getParcelableExtra("room") ?: return finish()

        setupUI()
        setupSocket()
        loadMessages()
    }

    private fun setupUI() {
        binding.tvRoomName.text = room.room_name

        messageAdapter = MessageAdapter(messages, prefsManager.getUserId() ?: "")
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.btnAttach.setOnClickListener {
            showFileOptions()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSocket() {
        socket = SocketManager.getSocket()

        socket?.let { s ->
            // Join room
            s.emit("join_room", room.room_id)

            // Listen for new messages
            s.on("new_message") { args ->
                if (args.isNotEmpty()) {
                    try {
                        val messageData = args[0] as JSONObject
                        val message = parseMessage(messageData)

                        runOnUiThread {
                            messages.add(message)
                            messageAdapter.notifyItemInserted(messages.size - 1)
                            binding.rvMessages.scrollToPosition(messages.size - 1)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun parseMessage(json: JSONObject): Message {
        return Message(
            message_id = json.getString("message_id"),
            user_id = json.getString("user_id"),
            username = json.getString("username"),
            room_id = json.getString("room_id"),
            content = json.getString("content"),
            message_type = json.getString("message_type"),
            timestamp = json.getString("timestamp"),
            file_url = json.optString("file_url").takeIf { it.isNotEmpty() }
        )
    }

    private fun loadMessages() {
        val token = prefsManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getMessages("Bearer $token", room.room_id)

                if (response.isSuccessful) {
                    response.body()?.let { messageList ->
                        messages.clear()
                        messages.addAll(messageList)
                        messageAdapter.notifyDataSetChanged()
                        if (messages.isNotEmpty()) {
                            binding.rvMessages.scrollToPosition(messages.size - 1)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendMessage() {
        val content = binding.etMessage.text.toString().trim()
        if (content.isEmpty()) return

        val messageData = JSONObject().apply {
            put("room_id", room.room_id)
            put("content", content)
            put("message_type", "text")
        }

        socket?.emit("send_message", messageData)
        binding.etMessage.setText("")
    }

    private fun showFileOptions() {
        ImagePicker.with(this)
            .galleryOnly()
            .start { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        uploadFile(uri)
                    }
                }
            }
    }

    private fun uploadFile(uri: Uri) {
        val token = prefsManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val file = File(getRealPathFromURI(uri))
                val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = ApiClient.apiService.uploadFile("Bearer $token", body, room.room_id)

                if (response.isSuccessful) {
                    Toast.makeText(this@ChatActivity, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ChatActivity, "Failed to upload file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ChatActivity, "Upload error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(android.provider.MediaStore.Images.ImageColumns.DATA)
            it.moveToFirst()
            return it.getString(index)
        }
        return uri.path ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        socket?.off("new_message")
    }
}
