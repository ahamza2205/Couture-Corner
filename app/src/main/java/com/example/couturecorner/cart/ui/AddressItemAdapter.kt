package com.example.couturecorner.cart.ui
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.couturecorner.R
import com.example.couturecorner.data.model.Address
import com.example.couturecorner.databinding.AddressItemBinding

class AddressItemAdapter(
    private val onAddressClickListener: OnAddressClickListener
) : RecyclerView.Adapter<AddressItemAdapter.AddressItemViewHolder>() {

    private val addressItems = mutableListOf<Address>()
    private var selectedPosition = RecyclerView.NO_POSITION

    inner class AddressItemViewHolder(val binding: AddressItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressItemViewHolder {
        val binding = AddressItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AddressItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return addressItems.size
    }

    override fun onBindViewHolder(holder: AddressItemViewHolder, position: Int) {
        val address = addressItems[position]

        with(holder.binding) {
            addressTitle.text = address.name
            addressDescription.text = address.addressDetails
            if (position == selectedPosition) {
                root.setBackgroundResource(R.drawable.selected_address_background) // Highlighted state
            } else {
                root.setBackgroundResource(R.drawable.default_address_background) // Default state
            }
            // Set click listener for selecting an address
           addressItem.setOnClickListener {
               val previousPosition = selectedPosition
               selectedPosition = position
               notifyItemChanged(previousPosition) // Refresh previously selected item
               notifyItemChanged(selectedPosition) // Refresh newly selected item
               onAddressClickListener.onAddressClick(address)

            }
        }
    }

    fun setAddressItems(addressList: List<Address>) {
        addressItems.clear()
        addressItems.addAll(addressList)
        notifyDataSetChanged()
    }
}

interface OnAddressClickListener {
    fun onAddressClick(address: Address)
}
