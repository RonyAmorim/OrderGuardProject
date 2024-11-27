package br.com.orderguard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.orderguard.R
import br.com.orderguard.model.Order

class OrdersAdapter(private var ordersList: List<Order>) :
    RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tv_order_id)
        val tvOrderName: TextView = view.findViewById(R.id.tv_order_name)
        val tvOrderStatus: TextView = view.findViewById(R.id.tv_order_status)
        val tvOrderDate: TextView = view.findViewById(R.id.tv_order_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = ordersList[position]
        holder.tvOrderId.text = order.title
        holder.tvOrderName.text = "Cliente: ${order.client}"
        holder.tvOrderStatus.text = "Status: ${order.status}"
        holder.tvOrderDate.text = "Prazo: ${order.deadline}"
    }

    override fun getItemCount(): Int = ordersList.size

    fun updateList(newList: List<Order>) {
        ordersList = newList
        notifyDataSetChanged()
    }
}