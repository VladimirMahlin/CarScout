package com.example.carscout.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.carscout.R
import com.example.carscout.data.model.Car
import com.example.carscout.databinding.ItemCarBinding
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

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
            binding.carManufacturerModelTextView.text = "${car.manufacturer} ${car.model}"
            binding.carYearTextView.text = car.year.toString()
            binding.carMileageTextView.text = formatMileage(car.mileage)
            binding.carPriceTextView.text = formatPrice(car.price)

            val mainImageUrl = car.imageUrls.firstOrNull()
            Picasso.get()
                .load(mainImageUrl)
                .placeholder(R.drawable.car_placeholder)
                .error(R.drawable.error)
                .into(binding.carImageView)

            binding.root.setOnClickListener { onItemClick(car) }
        }

        private fun formatMileage(mileage: Int): String {
            return "${NumberFormat.getNumberInstance(Locale.US).format(mileage)} miles"
        }

        private fun formatPrice(price: Double): String {
            return NumberFormat.getCurrencyInstance(Locale.US).format(price)
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