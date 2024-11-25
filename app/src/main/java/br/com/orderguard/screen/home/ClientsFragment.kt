package br.com.orderguard.screen.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.orderguard.R
import br.com.orderguard.adapter.ClientsAdapter
import br.com.orderguard.model.Client
import br.com.orderguard.screen.client_registration.ClientRegistrationScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var clientsAdapter: ClientsAdapter
    private val clientsList = mutableListOf<Client>()
    private val filteredClientsList = mutableListOf<Client>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.list_clients_fragment, container, false)

        // Configura o RecyclerView
        recyclerView = view.findViewById(R.id.rv_recent_orders)
        recyclerView.layoutManager = LinearLayoutManager(context)
        clientsAdapter = ClientsAdapter(filteredClientsList)
        recyclerView.adapter = clientsAdapter

        // Carrega os clientes
        fetchClients()

        // Configura o botão para adicionar um cliente
        val addButton: View = view.findViewById(R.id.addButton)
        addButton.setOnClickListener {
            startActivity(Intent(activity, ClientRegistrationScreen::class.java))
        }

        // Configura a barra de pesquisa
        val searchBar: EditText = view.findViewById(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterClients(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
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
                        val client = document.toObject(Client::class.java)
                        client?.let {
                            it.id = document.id // Define o ID gerado pelo Firebase
                            clientsList.add(it)
                        }
                    }
                    filterClients("") // Exibe todos os clientes inicialmente
                } else {
                    Toast.makeText(requireContext(), "Nenhum cliente encontrado.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao buscar clientes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterClients(query: String) {
        filteredClientsList.clear()
        if (query.isEmpty()) {
            filteredClientsList.addAll(clientsList)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (client in clientsList) {
                if (client.fullName.lowercase().contains(lowerCaseQuery) ||
                    client.cpfCnpj.lowercase().contains(lowerCaseQuery) ||
                    client.email.lowercase().contains(lowerCaseQuery)
                ) {
                    filteredClientsList.add(client)
                }
            }
        }
        clientsAdapter.notifyDataSetChanged()
    }
}
