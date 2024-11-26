package br.com.orderguard.screen.home

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

class HomeFragment : Fragment() {

    private lateinit var rvRecentOrders: RecyclerView
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
        // Configurar o botão de criar nova ordem
        val btnCreateOrder: LinearLayout = view.findViewById(R.id.btnCreateOrder)
        btnCreateOrder.setOnClickListener {
            val intent = Intent(requireContext(), OrderRegistrationScreen::class.java)
            startActivity(intent)
        }

        return view
    }
}
