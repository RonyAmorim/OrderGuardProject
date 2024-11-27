package br.com.orderguard.screen

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import br.com.orderguard.R
import br.com.orderguard.databinding.OrderRegistrationScreenBinding
import br.com.orderguard.model.Order
import br.com.orderguard.model.ServiceDetail
import br.com.orderguard.screen.client_registration.ClientRegistrationScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class OrderRegistrationScreen : AppCompatActivity() {

    private lateinit var binding: OrderRegistrationScreenBinding
    private val db = FirebaseFirestore.getInstance()
    private val serviceDetailsList = mutableListOf<ServiceDetail>()
    private val commentsList = mutableListOf<String>()
    private val clientIds = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OrderRegistrationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClientDropdown()
        setupStatusDropdown()
        applyMasks()
        setupListeners()
        loadClients()
    }

    private fun setupClientDropdown() {
        binding.clientDropdown.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.clientDropdown)
            clientIds.keys.forEach { clientName ->
                popupMenu.menu.add(clientName)
            }
            popupMenu.setOnMenuItemClickListener { menuItem ->
                binding.clientText.text = menuItem.title
                true
            }
            popupMenu.show()
        }
    }

    private fun setupStatusDropdown() {
        val statusOptions = listOf("Aberto", "Em andamento", "Concluído", "Cancelado")
        binding.statusDropdown.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.statusDropdown)
            statusOptions.forEach { status ->
                popupMenu.menu.add(status)
            }
            popupMenu.setOnMenuItemClickListener { menuItem ->
                binding.statusText.text = menuItem.title
                true
            }
            popupMenu.show()
        }
    }

    private fun applyMasks() {
        binding.deadlineEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val dateFormat = "##/##/####"
            private val regex = Regex("[^0-9]")

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val currentText = s?.toString() ?: ""
                val unmasked = currentText.replace(regex, "")

                val formatted = StringBuilder()
                var index = 0
                for (char in dateFormat) {
                    if (char == '#') {
                        if (index < unmasked.length) {
                            formatted.append(unmasked[index])
                            index++
                        } else break
                    } else {
                        if (index < unmasked.length || formatted.isNotEmpty()) {
                            formatted.append(char)
                        }
                    }
                }

                if (currentText != formatted.toString()) {
                    isUpdating = true
                    binding.deadlineEditText.setText(formatted.toString())
                    binding.deadlineEditText.setSelection(formatted.length)
                    isUpdating = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.totalCostEditText.addTextChangedListener(object : TextWatcher {
            private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.totalCostEditText.removeTextChangedListener(this)

                val cleanString = s.toString().replace("[R$,.\\s]".toRegex(), "")
                val parsed = cleanString.toDoubleOrNull() ?: 0.0
                val formatted = currencyFormat.format(parsed / 100)

                binding.totalCostEditText.setText(formatted)
                binding.totalCostEditText.setSelection(formatted.length)

                binding.totalCostEditText.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadClients() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val userUID = user.uid
        db.collection("Users").document(userUID).collection("Clients")
            .get()
            .addOnSuccessListener { result ->
                result.forEach { document ->
                    val clientName = document.getString("fullName") ?: "Cliente Desconhecido"
                    val clientId = document.id
                    clientIds[clientName] = clientId
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.addClientButton.setOnClickListener {
            startActivity(Intent(this, ClientRegistrationScreen::class.java))
        }
        binding.cadastrarButton.setOnClickListener { cadastrarOrdem() }
        binding.addServiceLayout.setOnClickListener { addServiceDetail() }
        binding.addCommentButton.setOnClickListener { addComment() }
    }

    private fun addComment() {
        val commentText = binding.commentEditText.text.toString()
        if (commentText.isNotBlank()) {
            // Obtém data e hora atuais
            val currentDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            // Cria o layout do comentário
            val commentView = layoutInflater.inflate(R.layout.item_comment, null)

            // Define o texto do comentário
            val textView = commentView.findViewById<TextView>(R.id.tv_comment)
            textView.text = commentText

            // Define a data e hora
            val dateTimeView = commentView.findViewById<TextView>(R.id.tv_comment_time)
            dateTimeView.text = currentDateTime

            // Configura o botão de deletar
            val deleteButton = commentView.findViewById<ImageView>(R.id.btn_delete_comment)
            deleteButton.setOnClickListener {
                binding.commentsContainer.removeView(commentView)
                commentsList.remove("$commentText - $currentDateTime")
            }

            // Configura margem entre os itens
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16) // Gap de 16dp entre os comentários
            commentView.layoutParams = layoutParams

            // Adiciona o comentário à lista na UI
            binding.commentsContainer.addView(commentView)
            binding.commentEditText.text.clear()

            // Salva o comentário com a data e hora na lista de comentários
            commentsList.add("$commentText - $currentDateTime")
        } else {
            Toast.makeText(this, "Comentário vazio!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun collectServiceDetails(): List<ServiceDetail> {
        val details = mutableListOf<ServiceDetail>()
        for (i in 0 until binding.serviceDetailsContainer.childCount) {
            val serviceView = binding.serviceDetailsContainer.getChildAt(i)
            val serviceName = serviceView.findViewById<EditText>(R.id.et_service_name).text.toString()
            val cost = serviceView.findViewById<EditText>(R.id.et_service_cost).text.toString().toDoubleOrNull() ?: 0.0
            val quantity = serviceView.findViewById<EditText>(R.id.et_service_quantity).text.toString().toIntOrNull() ?: 0
            if (serviceName.isNotBlank()) {
                details.add(ServiceDetail(serviceName, cost, quantity))
            }
        }
        return details
    }

    private fun addServiceDetail() {
        val serviceDetailView = layoutInflater.inflate(R.layout.item_service_detail, null)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 16, 0, 16)
        }
        serviceDetailView.layoutParams = layoutParams
        val deleteButton = serviceDetailView.findViewById<ImageView>(R.id.btn_delete_service)
        deleteButton.setOnClickListener {
            binding.serviceDetailsContainer.removeView(serviceDetailView)
            val serviceName = serviceDetailView.findViewById<EditText>(R.id.et_service_name).text.toString()
            serviceDetailsList.removeAll { it.serviceName == serviceName }
        }
        binding.serviceDetailsContainer.addView(serviceDetailView)
    }

    private fun cadastrarOrdem() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val clientName = binding.clientText.text.toString()
        if (clientName == "Selecione um cliente") {
            Toast.makeText(this, "Selecione um cliente!", Toast.LENGTH_SHORT).show()
            return
        }

        val clientId = clientIds[clientName] ?: return

        val order = collectOrderData() ?: return
        val userUID = user.uid

        db.collection("Users").document(userUID).collection("Clients").document(clientId)
            .collection("Orders")
            .add(order)
            .addOnSuccessListener {
                Toast.makeText(this, "Ordem cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar ordem: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun collectOrderData(): Order? {
        val clientName = binding.clientText.text.toString()
        val title = binding.titleEditText.text.toString()
        if (title.isEmpty()) {
            Toast.makeText(this, "Preencha o título!", Toast.LENGTH_SHORT).show()
            return null
        }

        val description = binding.descriptionEditText.text.toString()
        val status = binding.statusText.text.toString()
        val deadline = binding.deadlineEditText.text.toString()
        val totalCost = binding.totalCostEditText.text.toString().replace("[R$,.\\s]".toRegex(), "").toDoubleOrNull()?.div(100)
            ?: 0.0
        val serviceDetails = collectServiceDetails()

        return Order(
            client = clientName,
            title = title,
            description = description,
            status = status,
            createdAt = System.currentTimeMillis(),
            deadline = deadline,
            totalCost = totalCost,
            serviceDetails = serviceDetails,
            notes = commentsList
        )
    }
}
