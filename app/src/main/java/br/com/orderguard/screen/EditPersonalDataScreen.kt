package br.com.orderguard.screen.profile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.orderguard.databinding.EditPersonalDataScreenBinding
import br.com.orderguard.screen.home.MainScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class EditPersonalDataScreen : AppCompatActivity() {

    private lateinit var binding: EditPersonalDataScreenBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditPersonalDataScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura botão de voltar
        binding.btnBack.setOnClickListener {
            navigateToMainScreen()
        }

        // Aplica máscara no campo de CNPJ
        applyCnpjMask()

        // Carrega dados do Firestore ao abrir a tela
        loadPersonalData()

        // Configura botão Salvar
        binding.salvarButton.setOnClickListener {
            savePersonalData()
        }
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, MainScreen::class.java)
        intent.putExtra("navigateTo", "settings")
        startActivity(intent)
        finish() // Fecha a tela atual
    }

    private fun applyCnpjMask() {
        binding.cnpjEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var oldText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldText = s?.toString() ?: ""
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val currentText = s?.toString() ?: ""
                val unmasked = currentText.replace(Regex("[^\\d]"), "")

                // Verifica se é exclusão
                if (before > 0 && count == 0) {
                    return
                }

                val mask = "##.###.###/####-##"
                var masked = ""
                var index = 0

                for (char in mask) {
                    if (char == '#') {
                        if (index >= unmasked.length) break
                        masked += unmasked[index]
                        index++
                    } else {
                        masked += char
                    }
                }

                isUpdating = true
                binding.cnpjEditText.setText(masked)
                binding.cnpjEditText.setSelection(masked.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {
                val unmasked = s?.toString()?.replace(Regex("[^\\d]"), "") ?: ""
                if (unmasked.length == 14) {
                    fetchCnpjDetails(unmasked)
                }
            }
        })
    }

    private fun fetchCnpjDetails(cnpj: String) {
        val url = "https://brasilapi.com.br/api/cnpj/v1/$cnpj"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@EditPersonalDataScreen, "Erro ao validar o CNPJ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val jsonObject = JSONObject(jsonResponse ?: "{}")

                    runOnUiThread {
                        if (jsonObject.has("error")) {
                            binding.cnpjEditText.error = "CNPJ inválido ou não encontrado"
                        } else {
                            Toast.makeText(this@EditPersonalDataScreen, "CNPJ válido", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        binding.cnpjEditText.error = "CNPJ inválido ou não encontrado"
                    }
                }
            }
        })
    }

    private fun loadPersonalData() {
        val userUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userUID)

        // Busca os dados do Firestore
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Preenche os campos com os dados existentes
                binding.nomeEmpresaEditText.setText(document.getString("companyName") ?: "")
                binding.cnpjEditText.setText(document.getString("cnpj") ?: "")
                binding.telefoneEditText.setText(document.getString("phone") ?: "")
                binding.emailEditText.setText(document.getString("email") ?: "")
            } else {
                Toast.makeText(this, "Dados não encontrados!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao carregar dados: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePersonalData() {
        val userUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userUID)

        // Mapeia os dados atualizados
        val updatedData = mapOf(
            "companyName" to binding.nomeEmpresaEditText.text.toString(),
            "cnpj" to binding.cnpjEditText.text.toString(),
            "phone" to binding.telefoneEditText.text.toString(),
            "email" to binding.emailEditText.text.toString()
        )

        // Salva os dados no Firestore
        userRef.update(updatedData).addOnSuccessListener {
            Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
            navigateToMainScreen() // Volta à tela principal após salvar
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao salvar dados: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
