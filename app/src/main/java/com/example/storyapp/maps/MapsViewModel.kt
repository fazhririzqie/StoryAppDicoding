package com.example.storyapp.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.data.UserPreferenceDatastore
import com.example.storyapp.main.StoryRepository
import kotlinx.coroutines.Dispatchers

class MapsViewModel(private val pref: UserPreferenceDatastore, private val repository: StoryRepository) : ViewModel() {
    private val apiService = ApiConfig.getApiService()

    fun getStoriesWithLocation(token: String) = liveData(Dispatchers.IO) {
        val response = apiService.getStoriesWithLocation("Bearer $token")
        emit(response)
    }

    fun getUser() = pref.getUser().asLiveData()

    fun getStories(token: String) = repository.getStories(token).cachedIn(viewModelScope)
}