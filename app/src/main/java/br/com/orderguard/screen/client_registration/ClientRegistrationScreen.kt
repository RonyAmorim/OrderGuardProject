package br.com.orderguard.screen.client_registration

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import br.com.orderguard.R
import br.com.orderguard.databinding.ClientRegistrationScreenBinding
import br.com.orderguard.model.Client
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.text.SimpleDateFormat
import java.util.Locale

class ClientRegistrationScreen : AppCompatActivity() {

    private lateinit var binding: ClientRegistrationScreenBinding
    private var currentFragmentIndex = 0

    private val clientData = Client()

    private val fragments = listOf(
        PersonalDataFragment(clientData),
        AddressFragment(clientData),
        NotesFragment(clientData)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ClientRegistrationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(fragments[currentFragmentIndex])

        binding.submitButton.setOnClickListener {
            if (validateCurrentFragment()) {
                navigateNext()
            }
        }

        binding.btnBack.setOnClickListener {
            handleBackPress()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
        updateButtonState()
    }

    private fun navigateNext() {
        if (currentFragmentIndex < fragments.size - 1) {
            currentFragmentIndex++
            loadFragment(fragments[currentFragmentIndex])
        } else {
            saveClientToFirestore()
        }
    }

    private fun handleBackPress() {
        if (currentFragmentIndex == 0) {
            finish()
        } else {
            currentFragmentIndex--
            loadFragment(fragments[currentFragmentIndex])
        }
    }

    private fun updateButtonState() {
        binding.submitButton.text = if (currentFragmentIndex == fragments.size - 1) {
            "Cadastrar"
        } else {
            "Próximo"
        }
    }

    private fun validateCurrentFragment(): Boolean {
        val currentFragment = fragments[currentFragmentIndex]
        return when (currentFragment) {
            is PersonalDataFragment -> currentFragment.validateInputs()
            is AddressFragment -> currentFragment.validateInputs()
            else -> true
        }
    }

    private fun saveClientToFirestore() {
        val userUID = FirebaseAuth.getInstance().currentUser?.uid
        if (userUID == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val clientsRef = db.collection("Users").document(userUID).collection("Clients")

        // Conta os clientes existentes para determinar o próximo ID
        clientsRef.get()
            .addOnSuccessListener { snapshot ->
                val nextId = String.format("%04d", snapshot.size() + 1) // Gera ID sequencial
                clientData.id = nextId // Atualiza o ID na entidade

                // Formata a data no padrão dd/MM/yyyy
                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormatter.format(Date())

                // Converte os dados do cliente em um mapa
                val clientDataMap = mapOf(
                    "id" to clientData.id,
                    "fullName" to clientData.fullName,
                    // Salva o CPF/CNPJ com máscara
                    "cpfCnpj" to clientData.cpfCnpj,
                    "email" to clientData.email,
                    "phone" to clientData.phone,
                    "address" to mapOf(
                        "street" to clientData.address.street,
                        "number" to clientData.address.number,
                        "neighborhood" to clientData.address.neighborhood,
                        "city" to clientData.address.city,
                        "state" to clientData.address.state,
                        "zipCode" to clientData.address.zipCode
                    ),
                    "notes" to clientData.notes,
                    "createdAt" to formattedDate, // Salva a data formatada
                    "updatedAt" to formattedDate  // Salva a data formatada
                )

                // Salva os dados no Firestore usando o ID gerado
                clientsRef.document(nextId).set(clientDataMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cliente cadastrado com sucesso", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao cadastrar cliente: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao acessar Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onBackPressed() {
        handleBackPress()
        super.onBackPressed()
    }
}
