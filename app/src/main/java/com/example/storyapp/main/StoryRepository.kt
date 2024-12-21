package com.example.storyapp.main

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.example.storyapp.api.ApiService
import com.example.storyapp.paging.StoryPagingSource

class StoryRepository(private val apiService: ApiService) {

    fun getStories(token: String) = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { StoryPagingSource(apiService, token) }
    ).liveData
}