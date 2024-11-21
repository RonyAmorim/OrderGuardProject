package br.com.orderguard.screen.singup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import br.com.orderguard.R
import br.com.orderguard.databinding.SingupScreenBinding
import br.com.orderguard.screen.login.LoginScreen
import br.com.orderguard.screen.home.MainScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SingUpScreen : AppCompatActivity() {

    private lateinit var binding: SingupScreenBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SingupScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar o layout para ser edge-to-edge
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuração do botão para voltar
        val backButton: ImageButton = findViewById(R.id.btn_back_cad)
        backButton.setOnClickListener {
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }

        // Listener do botão de cadastro
        binding.cadastrarButton.setOnClickListener {
            if (validateInputFields()) {
                // Remover máscara antes de enviar para a API
                val cnpjUnmasked = binding.cnpjEditText.text.toString().replace(Regex("[^\\d]"), "")
                validateCNPJWithAPI(cnpjUnmasked)
            }
        }

        // Aplicar máscara de telefone
        applyPhoneMask()

        // Aplicar máscara de CNPJ
        applyCnpjMask()

        // Configuração para alternar a visibilidade da senha
        togglePasswordVisibility()

        // Configuração para alternar a visibilidade da confirmação de senha
        toggleConfirmPasswordVisibility()
    }

    // Máscara de Telefone
    private fun applyPhoneMask() {
        binding.telefoneEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var oldText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldText = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val unmasked = s.toString().replace(Regex("[^\\d]"), "")

                if (unmasked == oldText.replace(Regex("[^\\d]"), "")) {
                    return
                }

                var masked = ""
                val mask = "(##) #####-####"
                var index = 0

                for (char in mask) {
                    if (char == '#') {
                        if (index >= unmasked.length) break
                        masked += unmasked[index]
                        index++
                    } else {
                        if (index >= unmasked.length) break
                        masked += char
                    }
                }

                isUpdating = true
                binding.telefoneEditText.setText(masked)
                binding.telefoneEditText.setSelection(masked.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Máscara de CNPJ
    private fun applyCnpjMask() {
        binding.cnpjEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var oldText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldText = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val unmasked = s.toString().replace(Regex("[^\\d]"), "")

                if (unmasked == oldText.replace(Regex("[^\\d]"), "")) {
                    return
                }

                var masked = ""
                val mask = "##.###.###/####-##"
                var index = 0

                for (char in mask) {
                    if (char == '#') {
                        if (index >= unmasked.length) break
                        masked += unmasked[index]
                        index++
                    } else {
                        if (index >= unmasked.length) break
                        masked += char
                    }
                }

                isUpdating = true
                binding.cnpjEditText.setText(masked)
                binding.cnpjEditText.setSelection(masked.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun togglePasswordVisibility() {
        binding.togglePasswordVisibility.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.passwordEditText.inputType = if (isPasswordVisible)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.togglePasswordVisibility.setImageResource(
                if (isPasswordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
            )
            binding.passwordEditText.setSelection(binding.passwordEditText.text.length)
        }
    }

    private fun toggleConfirmPasswordVisibility() {
        binding.toggleConfirmPasswordVisibility.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            binding.confirmaSenhaEditText.inputType = if (isConfirmPasswordVisible)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.toggleConfirmPasswordVisibility.setImageResource(
                if (isConfirmPasswordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
            )
            binding.confirmaSenhaEditText.setSelection(binding.confirmaSenhaEditText.text.length)
        }
    }

    private fun validateInputFields(): Boolean {
        val companyName = binding.nomeEmpresaEditText.text.toString().trim()
        val cnpj = binding.cnpjEditText.text.toString().trim()
        val phone = binding.telefoneEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmaSenhaEditText.text.toString()

        when {
            companyName.isEmpty() -> {
                binding.nomeEmpresaEditText.error = "Company name is required"
                return false
            }
            cnpj.isEmpty() -> {
                binding.cnpjEditText.error = "CNPJ is required"
                return false
            }
            phone.isEmpty() -> {
                binding.telefoneEditText.error = "Phone number is required"
                return false
            }
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailEditText.error = "Invalid email"
                return false
            }
            password.isEmpty() -> {
                binding.passwordEditText.error = "Password is required"
                return false
            }
            password.length < 6 -> {
                binding.passwordEditText.error = "Password must be at least 6 characters"
                return false
            }
            password != confirmPassword -> {
                binding.confirmaSenhaEditText.error = "Passwords do not match"
                return false
            }
            else -> return true
        }
    }

    private fun validateCNPJWithAPI(cnpj: String) {
        val url = "https://brasilapi.com.br/api/cnpj/v1/$cnpj"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SingUpScreen, "Failed to validate CNPJ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val jsonObject = JSONObject(jsonResponse ?: "{}")

                    runOnUiThread {
                        if (jsonObject.has("error")) {
                            binding.cnpjEditText.error = "Invalid or not found CNPJ"
                        } else {
                            registerUser()
                        }
                    }
                } else {
                    runOnUiThread {
                        binding.cnpjEditText.error = "Invalid or not found CNPJ"
                    }
                }
            }
        })
    }

    private fun registerUser() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = hashMapOf(
                        "companyName" to binding.nomeEmpresaEditText.text.toString(),
                        "cnpj" to binding.cnpjEditText.text.toString(),
                        "phone" to binding.telefoneEditText.text.toString(),
                        "email" to email
                    )

                    db.collection("Users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            clearFields()
                            startActivity(Intent(this, MainScreen::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Registration error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun clearFields() {
        binding.nomeEmpresaEditText.setText("")
        binding.cnpjEditText.setText("")
        binding.telefoneEditText.setText("")
        binding.emailEditText.setText("")
        binding.passwordEditText.setText("")
        binding.confirmaSenhaEditText.setText("")
    }
}
