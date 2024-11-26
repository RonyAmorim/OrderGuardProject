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
import br.com.orderguard.adapter.ClientsAdapter
import br.com.orderguard.databinding.ListClientsFragmentBinding
import br.com.orderguard.model.Address
import br.com.orderguard.model.Client
import br.com.orderguard.screen.client_registration.ClientRegistrationScreen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ClientsFragment : Fragment() {

    private var _binding: ListClientsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var clientsAdapter: ClientsAdapter
    private val clientsList = mutableListOf<Client>()
    private val filteredClientsList = mutableListOf<Client>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListClientsFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()
        setupSearchBar()
        setupAddButton()

        fetchClients()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        clientsAdapter = ClientsAdapter(filteredClientsList)
        binding.rvRecentOrders.layoutManager = LinearLayoutManager(context)
        binding.rvRecentOrders.adapter = clientsAdapter
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterClients(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupAddButton() {
        binding.addButton.setOnClickListener {
            startActivity(Intent(activity, ClientRegistrationScreen::class.java))
        }
    }

    private fun fetchClients() {
        val userUID = FirebaseAuth.getInstance().currentUser?.uid
        if (userUID == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val clientsRef = db.collection("Users").document(userUID).collection("Clients")

        clientsRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    clientsList.clear()
                    for (document in snapshot.documents) {
                        val client = documentToClient(document)
                        clientsList.add(client)
                    }
                    filteredClientsList.clear()
                    filteredClientsList.addAll(clientsList)
                    clientsAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Nenhum cliente encontrado.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao buscar clientes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun documentToClient(document: DocumentSnapshot): Client {
        val id = document.id
        val fullName = document.getString("fullName") ?: ""
        val cpfCnpj = document.getString("cpfCnpj") ?: ""
        val email = document.getString("email") ?: ""
        val phone = document.getString("phone") ?: ""
        val address = document.toObject(Address::class.java) ?: Address()
        val notes = document.get("notes") as? List<String> ?: emptyList()

        val createdAt = when (val value = document.get("createdAt")) {
            is Long -> value
            is String -> value.toLongOrNull() ?: 0L
            is Timestamp -> value.toDate().time
            else -> 0L
        }

        val updatedAt = when (val value = document.get("updatedAt")) {
            is Long -> value
            is String -> value.toLongOrNull() ?: 0L
            is Timestamp -> value.toDate().time
            else -> 0L
        }

        return Client(
            id = id,
            fullName = fullName,
            cpfCnpj = cpfCnpj,
            email = email,
            phone = phone,
            address = address,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun filterClients(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredClientsList.clear()
        filteredClientsList.addAll(clientsList.filter { client ->
            client.fullName.lowercase().contains(lowerCaseQuery) ||
                    client.email.lowercase().contains(lowerCaseQuery) ||
                    client.cpfCnpj.lowercase().contains(lowerCaseQuery) ||
                    (client.id?.lowercase()?.contains(lowerCaseQuery) ?: false)
        })
        clientsAdapter.notifyDataSetChanged()
    }
}
