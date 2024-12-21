package com.example.storyapp.main

import com.example.storyapp.detailstory.DetailStoryActivity
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.data.ListStoryItem
import com.example.storyapp.databinding.ItemStoryBinding
import com.example.storyapp.withDateFormat

class StoryAdapter(private val listReview: List<ListStoryItem>) : RecyclerView.Adapter<StoryAdapter.MyViewHolderStory>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStory {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolderStory(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolderStory, position: Int) {
        val data = listReview[position]
        holder.bind(data)
    }

    override fun getItemCount() = listReview.size

    class MyViewHolderStory(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ListStoryItem) {
            loadImage(data.photoUrl)
            setTextFields(data)
            setItemClickListener(data)
        }

        private fun loadImage(photoUrl: String?) {
            Glide.with(binding.root.context)
                .load(photoUrl)
                .into(binding.imgItemPhoto)
        }

        private fun setTextFields(data: ListStoryItem) {
            binding.tvItemName.text = data.name
            binding.tvItemCreated.text = data.createdAt.withDateFormat()
            binding.tvItemDescription.text = data.description
        }

        private fun setItemClickListener(data: ListStoryItem) {
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailStoryActivity::class.java).apply {
                    putExtra(DetailStoryActivity.NAME, data.name)
                    putExtra(DetailStoryActivity.CREATE_AT, data.createdAt)
                    putExtra(DetailStoryActivity.DESCRIPTION, data.description)
                    putExtra(DetailStoryActivity.PHOTO_URL, data.photoUrl)
                    putExtra(DetailStoryActivity.LAT, data.lat as? Double ?: 0.0)
                    putExtra(DetailStoryActivity.LON, data.lon as? Double ?: 0.0)
                }

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        androidx.core.util.Pair(binding.imgItemPhoto, "photo"),
                        androidx.core.util.Pair(binding.tvItemName, "name"),
                        androidx.core.util.Pair(binding.tvItemCreated, "createdate"),
                        androidx.core.util.Pair(binding.tvItemDescription, "description"),
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }
}