
package com.example.bchatclient.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bchatclient.data.models.LoginRequest
import com.example.bchatclient.data.models.RegisterRequest
import com.example.bchatclient.databinding.ActivityAuthBinding
import com.example.bchatclient.network.ApiClient
import com.example.bchatclient.utils.SharedPrefsManager
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var prefsManager: SharedPrefsManager
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPrefsManager(this)

        // Check if already logged in
        if (prefsManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        binding.btnAuth.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegister()
            }
        }

        binding.btnToggleMode.setOnClickListener {
            toggleMode()
        }

        updateUI()
    }

    private fun toggleMode() {
        isLoginMode = !isLoginMode
        updateUI()
    }

    private fun updateUI() {
        if (isLoginMode) {
            binding.etEmail.visibility = android.view.View.GONE
            binding.btnAuth.text = "Login"
            binding.btnToggleMode.text = "Need an account? Register"
            binding.tvTitle.text = "Login to BChat"
        } else {
            binding.etEmail.visibility = android.view.View.VISIBLE
            binding.btnAuth.text = "Register"
            binding.btnToggleMode.text = "Have an account? Login"
            binding.tvTitle.text = "Register for BChat"
        }
    }

    private fun performLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnAuth.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.login(LoginRequest(username, password))

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        prefsManager.saveAuthData(authResponse.token, authResponse.user)
                        navigateToMain()
                    }
                } else {
                    Toast.makeText(this@AuthActivity, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AuthActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            binding.btnAuth.isEnabled = true
        }
    }

    private fun performRegister() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnAuth.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.register(RegisterRequest(username, email, password))

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        prefsManager.saveAuthData(authResponse.token, authResponse.user)
                        navigateToMain()
                    }
                } else {
                    Toast.makeText(this@AuthActivity, "Registration failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AuthActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            binding.btnAuth.isEnabled = true
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
