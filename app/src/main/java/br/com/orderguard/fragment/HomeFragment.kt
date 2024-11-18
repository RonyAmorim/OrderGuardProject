package br.com.orderguard.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.orderguard.screen.OrderRegistrationScreen
import br.com.orderguard.model.Order
import br.com.orderguard.R
import br.com.orderguard.RecentOrdersAdapter

class HomeFragment : Fragment() {

    private lateinit var rvRecentOrders: RecyclerView
    private lateinit var adapter: RecentOrdersAdapter
    private lateinit var recentOrders: MutableList<Order>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar o layout para este fragment
        val view = inflater.inflate(R.layout.home_fragment, container, false)

        // Inicializar RecyclerView
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders)
        rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())

        // Simular dados de ordens recentes
        recentOrders = mutableListOf(
            Order("1234", "Pedido de Equipamento", "Em Progresso", "01/01/2024"),
            Order("1235", "Serviço de Desenvolvimento", "Concluída", "02/01/2024"),
            Order("1236", "Consultoria Técnica", "Cancelada", "03/01/2024")
        )

        // Configurar o adapter
        adapter = RecentOrdersAdapter(requireContext(), recentOrders)
        rvRecentOrders.adapter = adapter

        // Configurar o botão de criar nova ordem
        val btnCreateOrder: LinearLayout = view.findViewById(R.id.btnCreateOrder)
        btnCreateOrder.setOnClickListener {
            val intent = Intent(requireContext(), OrderRegistrationScreen::class.java)
            startActivity(intent)
        }

        return view
    }
}
