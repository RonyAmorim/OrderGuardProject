package br.com.orderguard.screen.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.orderguard.R
import br.com.orderguard.adapter.OrdersAdapter
import br.com.orderguard.databinding.ListOrdersFragmentBinding
import br.com.orderguard.model.Order
import br.com.orderguard.screen.OrderRegistrationScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrdersFragment : Fragment() {

    private var _binding: ListOrdersFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrdersAdapter
    private val ordersList = mutableListOf<Order>() // Complete list of orders
    private val filteredOrdersList = mutableListOf<Order>() // Filtered list for display
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListOrdersFragmentBinding.inflate(inflater, container, false)

        setupRecyclerView()
        loadOrders()
        setupSearchBar()
        setupAddButton()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        adapter = OrdersAdapter(filteredOrdersList) // Use the filtered list for the adapter
        binding.rvRecentOrders.adapter = adapter
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterOrders(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupAddButton() {
        binding.addButton.setOnClickListener {
            val intent = Intent(activity, OrderRegistrationScreen::class.java)
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
                clientsSnapshot.forEach { clientDoc ->
                    val clientName = clientDoc.getString("fullName") ?: "Desconhecido"
                    loadClientOrders(clientDoc.id, clientName)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadClientOrders(clientId: String, clientName: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) return

        val userUID = user.uid
        db.collection("Users").document(userUID).collection("Clients").document(clientId)
            .collection("Orders")
            .get()
            .addOnSuccessListener { ordersSnapshot ->
                ordersSnapshot.forEach { orderDoc ->
                    val order = orderDoc.toObject(Order::class.java).apply {
                        client = clientName // Associate client name with order
                    }
                    ordersList.add(order)
                }
                updateFilteredOrders()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao carregar ordens do cliente.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFilteredOrders() {
        // Show all orders initially
        filteredOrdersList.clear()
        filteredOrdersList.addAll(ordersList)
        adapter.notifyDataSetChanged()
    }

    private fun filterOrders(query: String) {
        filteredOrdersList.clear()
        if (query.isBlank()) {
            filteredOrdersList.addAll(ordersList) // Show all orders if the query is empty
        } else {
            filteredOrdersList.addAll(ordersList.filter {
                it.title.contains(query, ignoreCase = true) || it.client.contains(query, ignoreCase = true)
            })
        }
        adapter.notifyDataSetChanged() // Notify the adapter about changes
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
