package com.example.storyapp.maps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.data.UserPreferenceDatastore
import com.example.storyapp.data.dataStore
import com.example.storyapp.databinding.ActivityMapsBinding
import com.example.storyapp.main.StoryRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val mapsViewModel: MapsViewModel by viewModels {
        ViewModelFactory(
            UserPreferenceDatastore.getInstance(applicationContext.dataStore),
            StoryRepository(ApiConfig.getApiService())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getUserLocationAndUpdateMarkers()
            }
        }

        mapsViewModel.getUser().observe(this) { user ->
            mapsViewModel.getStoriesWithLocation(user.token).observe(this) { response ->
                response.listStory.forEach { story ->
                    val location = LatLng(story.lat as Double, story.lon as Double)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(story.name)
                            .snippet(story.description)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::googleMap.isInitialized) {
            getUserLocationAndUpdateMarkers()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        getUserLocationAndUpdateMarkers()
    }

    private fun getUserLocationAndUpdateMarkers() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
            return
        }
        googleMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                googleMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
            }
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}