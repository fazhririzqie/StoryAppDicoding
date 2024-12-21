package com.example.storyapp.user.signin


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
import com.example.storyapp.MainActivity
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.data.UserPreferenceDatastore
import com.example.storyapp.data.dataStore
import com.example.storyapp.databinding.ActivitySigninBinding
import com.example.storyapp.main.StoryRepository

class SigninActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySigninBinding
    private lateinit var signinViewModel: SigninViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupVieModel()
        setupAction()
    }

    private fun setupAction() {
        binding.signinButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()
            when {
                email.isEmpty() -> {
                    binding.edLoginEmail.error = getString(R.string.input_name)
                }
                password.isEmpty() -> {
                    binding.edLoginPassword.error = getString(R.string.input_password)
                }
                password.length < 6 -> {
                    binding.edLoginPassword.error = getString(R.string.label_validation_password)
                }

                else -> {
                    signinViewModel.signin(email, password)
                }
            }
        }
    }

    private fun setupVieModel() {
        signinViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                UserPreferenceDatastore.getInstance(dataStore),
                StoryRepository(ApiConfig.getApiService())
            )
        )[SigninViewModel::class.java]

        signinViewModel.let { vmsignin ->
            vmsignin.signinResult.observe(this) { signin ->
                // success signin process triggered -> save preferences
                vmsignin.saveUser(
                    signin.loginResult.name,
                    signin.loginResult.userId,
                    signin.loginResult.token
                )

            }
            vmsignin.message.observe(this) { message ->
                if (message == "200") {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.info)
                    builder.setMessage(R.string.validate_login_success)
                    builder.setIcon(R.drawable.ic_baseline_check_green_24)
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        alertDialog.dismiss()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }, 2000)
                }
            }
            vmsignin.error.observe(this) { error ->
                if (error == "400") {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.info)
                    builder.setMessage(R.string.label_invalid_email)
                    builder.setIcon(R.drawable.ic_baseline_close_red_24)
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        alertDialog.dismiss()
                    }, 2000)
                }
                if (error == "401") {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.info)
                    builder.setMessage(R.string.login_user_not_found)
                    builder.setIcon(R.drawable.ic_baseline_close_red_24)
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        alertDialog.dismiss()
                    }, 2000)
                }
            }
            vmsignin.isLoading.observe(this) {
                showLoading(it)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBarLogin.visibility = View.VISIBLE
        } else {
            binding.progressBarLogin.visibility = View.GONE
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