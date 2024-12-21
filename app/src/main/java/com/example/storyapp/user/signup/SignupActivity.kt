package com.example.storyapp.user.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.data.UserPreferenceDatastore
import com.example.storyapp.data.dataStore
import com.example.storyapp.databinding.ActivitySignupBinding
import com.example.storyapp.main.StoryRepository
import com.example.storyapp.user.signin.SigninActivity

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var signupViewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()
            when {
                name.isEmpty() -> {
                    binding.edRegisterName.error = getString(R.string.input_name)
                }
                email.isEmpty() -> {
                    binding.edRegisterEmail.error = getString(R.string.input_email)
                }
                password.isEmpty() -> {
                    binding.edRegisterPassword.error = getString(R.string.input_password)
                }
                password.length < 6 -> {
                    binding.edRegisterPassword.error = getString(R.string.label_validation_password)
                }
                else -> {
                    signupViewModel.signup(name, email, password)
                }
            }
        }
    }

    private fun setupViewModel() {
        signupViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                UserPreferenceDatastore.getInstance(dataStore),
                StoryRepository(ApiConfig.getApiService())
            )
        )[SignupViewModel::class.java]

        signupViewModel?.let { signupvm ->
            signupvm.message.observe(this) { message ->
                if (message == "201") {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.info)
                    builder.setMessage(R.string.validate_register_success)
                    builder.setIcon(R.drawable.ic_baseline_check_green_24)
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        alertDialog.dismiss()
                        val intent = Intent(this, SigninActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 2000)
                }
            }
            signupvm.error.observe(this) { error ->
                if (error == "400") {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.info)
                    builder.setMessage(R.string.validate_register_failed)
                    builder.setIcon(R.drawable.ic_baseline_close_red_24)
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        alertDialog.dismiss()
                    }, 2000)
                }
            }
            signupvm.isLoading.observe(this) {
                showLoading(it)
            }

        }
    }

    private fun showLoading(isLoading: Boolean) {

        if (isLoading) {
            binding.progressBarRegister.visibility = View.VISIBLE
        } else {
            binding.progressBarRegister.visibility = View.GONE
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

}