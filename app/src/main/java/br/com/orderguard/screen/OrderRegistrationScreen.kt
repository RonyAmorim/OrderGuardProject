package br.com.orderguard.screen

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import br.com.orderguard.R
import br.com.orderguard.databinding.OrderRegistrationScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class OrderRegistrationScreen : AppCompatActivity() {

    // View Binding
    private lateinit var binding: OrderRegistrationScreenBinding

    // Firebase
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializando o binding
        binding = OrderRegistrationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar o Spinner de status
        val statusOptions = listOf("Aberto", "Em andamento", "Concluído", "Cancelado")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.statusOrdemSpinner.adapter = spinnerAdapter

        // Listener para buscar cliente ao perder foco
        binding.nomeClienteEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                buscarClientePorNome(binding.nomeClienteEditText.text.toString())
            }
        }

        // Listener do botão cadastrar
        binding.cadastrarButton.setOnClickListener {
            cadastrarOrdem()
        }
    }

    /**
     * Busca um cliente no Firestore pelo nome e preenche os campos se encontrado.
     */
    private fun buscarClientePorNome(nomeCliente: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado! Faça login novamente.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userUID = user.uid
        val clientesRef = db.collection("Users").document(userUID).collection("Clients")
        clientesRef.whereEqualTo("fullName", nomeCliente).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val cliente = snapshot.documents[0]
                    binding.contatoClienteEditText.setText(cliente.getString("phone") ?: "Não informado")
                    Toast.makeText(this, "Cliente encontrado: ${cliente.getString("fullName")}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar cliente: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Cadastra uma nova ordem no Firestore associada ao cliente.
     */
    private fun cadastrarOrdem() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val userUID = user.uid
        val nomeCliente = binding.nomeClienteEditText.text.toString()
        if (nomeCliente.isEmpty()) {
            Toast.makeText(this, "Digite o nome do cliente!", Toast.LENGTH_SHORT).show()
            return
        }

        val clientesRef = db.collection("Users").document(userUID).collection("Clients")
        clientesRef.whereEqualTo("fullName", nomeCliente).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val clienteId = snapshot.documents[0].id
                    salvarOrdem(userUID, clienteId)
                } else {
                    Toast.makeText(this, "Cliente não encontrado para associar a ordem!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar cliente: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Salva a ordem no Firestore associada ao cliente.
     */
    private fun salvarOrdem(userUID: String, clienteId: String) {
        val ordensRef = db.collection("Users").document(userUID).collection("Clients").document(clienteId).collection("Orders")

        // Verificar o número atual de ordens para gerar o próximo ID
        ordensRef.get()
            .addOnSuccessListener { snapshot ->
                // Calcula o próximo número de ID com base na quantidade atual
                val nextId = String.format("%04d", snapshot.size() + 1)

                val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                // Dados da ordem
                val ordemData = mapOf(
                    "id" to nextId, // ID sequencial
                    "categoria" to binding.categoriaEditText.text.toString(),
                    "preco" to binding.precoEditText.text.toString().toDoubleOrNull(),
                    "descricao" to binding.descreveEditText.text.toString(),
                    "status" to binding.statusOrdemSpinner.selectedItem.toString(),
                    "dataCriacao" to dataAtual
                )

                // Salvar a nova ordem com o próximo ID
                ordensRef.document(nextId).set(ordemData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Ordem cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao salvar ordem: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao acessar as ordens: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
