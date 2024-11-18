package br.com.orderguard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.orderguard.model.Order

class RecentOrdersAdapter(
    private val context: Context,
    private val orders: List<Order>
) : RecyclerView.Adapter<RecentOrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvOrderId.text = "Ordem #" + order.id
        holder.tvOrderName.text = order.name
        holder.tvOrderStatus.text = order.status
        holder.tvOrderDate.text = order.date
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        val tvOrderName: TextView = itemView.findViewById(R.id.tv_order_name)  // Corrigido para tv_order_name
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
    }
}
