package com.example.carscout.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.carscout.R
import com.example.carscout.data.model.Dealership
import com.example.carscout.databinding.ItemDealershipBinding
import com.squareup.picasso.Picasso

class DealershipListAdapter(private val onItemClick: (Dealership) -> Unit) :
    ListAdapter<Dealership, DealershipListAdapter.DealershipViewHolder>(DealershipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealershipViewHolder {
        val binding = ItemDealershipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DealershipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DealershipViewHolder, position: Int) {
        val dealership = getItem(position)
        holder.bind(dealership)
    }

    inner class DealershipViewHolder(private val binding: ItemDealershipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dealership: Dealership) {
            binding.dealershipNameTextView.text = dealership.name
            binding.dealershipAddressTextView.text = dealership.address
            binding.dealershipContactTextView.text = dealership.phoneNumber

            val mainImageUrl = dealership.imageUrls.firstOrNull()
            Picasso.get()
                .load(mainImageUrl)
                .placeholder(R.drawable.dealership_placeholder)
                .error(R.drawable.error)
                .into(binding.dealershipImageView)

            binding.root.setOnClickListener { onItemClick(dealership) }
        }
    }

    class DealershipDiffCallback : DiffUtil.ItemCallback<Dealership>() {
        override fun areItemsTheSame(oldItem: Dealership, newItem: Dealership): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Dealership, newItem: Dealership): Boolean {
            return oldItem == newItem
        }
    }
}
