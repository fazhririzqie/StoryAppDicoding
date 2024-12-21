package com.example.storyapp.detailstory

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivityDetailStoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding

    companion object {
        const val NAME = "name"
        const val CREATE_AT = "create_at"
        const val DESCRIPTION = "description"
        const val PHOTO_URL = "photoUrl"
        const val LAT = "lat"
        const val LON = "lon"
    }

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.detail_story)

        val photoUrl = intent.getStringExtra(PHOTO_URL)
        val name = intent.getStringExtra(NAME)
        val createAtTimestamp = intent.getStringExtra(CREATE_AT)?.toLongOrNull()
        val description = intent.getStringExtra(DESCRIPTION)
        val lat = intent.getDoubleExtra(LAT, 0.0)
        val lon = intent.getDoubleExtra(LON, 0.0)

        Glide.with(binding.root.context)
            .load(photoUrl)
            .into(binding.ivDetailPhoto)
        binding.tvDetailName.text = name
        binding.tvDetailCreatedTime.text = createAtTimestamp?.let { formatDate(it) }
        binding.tvDetailDescription.text = description

        // Get address from lat and lon
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        val address = if (addresses?.isNotEmpty() == true) {
            addresses[0]?.getAddressLine(0)
        } else {
            getString(R.string.address_not_found)
        }
        binding.tvDetailLocation.text = address
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return format.format(date)
    }
}