// MainViewModel.kt
package com.example.storyapp.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.data.AddStoryResponse
import com.example.storyapp.data.SignInResult
import com.example.storyapp.data.UserPreferenceDatastore
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainViewModel(private val pref: UserPreferenceDatastore, private val repository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    fun postNewStory(token: String, file: File, description: String, lat: Double, lon: Double) {
        _isLoading.value = true
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo", file.name, requestImageFile
        )
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())
        val latRequestBody = lat.toString().toRequestBody("text/plain".toMediaType())
        val lonRequestBody = lon.toString().toRequestBody("text/plain".toMediaType())

        val client = ApiConfig.getApiService().postStory("Bearer $token", imageMultipart, descriptionRequestBody, latRequestBody, lonRequestBody)
        client.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(call: Call<AddStoryResponse>, response: Response<AddStoryResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _isError.value = false
                } else {
                    _isError.value = true
                }
            }

            override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                _isLoading.value = false
                _isError.value = true
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    fun logout() {
        viewModelScope.launch {
            pref.signout()
        }
    }

    fun getUser(): LiveData<SignInResult> {
        return pref.getUser().asLiveData()
    }

    fun getStories(token: String) = repository.getStories(token).cachedIn(viewModelScope)

    companion object {
        private const val TAG = "MainViewModel"
    }
}