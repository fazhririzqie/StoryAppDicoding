package com.example.storyapp.story

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.main.MainViewModel
import com.example.storyapp.MainActivity
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.data.UserPreferenceDatastore
import com.example.storyapp.databinding.ActivityAddNewStoryBinding
import com.example.storyapp.main.StoryRepository
import com.example.storyapp.reduceFileImage
import com.example.storyapp.rotateBitmap
import com.example.storyapp.uriToFile
import com.example.storyapp.user.signin.SigninViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "User")

class AddNewStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewStoryBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var signViewModel: SigninViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!permissionsGranted()) {
                showToast(getString(R.string.permission))
                finish()
            }
        }
    }

    private fun permissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        initializeViewModels()
        initializeLocationClient()
        checkPermissions()
        setupListeners()
    }

    private fun setupActionBar() {
        supportActionBar?.title = getString(R.string.add_story)
    }

    private fun initializeViewModels() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                UserPreferenceDatastore.getInstance(dataStore),
                StoryRepository(ApiConfig.getApiService())
            )
        )[MainViewModel::class.java]

        signViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                UserPreferenceDatastore.getInstance(dataStore),
                StoryRepository(ApiConfig.getApiService())
            )
        )[SigninViewModel::class.java]
    }

    private fun initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun checkPermissions() {
        if (!permissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun setupListeners() {
        binding.btnAddCamera.setOnClickListener { launchCamera() }
        binding.btnAddGalery.setOnClickListener { openGallery() }
        binding.btnAddLocation.setOnClickListener { fetchUserLocation() }
        binding.buttonAdd.setOnClickListener { submitStory() }
    }

    private fun launchCamera() {
        val intent = Intent(this, CameraNewStoryActivity::class.java)
        cameraLauncher.launch(intent)
    }

    private var selectedFile: File? = null
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val file = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            selectedFile = file
            val bitmap = rotateBitmap(BitmapFactory.decodeFile(file.path), isBackCamera)
            binding.tvAddImg.setImageBitmap(bitmap)
        }
    }

    private fun openGallery() {
        val intent = Intent().apply {
            action = ACTION_GET_CONTENT
            type = "image/*"
        }
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        galleryLauncher.launch(chooser)
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data as Uri
            val file = uriToFile(uri, this)
            selectedFile = file
            binding.tvAddImg.setImageURI(uri)
        }
    }

    private fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_PERMISSIONS)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                userLocation = it
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val address = if (addresses?.isNotEmpty() == true) addresses[0]?.getAddressLine(0) else getString(R.string.address_not_found)
                binding.tvLocationName.text = address
            }
        }
    }

    private fun submitStory() {
        if (selectedFile != null && userLocation != null) {
            if (binding.edAddDescription.text.toString().isNotEmpty()) {
                val file = reduceFileImage(selectedFile as File)
                signViewModel.getUser().observe(this) { user ->
                    mainViewModel.postNewStory(
                        user.token, file, binding.edAddDescription.text.toString(),
                        userLocation!!.latitude, userLocation!!.longitude
                    )
                    mainViewModel.isLoading.observe(this) { showLoading(it) }
                    mainViewModel.isError.observe(this) { isError ->
                        if (isError) {
                            showToast(getString(R.string.upload_failed))
                        } else {
                            navigateToMainActivity()
                        }
                    }
                }
            } else {
                showToast(getString(R.string.description_mandatory))
            }
        } else {
            showToast(getString(R.string.image_mandatory))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarAdd.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}