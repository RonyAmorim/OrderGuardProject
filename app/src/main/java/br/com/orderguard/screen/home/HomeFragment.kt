package br.com.orderguard.screen.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.orderguard.R
import br.com.orderguard.adapter.OrdersAdapter
import br.com.orderguard.databinding.HomeFragmentBinding
import br.com.orderguard.model.Order
import br.com.orderguard.screen.OrderRegistrationScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrdersAdapter
    private val recentOrders = mutableListOf<Order>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla o layout com View Binding
        _binding = HomeFragmentBinding.inflate(inflater, container, false)

        // Configurações iniciais
        setupRecyclerView()
        setupListeners()
        loadOrders()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        adapter = OrdersAdapter(recentOrders)
        binding.rvRecentOrders.adapter = adapter
    }

    private fun setupListeners() {
        // Botão de criar nova ordem
        binding.btnCreateOrder.setOnClickListener {
            val intent = Intent(requireContext(), OrderRegistrationScreen::class.java)
            startActivity(intent)
        }
    }

    private fun loadOrders() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val userUID = user.uid
        db.collection("Users").document(userUID).collection("Clients")
            .get()
            .addOnSuccessListener { clientsSnapshot ->
                val allOrders = mutableListOf<Order>()

                clientsSnapshot.forEach { clientDoc ->
                    val clientName = clientDoc.getString("fullname") ?: "Desconhecido"
                    clientDoc.reference.collection("Orders")
                        .get()
                        .addOnSuccessListener { ordersSnapshot ->
                            ordersSnapshot.forEach { orderDoc ->
                                val order = orderDoc.toObject(Order::class.java).apply {
                                    this.client = clientName
                                }
                                allOrders.add(order)
                            }

                            // Atualizar a UI quando todas as ordens forem carregadas
                            if (clientDoc == clientsSnapshot.last()) {
                                updateUI(allOrders)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Erro ao carregar ordens.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(allOrders: List<Order>) {
        // Filtrar as 3 ordens mais recentes usando createdAt
        val sortedOrders = allOrders.sortedByDescending { it.createdAt }.take(3)
        recentOrders.clear()
        recentOrders.addAll(sortedOrders)
        adapter.notifyDataSetChanged()

        // Atualizar contadores de status
        val completedCount = allOrders.count { it.status == "Concluído" }
        val acceptedCount = allOrders.count { it.status == "Aberto" }
        val pendingCount = allOrders.count { it.status == "Em andamento" }

        binding.tvCompletedOrders.text = completedCount.toString()
        binding.tvAcceptedOrders.text = acceptedCount.toString()
        binding.tvPendingOrders.text = pendingCount.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Libera o binding para evitar memory leaks
    }
}
