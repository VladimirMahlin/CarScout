package com.example.carscout.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carscout.R
import com.example.carscout.databinding.ItemImageBinding
import com.squareup.picasso.Picasso

class ImageAdapter(
    private val imageUris: MutableList<Uri>,
    private var isEditing: Boolean = false,
    private val onImageClick: ((Uri) -> Unit)? = null,
    private val onImageDelete: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    fun getImageUris(): List<Uri> {
        return imageUris
    }

    fun setEditingMode(isEditing: Boolean) {
        this.isEditing = isEditing
        notifyDataSetChanged()
    }

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
            binding.imageView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val uri = imageUris[pos]
                    onImageClick?.invoke(uri)
                }
            }

            binding.deleteButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onImageDelete?.invoke(pos)
                }
            }
        }

        fun bind(uri: Uri) {
            Picasso.get()
                .load(uri)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.imageView)

            binding.deleteButton.visibility = if (isEditing) View.VISIBLE else View.GONE
        }
    }
}
