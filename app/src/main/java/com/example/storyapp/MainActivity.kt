package com.example.storyapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.data.UserPreferenceDatastore
import com.example.storyapp.data.dataStore
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.detailstory.DetailStoryActivity
import com.example.storyapp.main.MainViewModel
import com.example.storyapp.main.StoryRepository
import com.example.storyapp.paging.StoryPagingAdapter
import com.example.storyapp.story.AddNewStoryActivity
import com.example.storyapp.welcome.WelcomeActivity
import com.example.storyapp.maps.MapsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(UserPreferenceDatastore.getInstance(dataStore), StoryRepository(ApiConfig.getApiService()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.dashboard_story)

        val adapter = StoryPagingAdapter()
        binding.rvListStory.layoutManager = LinearLayoutManager(this)
        binding.rvListStory.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.rvListStory.adapter = adapter

        mainViewModel.getUser().observe(this) { user ->
            if (user.userId.isEmpty()) {
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                mainViewModel.getStories(user.token).observe(this) { pagingData ->
                    adapter.submitData(lifecycle, pagingData)
                }
            }
        }

        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this, AddNewStoryActivity::class.java)
            startActivity(intent)
        }

        binding.btnMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }



        adapter.setOnItemClickListener { story ->
            val intent = Intent(this, DetailStoryActivity::class.java).apply {
                putExtra(DetailStoryActivity.NAME, story.name)
                putExtra(DetailStoryActivity.CREATE_AT, story.createdAt)
                putExtra(DetailStoryActivity.DESCRIPTION, story.description)
                putExtra(DetailStoryActivity.PHOTO_URL, story.photoUrl)
                putExtra(DetailStoryActivity.LAT, story.lat as? Double ?: 0.0)
                putExtra(DetailStoryActivity.LON, story.lon as? Double ?: 0.0)
            }
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                mainViewModel.logout()
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}