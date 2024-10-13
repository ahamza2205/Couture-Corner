package com.example.couturecorner.order.ui

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.couturecorner.R
import com.example.couturecorner.databinding.OrderItemBinding
import com.graphql.GetOrdersByCustomerQuery
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class OrderDiffUtill:DiffUtil.ItemCallback<GetOrdersByCustomerQuery.Edge>() {
    override fun areItemsTheSame(
        oldItem: GetOrdersByCustomerQuery.Edge,
        newItem: GetOrdersByCustomerQuery.Edge
    ): Boolean {
        return oldItem.node?.name==newItem.node?.name
    }

    override fun areContentsTheSame(
        oldItem: GetOrdersByCustomerQuery.Edge,
        newItem: GetOrdersByCustomerQuery.Edge
    ): Boolean {
       return oldItem==newItem
    }

}

class OrderAdapter(
    val myListenner: (String?) -> Unit,
    val listennerForCurenncy:() -> String
): ListAdapter<GetOrdersByCustomerQuery.Edge, OrderAdapter.OrderViewHolder>(OrderDiffUtill()) {
    lateinit var binding: OrderItemBinding
   class OrderViewHolder(var binding: OrderItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater : LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding= OrderItemBinding.inflate(inflater, parent, false)
        return OrderViewHolder(binding)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position).node

       holder.binding.orderIdTextView.text=order?.name
        holder.binding.dateTextView.text=convertUtcToLocal(order?.createdAt)
        holder.binding.totalPriceTextView.text=holder.itemView.context.getString(
            R.string.price,
            order?.totalPriceSet?.shopMoney?.amount,
            listennerForCurenncy.invoke())

        holder.binding.seeDetailsButton.setOnClickListener {
            myListenner.invoke(order?.id)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertUtcToLocal(utcTime: String?): String? {
        // Define the format of the input UTC time
        val utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"))

        // Parse the UTC time into a ZonedDateTime object
        val utcDateTime = ZonedDateTime.parse(utcTime, utcFormatter)

        // Convert to the system's default time zone (local time)
        val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault())

        // Define a formatter for the output in your desired format
        val localFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Format the local time and return as a string
        return localDateTime.format(localFormatter)
    }


}