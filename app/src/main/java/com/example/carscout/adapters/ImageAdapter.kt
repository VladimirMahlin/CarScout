package com.example.carscout.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carscout.R
import com.example.carscout.databinding.ItemImageBinding
import com.squareup.picasso.Picasso

class ImageAdapter(
    private val imageUris: List<Uri>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUris[position])
    }

    override fun getItemCount(): Int = imageUris.size

    inner class ImageViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.deleteButton.setOnClickListener {
                onDeleteClick(adapterPosition)
            }
        }

        fun bind(uri: Uri) {
            Picasso.get()
                .load(uri)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.imageView)
        }
    }
}