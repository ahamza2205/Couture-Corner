package com.example.couturecorner.setting.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.couturecorner.data.model.Address
import com.example.couturecorner.databinding.AddtessitemBinding

class AddressAdapter(
    private val deleteListener: OnAddressDeleteListener
) : RecyclerView.Adapter<AddressAdapter.AddressItemViewHolder>() {

    // Use a mutable list to hold address items
    private val addressItems = mutableListOf<Address>()

    inner class AddressItemViewHolder(private val binding: AddtessitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(addressItem: Address) {
            binding.title.text = addressItem.name
            binding.details.text = addressItem.addressDetails
            binding.phone.text = addressItem.phone

            // Set click listener for delete action using the interface
            binding.delete.setOnClickListener {
                deleteListener.onDeleteAddress(addressItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressItemViewHolder {
        val binding = AddtessitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return addressItems.size
    }

    override fun onBindViewHolder(holder: AddressItemViewHolder, position: Int) {
        holder.bind(addressItems[position])
    }

    // Function to set a new list of addresses
    fun setAddressList(newAddressItems: List<Address>) {
        addressItems.clear() // Clear the existing list
        addressItems.addAll(newAddressItems) // Add all new items
        notifyDataSetChanged() // Notify the adapter of the data change
    }
}

interface OnAddressDeleteListener {
    fun onDeleteAddress(address: Address)
}