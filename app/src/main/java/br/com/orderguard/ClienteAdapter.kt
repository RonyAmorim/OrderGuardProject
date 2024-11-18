package br.com.orderguard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.orderguard.model.Client

class ClienteAdapter(
    private val context: Context,
    private val clientes: List<Client>
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_client, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.tvClientId.text = "Cliente #" + cliente.id
        holder.tvClientName.text = "Nome: " + cliente.nome
        holder.tvClientEmail.text = "Email: " + cliente.email
        holder.tvClientPhone.text = "Telefone: " + cliente.telefone
    }

    override fun getItemCount(): Int {
        return clientes.size
    }

    class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvClientId: TextView = itemView.findViewById(R.id.tv_client_id)
        val tvClientName: TextView = itemView.findViewById(R.id.tv_client_name)
        val tvClientEmail: TextView = itemView.findViewById(R.id.tv_client_email)
        val tvClientPhone: TextView = itemView.findViewById(R.id.tv_client_phone)
    }
}
