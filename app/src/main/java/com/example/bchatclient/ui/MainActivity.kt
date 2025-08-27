
package com.example.bchatclient.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bchatclient.data.models.Room
import com.example.bchatclient.databinding.ActivityMainBinding
import com.example.bchatclient.network.ApiClient
import com.example.bchatclient.network.SocketManager
import com.example.bchatclient.ui.adapters.RoomAdapter
import com.example.bchatclient.utils.SharedPrefsManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var roomAdapter: RoomAdapter
    private val rooms = mutableListOf<Room>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPrefsManager(this)

        if (!prefsManager.isLoggedIn()) {
            navigateToAuth()
            return
        }

        setupUI()
        loadRooms()
        connectSocket()
    }

    private fun setupUI() {
        binding.tvWelcome.text = "Welcome, ${prefsManager.getUsername()}!"

        roomAdapter = RoomAdapter(rooms) { room ->
            openChatRoom(room)
        }

        binding.rvRooms.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = roomAdapter
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadRooms()
        }
    }

    private fun loadRooms() {
        val token = prefsManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getRooms("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { roomList ->
                        rooms.clear()
                        rooms.addAll(roomList)
                        roomAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load rooms", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun connectSocket() {
        val token = prefsManager.getToken()
        if (token != null) {
            SocketManager.connect(token)
        }
    }

    private fun openChatRoom(room: Room) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("room", room)
        startActivity(intent)
    }

    private fun logout() {
        SocketManager.disconnect()
        prefsManager.clearAuthData()
        navigateToAuth()
    }

    private fun navigateToAuth() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.disconnect()
    }
}
