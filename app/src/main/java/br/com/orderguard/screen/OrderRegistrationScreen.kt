package br.com.orderguard.screen

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import br.com.orderguard.R
import br.com.orderguard.databinding.OrderRegistrationScreenBinding
import br.com.orderguard.model.Order
import br.com.orderguard.model.ServiceDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderRegistrationScreen : AppCompatActivity() {

    private lateinit var binding: OrderRegistrationScreenBinding
    private val db = FirebaseFirestore.getInstance()
    private val serviceDetailsList = mutableListOf<ServiceDetail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = OrderRegistrationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureSpinner()
        applyMasks()
        setupListeners()
    }

    private fun configureSpinner() {
        val statusOptions = listOf("Aberto", "Em andamento", "Concluído", "Cancelado")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.statusSpinner.adapter = spinnerAdapter
    }

    private fun applyMasks() {
        // Mascara de data
        binding.createdAtEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val dateFormat = "##/##/####"
            private val regex = Regex("[^0-9]")

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return
                val unmasked = s?.replace(regex, "") ?: ""
                var formatted = ""
                var index = 0

                for (char in dateFormat) {
                    if (char == '#') {
                        if (index < unmasked.length) {
                            formatted += unmasked[index]
                            index++
                        } else break
                    } else {
                        formatted += char
                    }
                }

                isUpdating = true
                binding.createdAtEditText.setText(formatted)
                binding.createdAtEditText.setSelection(formatted.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Mascara de moeda
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

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish() // Fecha a atividade atual e retorna à anterior
        }
        binding.cadastrarButton.setOnClickListener { cadastrarOrdem() }
        binding.addServiceLayout.setOnClickListener { addServiceDetail() }
    }

    private fun addServiceDetail() {
        // Infla o layout do item de detalhe de serviço
        val serviceDetailView = layoutInflater.inflate(R.layout.item_service_detail, null)

        // Configura as margens para o item
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 16, 0, 16) // Adiciona margem superior e inferior de 16dp
        }
        serviceDetailView.layoutParams = layoutParams

        // Configura o botão de excluir
        val deleteButton = serviceDetailView.findViewById<ImageView>(R.id.btn_delete_service)
        deleteButton.setOnClickListener {
            binding.serviceDetailsContainer.removeView(serviceDetailView)
            val serviceName = serviceDetailView.findViewById<EditText>(R.id.et_service_name).text.toString()
            serviceDetailsList.removeAll { it.serviceName == serviceName }
        }

        // Adiciona o item ao contêiner
        binding.serviceDetailsContainer.addView(serviceDetailView)
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

    private fun cadastrarOrdem() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val order = collectOrderData() ?: return
        val userUID = user.uid

        db.collection("Users").document(userUID).collection("Orders")
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
        val title = binding.titleEditText.text.toString()
        if (title.isEmpty()) {
            Toast.makeText(this, "Preencha o título!", Toast.LENGTH_SHORT).show()
            return null
        }

        val description = binding.descriptionEditText.text.toString()
        val status = binding.statusSpinner.selectedItem.toString()
        val deadline = binding.createdAtEditText.text.toString()
        val totalCost = binding.totalCostEditText.text.toString().replace("[R$,.\\s]".toRegex(), "").toDoubleOrNull()?.div(100)
            ?: 0.0

        val serviceDetails = collectServiceDetails()
        val notes = listOf("Sem observações adicionais")

        return Order(
            title = title,
            description = description,
            status = status,
            deadline = deadline,
            totalCost = totalCost,
            serviceDetails = serviceDetails,
            notes = notes
        )
    }
}
