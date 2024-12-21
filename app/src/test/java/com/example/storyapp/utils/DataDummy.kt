package com.example.storyapp.utils

import com.example.storyapp.data.ListStoryItem

object DataDummy {
    fun generateDummyStories(): List<ListStoryItem> {
        val stories = ArrayList<ListStoryItem>()
        for (i in 0..10) {
            val story = ListStoryItem(
                id = "id_$i",
                name = "name_$i",
                description = "description_$i",
                photoUrl = "photoUrl_$i",
                createdAt = "createdAt_$i",
                lat = 0.0,
                lon = 0.0
            )
            stories.add(story)
        }
        return stories
    }
}