package com.example.carscout.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.carscout.R
import com.example.carscout.data.model.Car
import com.example.carscout.databinding.ItemCarBinding
import com.squareup.picasso.Picasso

class CarListAdapter(private val onItemClick: (Car) -> Unit) :
    ListAdapter<Car, CarListAdapter.CarViewHolder>(CarDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = getItem(position)
        holder.bind(car)
    }

    inner class CarViewHolder(private val binding: ItemCarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(car: Car) {
            binding.carModelTextView.text = car.model
            binding.carYearTextView.text = car.year.toString()
            binding.carPriceTextView.text = "$${car.price}"

            val mainImageUrl = car.imageUrls.firstOrNull()
            Picasso.get()
                .load(mainImageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.carImageView)

            binding.root.setOnClickListener { onItemClick(car) }
        }
    }

    class CarDiffCallback : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem == newItem
        }
    }
}