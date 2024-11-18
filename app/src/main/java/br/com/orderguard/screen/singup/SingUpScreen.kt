package br.com.orderguard.screen.singup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
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
import br.com.orderguard.screen.MainScreen
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SingUpScreen : AppCompatActivity() {

    private lateinit var binding: SingupScreenBinding
    private val auth = FirebaseAuth.getInstance()
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
                validateCNPJWithAPI(binding.cnpjEditText.text.toString().trim())
            }
        }

        // Máscara de CNPJ
        applyCnpjMask()

        // Máscara de Telefone
        applyPhoneMask()

        // Configuração para alternar a visibilidade da senha
        togglePasswordVisibility()

        // Configuração para alternar a visibilidade da confirmação de senha
        toggleConfirmPasswordVisibility()
    }

    private fun applyCnpjMask() {
        binding.cnpjEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "##.###.###/####-##"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    isUpdating = false
                    return
                }

                var unmasked = s.toString().replace(Regex("[^\\d]"), "")
                if (unmasked.length > 14) unmasked = unmasked.substring(0, 14)

                val masked = StringBuilder()
                var index = 0
                for (char in mask.toCharArray()) {
                    if (char == '#') {
                        if (index >= unmasked.length) break
                        masked.append(unmasked[index])
                        index++
                    } else {
                        masked.append(char)
                    }
                }

                isUpdating = true
                binding.cnpjEditText.setText(masked.toString())
                binding.cnpjEditText.setSelection(masked.length)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyPhoneMask() {
        binding.telefoneEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "(##) #####-####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    isUpdating = false
                    return
                }

                var unmasked = s.toString().replace(Regex("[^\\d]"), "")
                if (unmasked.length > 11) unmasked = unmasked.substring(0, 11)

                val masked = StringBuilder()
                var index = 0
                for (char in mask.toCharArray()) {
                    if (char == '#') {
                        if (index >= unmasked.length) break
                        masked.append(unmasked[index])
                        index++
                    } else {
                        masked.append(char)
                    }
                }

                isUpdating = true
                binding.telefoneEditText.setText(masked.toString())
                binding.telefoneEditText.setSelection(masked.length)
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
        val nomeEmpresa = binding.nomeEmpresaEditText.text.toString().trim()
        val cnpj = binding.cnpjEditText.text.toString().trim()
        val telefone = binding.telefoneEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val senha = binding.passwordEditText.text.toString()
        val confirmaSenha = binding.confirmaSenhaEditText.text.toString()

        when {
            nomeEmpresa.isEmpty() -> {
                binding.nomeEmpresaEditText.error = "O nome da empresa é obrigatório"
                return false
            }
            cnpj.isEmpty() -> {
                binding.cnpjEditText.error = "CNPJ é obrigatório"
                return false
            }
            telefone.isEmpty() -> {
                binding.telefoneEditText.error = "O telefone é obrigatório"
                return false
            }
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailEditText.error = "Email inválido"
                return false
            }
            senha.isEmpty() -> {
                binding.passwordEditText.error = "A senha é obrigatória"
                return false
            }
            senha.length < 6 -> {
                binding.passwordEditText.error = "A senha deve ter no mínimo 6 caracteres"
                return false
            }
            senha != confirmaSenha -> {
                binding.confirmaSenhaEditText.error = "As senhas não coincidem"
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
                    Toast.makeText(this@SingUpScreen, "Erro ao validar CNPJ", Toast.LENGTH_SHORT).show()
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
                            registerUser()
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

    private fun registerUser() {
        val email = binding.emailEditText.text.toString().trim()
        val senha = binding.passwordEditText.text.toString()

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    clearFields()
                    startActivity(Intent(this, MainScreen::class.java))
                } else {
                    Toast.makeText(this, "Erro no cadastro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
