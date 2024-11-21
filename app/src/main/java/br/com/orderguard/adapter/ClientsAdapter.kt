package br.com.orderguard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.orderguard.R
import br.com.orderguard.model.Client

class ClientsAdapter(private val clients: List<Client>) :
    RecyclerView.Adapter<ClientsAdapter.ClientViewHolder>() {

    class ClientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClientId: TextView = view.findViewById(R.id.tv_client_id)
        val tvClientName: TextView = view.findViewById(R.id.tv_client_name)
        val tvClientEmail: TextView = view.findViewById(R.id.tv_client_email)
        val tvClientPhone: TextView = view.findViewById(R.id.tv_client_phone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client, parent, false)
        return ClientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = clients[position]
        holder.tvClientId.text = "Cliente #${client.id}"
        holder.tvClientName.text = "Nome: ${client.fullName}"
        holder.tvClientEmail.text = "Email: ${client.email}"
        holder.tvClientPhone.text = "Telefone: ${client.phone}"
    }

    override fun getItemCount(): Int {
        return clients.size
    }
}